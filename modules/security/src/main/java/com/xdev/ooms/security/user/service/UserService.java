package com.xdev.ooms.security.user.service;

import com.xdev.ooms.security.confirmationcode.service.ConfirmationCodeService;
import com.xdev.ooms.security.companyprofile.entity.CompanyProfile;
import com.xdev.ooms.security.companyprofile.repository.CompanyProfileRepository;
import com.xdev.ooms.security.role.repository.RoleRepository;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.security.confirmationcode.dto.ConfirmationCodeDTO;
import com.xdev.ooms.security.user.dto.AssignableUserDTO;
import com.xdev.ooms.security.user.dto.OSMUserDTO;
import com.xdev.ooms.security.user.dto.OSMUserOUTDTO;
import com.xdev.ooms.security.user.dto.UpdatePasswordDTO;
import com.xdev.ooms.security.user.dto.UserProfileUpdateDTO;
import com.xdev.ooms.security.user.dto.UserPhotoDTO;
import com.xdev.ooms.security.confirmationcode.entity.ConfirmationCode;
import com.xdev.ooms.security.user.entity.OSMUser;
import com.xdev.ooms.security.role.entity.Role;
import com.xdev.ooms.security.confirmationcode.enums.ConfirmationCodeType;
import com.xdev.ooms.security.confirmationcode.enums.ConfirmationMethod;
import com.xdev.ooms.sharedkernel.mail.models.EmailBranding;
import com.xdev.ooms.sharedkernel.mail.models.MailRequest;
import com.xdev.ooms.sharedkernel.mail.services.MailService;
import com.xdev.ooms.sharedkernel.mail.templates.OsmMailComposer;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models.SearchResponse;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.models.OSMModule;
import com.xdev.ooms.sharedkernel.models.SearchData;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import com.xdev.ooms.sharedkernel.utils.SecurityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import jakarta.persistence.EntityNotFoundException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService extends BaseServiceImpl<OSMUser, OSMUserDTO, OSMUserOUTDTO> implements UserDetailsService {
    private static final int MAX_PHOTO_BYTES = 200 * 1024;
    private static final Set<String> ALLOWED_PHOTO_TYPES = Set.of("image/png", "image/jpeg", "image/jpg");
    private static final String USER_MGMT_MODULE = "HABILITATION";
    private static final String USER_MGMT_ENTITY = "OSMUSER";

    private final UserRepository userRepository;
    private final MailService mailService;
    private final OsmMailComposer mailComposer;
    private final ConfirmationCodeService confirmationCodeService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CompanyProfileRepository companyProfileRepository;

    protected UserService(BaseRepository<OSMUser> repository, ModelMapper modelMapper, UserRepository userRepository, MailService mailService, OsmMailComposer mailComposer, ConfirmationCodeService confirmationCodeService, PasswordEncoder passwordEncoder, RoleRepository roleRepository, CompanyProfileRepository companyProfileRepository) {
        super(repository, modelMapper);

        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "UserService", "Initializing UserService");

        try {
            this.userRepository = userRepository;
            this.mailService = mailService;
            this.mailComposer = mailComposer;
            this.confirmationCodeService = confirmationCodeService;
            this.passwordEncoder = passwordEncoder;

            OSMLogger.logMethodExit(this.getClass(), "UserService", "UserService initialized successfully");
            OSMLogger.logPerformance(this.getClass(), "UserService", startTime, System.currentTimeMillis());
            OSMLogger.logSecurityEvent(this.getClass(), "USER_SERVICE_INITIALIZED",
                    "User service initialized successfully");

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error initializing UserService", e);
            throw e;
        }
        this.roleRepository = roleRepository;
        this.companyProfileRepository = companyProfileRepository;
    }


    @Override
    public SearchResponse<OSMUser, OSMUserOUTDTO> search(SearchData searchData) {
        assertCanListUsers();
        SearchResponse<OSMUser, OSMUserOUTDTO> response = super.search(searchData);
        enrichTenantNames(response.getData());
        stripPhotoPayloads(response.getData());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OSMUserOUTDTO findById(UUID id) {
        OSMUser target = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with this id " + id));

        if (isCurrentUser(id)) {
            String username = SecurityUtils.getCurrentUsername()
                    .orElseThrow(() -> new UsernameNotFoundException("Unauthorized"));
            return getCurrentUserProfile(username);
        }

        assertCanViewUser(target);
        OSMUserOUTDTO result = modelMapper.map(target, OSMUserOUTDTO.class);
        enrichTenantNames(List.of(result));
        stripPhotoPayloads(List.of(result));
        return result;
    }

    private void stripPhotoPayloads(List<OSMUserOUTDTO> users) {
        if (users == null) {
            return;
        }
        for (OSMUserOUTDTO user : users) {
            if (user == null) {
                continue;
            }
            user.setPhotoData(null);
            user.setPhotoContentType(null);
        }
    }

    private void assertCanListUsers() {
        if (!hasUserManagementPermission("READ")) {
            throw new AccessDeniedException("You are not allowed to list users");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OSMUserOUTDTO> findAll() {
        assertCanListUsers();
        List<OSMUserOUTDTO> users = super.findAll();
        enrichTenantNames(users);
        stripPhotoPayloads(users);
        return users;
    }

    private void enrichTenantNames(List<OSMUserOUTDTO> users) {
        if (users == null || users.isEmpty()) {
            return;
        }

        Set<UUID> tenantIds = users.stream()
                .map(OSMUserOUTDTO::getTenantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (tenantIds.isEmpty()) {
            return;
        }

        Map<UUID, String> tenantNames = new HashMap<>();
        companyProfileRepository.findActiveByIdOrTenantIdIn(tenantIds).forEach(profile -> {
            String name = profile.getLegalName() != null ? profile.getLegalName() : "";
            tenantNames.put(profile.getId(), name);
            if (profile.getTenantId() != null) {
                tenantNames.put(profile.getTenantId(), name);
            }
        });

        users.forEach(user -> {
            UUID tenantId = user.getTenantId();
            if (tenantId != null) {
                user.setTenantName(tenantNames.getOrDefault(tenantId, ""));
            }
        });
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "loadUserByUsername", "Loading user details for username: " + username);

        try {
            UserDetails userDetails = userRepository.findByUsernameAndIsDeletedFalse(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            OSMLogger.logMethodExit(this.getClass(), "loadUserByUsername", "User details loaded successfully for: " + username);
            OSMLogger.logPerformance(this.getClass(), "loadUserByUsername", startTime, System.currentTimeMillis());
            OSMLogger.logSecurityEvent(this.getClass(), "USER_DETAILS_LOADED",
                    "User details loaded successfully for username: " + username);

            return userDetails;

        } catch (UsernameNotFoundException e) {
            OSMLogger.logSecurityEvent(this.getClass(), "USER_NOT_FOUND",
                    "User not found during authentication: " + username);
            throw e;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Unexpected error loading user details for username: " + username, e);
            throw e;
        }
    }

    // Fixed as part of TICKET-001: Roll back on checked exceptions during user activation/addition
    @Transactional(rollbackFor = Exception.class)
    public OSMUserOUTDTO addUser(OSMUserOUTDTO userDTO) throws Exception {
        long startTime = System.currentTimeMillis();
        String username = userDTO != null ? userDTO.getUsername() : "null";
        OSMLogger.logMethodEntry(this.getClass(), "addUser", "Adding new user: " + username);

        try {
            assertCanCreateUser();
            validateUserDTO(userDTO);
            checkExistUser(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPhoneNumber());

            String rawPassword = generateSecureCode(8);
            String hashedPassword = passwordEncoder.encode(rawPassword);
            boolean mailEnabled = mailService.isDeliveryEnabled();

            OSMUser user = modelMapper.map(userDTO, OSMUser.class);
            user.setPassword(hashedPassword);
            user.setNewUser(mailEnabled);
            user.setEnabled(true);

            OSMUser savedUser = userRepository.save(user);
            dispatchConfirmationAsync(userDTO, rawPassword);

            OSMLogger.logMethodExit(this.getClass(), "addUser", "User added successfully: " + username);
            OSMLogger.logPerformance(this.getClass(), "addUser", startTime, System.currentTimeMillis());
            OSMLogger.logSecurityEvent(this.getClass(), "USER_ADDED",
                    "New user added successfully: " + username);

            OSMUserOUTDTO result = modelMapper.map(savedUser, OSMUserOUTDTO.class);
            if (!mailEnabled) {
                result.setInitialPassword(rawPassword);
            }
            return result;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error adding user: " + username, e);
            throw e;
        }
    }

    // Fixed as part of TICKET-001: Roll back on checked exceptions during user update
    @Transactional(rollbackFor = Exception.class)
    public OSMUserOUTDTO updateUser(OSMUserOUTDTO userDTO, UUID id) throws Exception {
        long startTime = System.currentTimeMillis();
        String username = userDTO != null ? userDTO.getUsername() : "null";
        OSMLogger.logMethodEntry(this.getClass(), "updateUser", "Updating user: " + username + " with ID: " + id);

        try {
            if (id == null) throw new IllegalArgumentException("User ID must not be null");

            validateUserDTO(userDTO);

            OSMUser user = repository.findByIdAndIsDeletedFalse(id)
                    .orElseThrow(() -> new UsernameNotFoundException(id.toString()));

            assertCanManageUser(user);
            checkUserToUpdate(user, userDTO.getUsername(), userDTO.getEmail(), userDTO.getPhoneNumber());

            boolean usernameChanged = !Objects.equals(userDTO.getUsername(), user.getUsername());
            boolean emailChanged = userDTO.getEmail() != null && !Objects.equals(userDTO.getEmail(), user.getEmail());
            boolean phoneChanged = userDTO.getPhoneNumber() != null && !Objects.equals(userDTO.getPhoneNumber(), user.getPhoneNumber());
            Role newRole = roleRepository.findByIdAndIsDeletedFalse(userDTO.getRole().getId()).orElse(null);
            user.setLocked(userDTO.isLocked());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setUsername(userDTO.getUsername());
            user.setConfirmationMethod(userDTO.getConfirmationMethod());
            if (newRole != null) {
                user.setRole(newRole);
            }

            if (userDTO.getEmail() != null) {
                user.setEmail(userDTO.getEmail());
            }

            if (userDTO.getPhoneNumber() != null) {
                user.setPhoneNumber(userDTO.getPhoneNumber());
            }

            userRepository.save(user);

            if (usernameChanged || emailChanged || phoneChanged) {
                String rawPassword = generateSecureCode(8);
                user.setPassword(passwordEncoder.encode(rawPassword));
                userRepository.save(user);
                dispatchConfirmationAsync(userDTO, rawPassword);
                OSMLogger.logSecurityEvent(this.getClass(), "USER_CREDENTIALS_UPDATED",
                        "User credentials updated and new password sent: " + username);
            }

            OSMLogger.logMethodExit(this.getClass(), "updateUser", "User updated successfully: " + username + " with ID: " + id);
            OSMLogger.logPerformance(this.getClass(), "updateUser", startTime, System.currentTimeMillis());
            OSMLogger.logSecurityEvent(this.getClass(), "USER_UPDATED",
                    "User updated successfully: " + username + " with ID: " + id);

            return modelMapper.map(user, OSMUserOUTDTO.class);

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error updating user: " + username + " with ID: " + id, e);
            throw e;
        }
    }

    private void checkExistUser(String username, String email, String phoneNumber) {
        if (username != null && userRepository.findByUsernameAndIsDeletedFalse(username).isPresent()) {
            OSMLogger.logSecurityEvent(this.getClass(), "USERNAME_ALREADY_EXISTS",
                    "Username already exists: " + username);
            throw new IllegalArgumentException("Username is already in use");
        }
        if (email != null && userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(email).isPresent()) {
            OSMLogger.logSecurityEvent(this.getClass(), "EMAIL_ALREADY_EXISTS",
                    "Email already exists: " + email);
            throw new IllegalArgumentException("Email is already in use");
        }
        if (phoneNumber != null && userRepository.findByPhoneNumberAndIsDeletedFalse(phoneNumber).isPresent()) {
            OSMLogger.logSecurityEvent(this.getClass(), "PHONE_ALREADY_EXISTS",
                    "Phone number already exists: " + phoneNumber);
            throw new IllegalArgumentException("Phone number is already in use");
        }
    }

    private void checkUserToUpdate(OSMUser user, String username, String email, String phoneNumber) {
        if (((user.getUsername() != null && username != null && !user.getUsername().equals(username)) || (user.getUsername() == null && username != null))
                && userRepository.findByUsernameAndIsDeletedFalse(username)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .isPresent()) {
            OSMLogger.logSecurityEvent(this.getClass(), "USERNAME_ALREADY_EXISTS_UPDATE",
                    "Username already exists during update: " + username);
            throw new IllegalArgumentException("Username is already in use");
        }
        if (((user.getEmail() != null && email != null && !user.getEmail().equals(email)) || (user.getEmail() == null && email != null))
                && userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .isPresent()) {
            OSMLogger.logSecurityEvent(this.getClass(), "EMAIL_ALREADY_EXISTS_UPDATE",
                    "Email already exists during update: " + email);
            throw new IllegalArgumentException("Email is already in use");
        }
        if (((user.getPhoneNumber() != null && phoneNumber != null && !user.getPhoneNumber().equals(phoneNumber)) || (user.getPhoneNumber() == null && phoneNumber != null))
                && userRepository.findByPhoneNumberAndIsDeletedFalse(phoneNumber)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .isPresent()) {
            OSMLogger.logSecurityEvent(this.getClass(), "PHONE_ALREADY_EXISTS_UPDATE",
                    "Phone number already exists during update: " + phoneNumber);
            throw new IllegalArgumentException("Phone number is already in use");
        }
    }

    public OSMUser getByUsername(String username) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getByUsername", "Getting user by username: " + username);

        try {
            OSMUser user = userRepository.findByUsernameAndIsDeletedFalse(username).orElse(null);

            if (user != null) {
                OSMLogger.logMethodExit(this.getClass(), "getByUsername", "User found: " + username);
                OSMLogger.logPerformance(this.getClass(), "getByUsername", startTime, System.currentTimeMillis());
            } else {
                OSMLogger.logMethodExit(this.getClass(), "getByUsername", "User not found: " + username);
                OSMLogger.logPerformance(this.getClass(), "getByUsername", startTime, System.currentTimeMillis());
                OSMLogger.logSecurityEvent(this.getClass(), "USER_NOT_FOUND_BY_USERNAME",
                        "User not found by username: " + username);
            }

            return user;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error getting user by username: " + username, e);
            throw e;
        }
    }

    public OSMUser getByUsernameWithFreshPermissions(String username) {
        return userRepository.findByUsernameWithRolePermissions(username)
                .or(() -> userRepository.findByUsernameAndIsDeletedFalse(username))
                .orElse(null);
    }

    public void sendWelcomeCredentials(OSMUserOUTDTO userDTO, String rawPassword) {
        dispatchConfirmationAsync(userDTO, rawPassword);
    }

    private void dispatchConfirmationAsync(OSMUserOUTDTO userDTO, String rawPassword) {
        if (userDTO == null) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                sendConfirmation(userDTO, rawPassword);
            } catch (Exception e) {
                OSMLogger.logException(this.getClass(),
                        "Async confirmation failed for user: " + userDTO.getUsername(), e);
            }
        });
    }

    private EmailBranding resolveBranding(UUID tenantId) {
        EmailBranding branding = mailComposer.defaultBranding();
        if (tenantId == null) {
            return branding;
        }

        companyProfileRepository.findById(tenantId).ifPresent(profile -> {
            if (profile.getLegalName() != null && !profile.getLegalName().isBlank()) {
                branding.setCompanyName(profile.getLegalName());
            }
            if (profile.getEmail() != null && !profile.getEmail().isBlank()) {
                branding.setSupportEmail(profile.getEmail());
            }
        });

        return branding;
    }

    private void sendConfirmation(OSMUserOUTDTO userDTO, String rawPassword) throws Exception {
        long startTime = System.currentTimeMillis();
        String username = userDTO != null ? userDTO.getUsername() : "null";
        OSMLogger.logMethodEntry(this.getClass(), "sendConfirmation",
                "Sending confirmation for user: " + username + ", Method: " + userDTO.getConfirmationMethod());

        try {
            switch (userDTO.getConfirmationMethod()) {
                case EMAIL -> {
                    MailRequest mailRequest = mailComposer.composeWelcomeCredentials(
                            userDTO.getEmail(),
                            userDTO.getUsername(),
                            rawPassword,
                            resolveBranding(userDTO.getTenantId())
                    );
                    mailService.sendEmail(mailRequest);

                    OSMLogger.logSecurityEvent(this.getClass(), "CONFIRMATION_EMAIL_SENT",
                            "Confirmation email sent to: " + userDTO.getEmail());
                }
                case PHONE -> {
                    //TODO send sms message
                    OSMLogger.logSecurityEvent(this.getClass(), "CONFIRMATION_SMS_PENDING",
                            "SMS confirmation pending for phone: " + userDTO.getPhoneNumber());
                }
                default -> {
                    OSMLogger.logSecurityEvent(this.getClass(), "UNSUPPORTED_CONFIRMATION_METHOD",
                            "Unsupported confirmation method: " + userDTO.getConfirmationMethod());
                    throw new IllegalArgumentException("Unsupported confirmation method");
                }
            }

            OSMLogger.logMethodExit(this.getClass(), "sendConfirmation",
                    "Confirmation sent successfully for user: " + username);
            OSMLogger.logPerformance(this.getClass(), "sendConfirmation", startTime, System.currentTimeMillis());

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error sending confirmation for user: " + username, e);
            throw e;
        }
    }

    // Fixed as part of TICKET-001: Make password reset atomic and roll back if email delivery fails
    @Transactional(rollbackFor = Exception.class)
    public OSMUserOUTDTO resetPassword(String identifier) throws Exception {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "resetPassword", "Password reset request for identifier: " + identifier);

        try {
            OSMUser user = userRepository.findByPhoneOrEmailIgnoreCase(identifier).orElse(null);
            if (user != null) {
                if (user.isLocked()) {
                    OSMLogger.logSecurityEvent(this.getClass(), "PASSWORD_RESET_ACCOUNT_LOCKED",
                            "Password reset failed - Account locked for identifier: " + identifier);
                    throw new AccountLockedException("Invalid input");
                }

                if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(identifier)) {
                    String code = generateRandomCode();
                    ConfirmationCode confirmationCode = new ConfirmationCode();
                    confirmationCode.setCode(passwordEncoder.encode(code));
                    confirmationCode.setUser(user);
                    confirmationCode.setConfirmationCodeType(ConfirmationCodeType.RESETPASSWORD);
                    confirmationCode.setFailedAttempts(0);
                    confirmationCode.setConsumedAt(null);
                    saveConfirmationCode(confirmationCode);

                    MailRequest mailrequest = mailComposer.composePasswordReset(
                            user.getEmail(),
                            code,
                            user.getId().toString(),
                            resolveBranding(user.getTenantId())
                    );
                    mailService.sendEmail(mailrequest);

                    OSMLogger.logSecurityEvent(this.getClass(), "PASSWORD_RESET_CODE_SENT",
                            "Password reset code sent to email: " + user.getEmail());
                } else {
                    //TODO send to phone number
                    OSMLogger.logSecurityEvent(this.getClass(), "PASSWORD_RESET_SMS_PENDING",
                            "Password reset SMS pending for phone: " + identifier);
                }

                OSMLogger.logMethodExit(this.getClass(), "resetPassword",
                        "Password reset initiated successfully for identifier: " + identifier);
                OSMLogger.logPerformance(this.getClass(), "resetPassword", startTime, System.currentTimeMillis());
                OSMLogger.logSecurityEvent(this.getClass(), "PASSWORD_RESET_INITIATED",
                        "Password reset initiated successfully for identifier: " + identifier);

                return modelMapper.map(user, OSMUserOUTDTO.class);

            } else {
                OSMLogger.logSecurityEvent(this.getClass(), "PASSWORD_RESET_INVALID_IDENTIFIER",
                        "Password reset failed - Invalid identifier: " + identifier);
                throw new IllegalArgumentException("Invalid input");
            }

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error during password reset for identifier: " + identifier, e);
            throw e;
        }
    }

    private ConfirmationCode saveConfirmationCode(ConfirmationCode code) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "saveConfirmationCode",
                "Saving confirmation code for user: " + (code.getUser() != null ? code.getUser().getUsername() : "null"));

        try {
            ConfirmationCode existedCode = confirmationCodeService.getByConfirmationCodeTypeAndUser(ConfirmationCodeType.RESETPASSWORD, code.getUser());
            if (existedCode != null) {
                existedCode.setCode(code.getCode());
                existedCode.setFailedAttempts(0);
                existedCode.setConsumedAt(null);
                ConfirmationCode savedCode = confirmationCodeService.persist(existedCode);

                OSMLogger.logMethodExit(this.getClass(), "saveConfirmationCode", "Existing confirmation code updated");
                OSMLogger.logPerformance(this.getClass(), "saveConfirmationCode", startTime, System.currentTimeMillis());

                return savedCode;
            }

            ConfirmationCode savedCode = confirmationCodeService.persist(code);

            OSMLogger.logMethodExit(this.getClass(), "saveConfirmationCode", "New confirmation code saved");
            OSMLogger.logPerformance(this.getClass(), "saveConfirmationCode", startTime, System.currentTimeMillis());

            return savedCode;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error saving confirmation code for user: " + (code.getUser() != null ? code.getUser().getUsername() : "null"), e);
            throw e;
        }
    }


    public boolean validateResetCode(String code, UUID userId) throws Exception {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "validateResetCode",
                "Validating reset code for user: " + userId + ", Code: " + (code != null ? code.substring(0, Math.min(3, code.length())) + "..." : "null"));

        try {
            OSMUser user = userRepository.findByIdAndIsDeletedFalse(userId).orElse(null);
            if (user == null) {
                OSMLogger.logSecurityEvent(this.getClass(), "RESET_CODE_VALIDATION_USER_NOT_FOUND",
                        "Reset code validation failed - User not found: " + userId);
                throw new IllegalArgumentException("Invalid code");
            }

            ConfirmationCode existedCode = confirmationCodeService.getByConfirmationCodeTypeAndUser(ConfirmationCodeType.RESETPASSWORD, user);
            if (existedCode == null || existedCode.isConsumed() || existedCode.getFailedAttempts() >= 3) {
                OSMLogger.logSecurityEvent(this.getClass(), "RESET_CODE_VALIDATION_INVALID",
                        "Reset code validation failed - Invalid code for user: " + userId);
                throw new IllegalArgumentException("Invalid code");
            }
            if (existedCode.isExpired()) {
                OSMLogger.logSecurityEvent(this.getClass(), "RESET_CODE_VALIDATION_EXPIRED",
                        "Reset code validation failed - Code expired for user: " + userId);
                throw new CredentialExpiredException("Expired code");
            }
            if (code == null || !passwordEncoder.matches(code, existedCode.getCode())) {
                existedCode.setFailedAttempts(existedCode.getFailedAttempts() + 1);
                confirmationCodeService.persist(existedCode);
                OSMLogger.logSecurityEvent(this.getClass(), "RESET_CODE_VALIDATION_INVALID",
                        "Reset code validation failed - Invalid code for user: " + userId);
                throw new IllegalArgumentException("Invalid code");
            }

            OSMLogger.logMethodExit(this.getClass(), "validateResetCode",
                    "Reset code validated successfully for user: " + userId);
            OSMLogger.logPerformance(this.getClass(), "validateResetCode", startTime, System.currentTimeMillis());
            OSMLogger.logSecurityEvent(this.getClass(), "RESET_CODE_VALIDATED",
                    "Reset code validated successfully for user: " + userId);

            return true;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error validating reset code for user: " + userId, e);
            throw e;
        }
    }

    @Transactional
    public void updatePassword(UpdatePasswordDTO dto, UUID userId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "updatePassword", "Updating password for user: " + userId);

        try {
            OSMUser user = userRepository.findByIdAndIsDeletedFalse(userId).orElse(null);
            if (user == null) {
                OSMLogger.logSecurityEvent(this.getClass(), "PASSWORD_UPDATE_USER_NOT_FOUND",
                        "Password update failed - User not found: " + userId);
                throw new IllegalArgumentException("Invalid user");
            }

            if (dto == null
                    || dto.getResetCode() == null
                    || dto.getNewPassword() == null
                    || !dto.getNewPassword().equals(dto.getNewPasswordConfirmation())) {
                throw new IllegalArgumentException("Invalid password reset request");
            }

            ConfirmationCode resetCode = confirmationCodeService.getByConfirmationCodeTypeAndUser(
                    ConfirmationCodeType.RESETPASSWORD, user);
            if (resetCode == null
                    || resetCode.isConsumed()
                    || resetCode.isExpired()
                    || resetCode.getFailedAttempts() >= 3
                    || !passwordEncoder.matches(dto.getResetCode(), resetCode.getCode())) {
                if (resetCode != null && !resetCode.isConsumed() && resetCode.getFailedAttempts() < 3) {
                    resetCode.setFailedAttempts(resetCode.getFailedAttempts() + 1);
                    confirmationCodeService.persist(resetCode);
                }
                throw new IllegalArgumentException("Invalid password reset request");
            }
            if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
                throw new IllegalArgumentException("New password must differ from the current password");
            }

            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            user.setNewUser(false);
            userRepository.save(user);
            resetCode.setConsumedAt(LocalDateTime.now());
            confirmationCodeService.persist(resetCode);

            OSMLogger.logMethodExit(this.getClass(), "updatePassword",
                    "Password updated successfully for user: " + userId);
            OSMLogger.logPerformance(this.getClass(), "updatePassword", startTime, System.currentTimeMillis());
            OSMLogger.logSecurityEvent(this.getClass(), "PASSWORD_UPDATED",
                    "Password updated successfully for user: " + userId);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error updating password for user: " + userId, e);
            throw e;
        }
    }

    public OSMUserOUTDTO getCurrentUserProfile(String username) {
        OSMUser user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        OSMUserOUTDTO dto = modelMapper.map(user, OSMUserOUTDTO.class);
        enrichTenantNames(List.of(dto));
        if (dto.getRole() != null) {
            dto.getRole().setPermissions(null);
        }
        stripPhotoPayloads(List.of(dto));
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public OSMUserOUTDTO updateCurrentUserProfile(String username, UserProfileUpdateDTO profileDto) {
        if (profileDto == null) {
            throw new IllegalArgumentException("Profile data must not be null");
        }

        OSMUser user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        OSMUserOUTDTO validationDto = new OSMUserOUTDTO();
        validationDto.setUsername(user.getUsername());
        validationDto.setFirstName(profileDto.getFirstName());
        validationDto.setLastName(profileDto.getLastName());
        validationDto.setEmail(profileDto.getEmail());
        validationDto.setPhoneNumber(profileDto.getPhoneNumber());
        validationDto.setConfirmationMethod(profileDto.getConfirmationMethod());
        validateUserDTO(validationDto);
        checkUserToUpdate(user, user.getUsername(), profileDto.getEmail(), profileDto.getPhoneNumber());

        user.setFirstName(profileDto.getFirstName());
        user.setLastName(profileDto.getLastName());
        user.setEmail(profileDto.getEmail());
        user.setPhoneNumber(profileDto.getPhoneNumber());
        user.setConfirmationMethod(profileDto.getConfirmationMethod());
        userRepository.save(user);

        OSMUserOUTDTO result = modelMapper.map(user, OSMUserOUTDTO.class);
        enrichTenantNames(List.of(result));
        if (result.getRole() != null) {
            result.getRole().setPermissions(null);
        }
        return result;
    }

    @Transactional
    public void changeOwnPassword(String username, UpdatePasswordDTO dto) {
        OSMUser user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (dto == null
                || dto.getOldPassword() == null
                || dto.getNewPassword() == null
                || !dto.getNewPassword().equals(dto.getNewPasswordConfirmation())) {
            throw new IllegalArgumentException("Invalid password change request");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must differ from the current password");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setNewUser(false);
        userRepository.save(user);
    }

    public UserPhotoDTO getCurrentUserPhoto(String username) {
        OSMUser user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        UserPhotoDTO dto = new UserPhotoDTO();
        dto.setPhotoData(user.getPhotoData());
        dto.setPhotoContentType(user.getPhotoContentType());
        return dto;
    }

    @Transactional
    public UserPhotoDTO updateCurrentUserPhoto(String username, UserPhotoDTO photoDto) {
        OSMUser user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        validatePhoto(photoDto.getPhotoData(), photoDto.getPhotoContentType());
        user.setPhotoData(photoDto.getPhotoData());
        user.setPhotoContentType(photoDto.getPhotoContentType());
        userRepository.save(user);

        UserPhotoDTO result = new UserPhotoDTO();
        result.setPhotoData(user.getPhotoData());
        result.setPhotoContentType(user.getPhotoContentType());
        return result;
    }

    @Transactional
    public void removeCurrentUserPhoto(String username) {
        OSMUser user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        user.setPhotoData(null);
        user.setPhotoContentType(null);
        userRepository.save(user);
    }

    private void validatePhoto(String photoData, String photoContentType) {
        if (photoData == null || photoData.isBlank() || photoContentType == null || photoContentType.isBlank()) {
            throw new IllegalArgumentException("Photo data is required");
        }
        if (!ALLOWED_PHOTO_TYPES.contains(photoContentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Only PNG and JPEG images are allowed");
        }
        int estimatedBytes = (photoData.length() * 3) / 4;
        if (estimatedBytes > MAX_PHOTO_BYTES) {
            throw new IllegalArgumentException("Photo must be 200KB or smaller");
        }
    }

    @Transactional
    public void updateInitialPassword(UpdatePasswordDTO dto, UUID userId) {
        OSMUser user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (dto == null
                || dto.getOldPassword() == null
                || dto.getNewPassword() == null
                || !dto.getNewPassword().equals(dto.getNewPasswordConfirmation())
                || !user.isNewUser()
                || !passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must differ from the temporary password");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setNewUser(false);
        userRepository.save(user);
    }

    private void validateUserDTO(OSMUserOUTDTO userDTO) {
        if (userDTO == null) {
            OSMLogger.logSecurityEvent(this.getClass(), "USER_VALIDATION_NULL_DTO",
                    "User validation failed - DTO is null");
            throw new IllegalArgumentException("User data must not be null");
        }
        if (userDTO.getUsername() == null || userDTO.getUsername().isBlank()) {
            OSMLogger.logSecurityEvent(this.getClass(), "USER_VALIDATION_MISSING_USERNAME",
                    "User validation failed - Username is missing");
            throw new IllegalArgumentException("Username is required");
        }
        if (userDTO.getConfirmationMethod() == null) {
            OSMLogger.logSecurityEvent(this.getClass(), "USER_VALIDATION_MISSING_CONFIRMATION_METHOD",
                    "User validation failed - Confirmation method is missing");
            throw new IllegalArgumentException("Confirmation method is required");
        }
        if (userDTO.getConfirmationMethod() == ConfirmationMethod.EMAIL && (userDTO.getEmail() == null || userDTO.getEmail().isBlank())) {
            OSMLogger.logSecurityEvent(this.getClass(), "USER_VALIDATION_MISSING_EMAIL",
                    "User validation failed - Email is missing for email confirmation");
            throw new IllegalArgumentException("Email is required for email confirmation");
        }
        if (userDTO.getConfirmationMethod() == ConfirmationMethod.PHONE && (userDTO.getPhoneNumber() == null || userDTO.getPhoneNumber().isBlank())) {
            OSMLogger.logSecurityEvent(this.getClass(), "USER_VALIDATION_MISSING_PHONE",
                    "User validation failed - Phone number is missing for phone confirmation");
            throw new IllegalArgumentException("Phone number is required for phone confirmation");
        }
    }

    public String generateSecureCode(int length) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "generateSecureCode", "Generating secure code with length: " + length);

        try {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            SecureRandom random = new SecureRandom();
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            String code = sb.toString();

            OSMLogger.logMethodExit(this.getClass(), "generateSecureCode", "Secure code generated successfully");
            OSMLogger.logPerformance(this.getClass(), "generateSecureCode", startTime, System.currentTimeMillis());
            OSMLogger.logSecurityEvent(this.getClass(), "SECURE_CODE_GENERATED",
                    "Secure code generated with length: " + length);

            return code;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error generating secure code with length: " + length, e);
            throw e;
        }
    }

    public String generateRandomCode() {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "generateRandomCode", "Generating random 6-digit code");

        try {
            int code = SecureRandomHolder.INSTANCE.nextInt(900_000) + 100_000;
            String codeString = String.valueOf(code);

            OSMLogger.logMethodExit(this.getClass(), "generateRandomCode", "Random code generated successfully");
            OSMLogger.logPerformance(this.getClass(), "generateRandomCode", startTime, System.currentTimeMillis());
            OSMLogger.logSecurityEvent(this.getClass(), "RANDOM_CODE_GENERATED",
                    "Random 6-digit code generated");

            return codeString;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error generating random code", e);
            throw e;
        }
    }

    private static final class SecureRandomHolder {
        private static final SecureRandom INSTANCE = new SecureRandom();
    }

    public List<OSMUserDTO> findByRoleName(String roleName) {

        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findByRoleName", "Finding users by role name: " + roleName);

        try {
            List<OSMUserDTO> users = userRepository.findByRoleRoleNameAndTenantIdAndIsDeletedFalse(roleName, TenantContext.getCurrentTenant()).stream().map(
                    user -> modelMapper.map(user, OSMUserDTO.class)
            ).toList();

            OSMLogger.logMethodExit(this.getClass(), "findByRoleName",
                    "Found " + users.size() + " users with role: " + roleName);
            OSMLogger.logPerformance(this.getClass(), "findByRoleName", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ_BY_ROLE", "OSMUser");

            return users;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(),
                    "Error finding users by role name: " + roleName, e);
            throw e;
        }
    }
    public List<OSMUserDTO> findByRole(String roleName) {
        UUID tenantId = TenantContext.getCurrentTenant();
        List<OSMUser> users = userRepository.findByRoleNameAndTenant(roleName, tenantId);
        return users.stream()
                .map(user -> modelMapper.map(user, OSMUserDTO.class))
                .toList();
    }

    public List<AssignableUserDTO> findAssignableUsersByPermissionIncludingAdmins(
            OSMModule module,
            String entity,
            String permissionName
    ) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return userRepository.findAssignableUsersByPermissionOrAdmin(
                        tenantId,
                        module,
                        entity,
                        permissionName
                ).stream()
                .map(user -> {
                    AssignableUserDTO dto = new AssignableUserDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());
                    dto.setRoleName(user.getRole() != null ? user.getRole().getRoleName() : null);
                    dto.setOneSignalPlayerId(user.getOneSignalPlayerId());

                    String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
                    String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
                    String fullName = (firstName + " " + lastName).trim();
                    dto.setDisplayName(!fullName.isEmpty() ? fullName : user.getUsername());
                    return dto;
                })
                .toList();
    }

    @Override
    public Set<Action> actionsMapping(OSMUser user) {
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        if (!user.getRole().getRoleName().equals("ADMIN")) {
            actions.addAll(Set.of(Action.UPDATE, Action.DELETE));
        }
        return actions;
    }


    @Transactional
    public void updateOneSignalPlayerId(String userIdOrUsername, String playerId) {
        // Chercher par UUID d'abord, puis par username en fallback
        OSMUser user = null;
        try {
            UUID uuid = UUID.fromString(userIdOrUsername);
            user = userRepository.findByIdAndIsDeletedFalse(uuid).orElse(null);
        } catch (IllegalArgumentException ignored) {}

        if (user == null) {
            user = userRepository.findByUsernameAndIsDeletedFalse(userIdOrUsername)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userIdOrUsername));
        }

        UUID currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Unauthorized"));
        if (!currentUserId.equals(user.getId())) {
            throw new AccessDeniedException("You can only register devices for your own account");
        }

        user.setOneSignalPlayerId(playerId);
        userRepository.save(user);
        OSMLogger.logSecurityEvent(this.getClass(), "PLAYER_ID_UPDATED",
                "OneSignal Player ID updated for user: " + userIdOrUsername);
    }

    private boolean isCurrentUser(UUID userId) {
        if (userId == null) {
            return false;
        }
        return SecurityUtils.getCurrentUserId().map(userId::equals).orElse(false);
    }

    private boolean hasUserManagementPermission(String action) {
        if (SecurityUtils.hasElevatedAdminAccess()) {
            return true;
        }
        String required = String.format("%s:%s:%s", USER_MGMT_MODULE, USER_MGMT_ENTITY, action).toUpperCase(Locale.ROOT);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(granted -> required.equalsIgnoreCase(granted.getAuthority()));
    }

    private void assertCanCreateUser() {
        if (!hasUserManagementPermission("CREATE")) {
            throw new AccessDeniedException("You are not allowed to create users");
        }
    }

    private void assertCanViewUser(OSMUser target) {
        if (!hasUserManagementPermission("READ")) {
            throw new AccessDeniedException("You are not allowed to view this user");
        }
        assertSameTenant(target);
    }

    private void assertCanManageUser(OSMUser target) {
        if (isCurrentUser(target.getId())) {
            throw new AccessDeniedException("Use your profile settings to update your own account");
        }
        if (!hasUserManagementPermission("UPDATE")) {
            throw new AccessDeniedException("You are not allowed to update users");
        }
        assertSameTenant(target);
    }

    private void assertSameTenant(OSMUser target) {
        if (SecurityUtils.isOsmAdmin()) {
            return;
        }
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null || target.getTenantId() == null || !tenantId.equals(target.getTenantId())) {
            throw new AccessDeniedException("User is not in your organization");
        }
    }

}
