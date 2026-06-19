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
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService extends BaseServiceImpl<OSMUser, OSMUserDTO, OSMUserOUTDTO> implements UserDetailsService {
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
        SearchResponse<OSMUser, OSMUserOUTDTO> response = super.search(searchData);
        enrichTenantNames(response.getData());
        return response;
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
            validateUserDTO(userDTO);
            checkExistUser(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPhoneNumber());

            String rawPassword = generateSecureCode(8);
            String hashedPassword = passwordEncoder.encode(rawPassword);

            OSMUser user = modelMapper.map(userDTO, OSMUser.class);
            user.setPassword(hashedPassword);
            user.setNewUser(true);

            sendConfirmation(userDTO, rawPassword);
            OSMUser savedUser = userRepository.save(user);

            OSMLogger.logMethodExit(this.getClass(), "addUser", "User added successfully: " + username);
            OSMLogger.logPerformance(this.getClass(), "addUser", startTime, System.currentTimeMillis());
            OSMLogger.logSecurityEvent(this.getClass(), "USER_ADDED",
                    "New user added successfully: " + username);

            return modelMapper.map(savedUser, OSMUserOUTDTO.class);

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
                sendConfirmation(userDTO, rawPassword);
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

    public void sendWelcomeCredentials(OSMUserOUTDTO userDTO, String rawPassword) throws Exception {
        sendConfirmation(userDTO, rawPassword);
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

        user.setOneSignalPlayerId(playerId);
        userRepository.save(user);
        OSMLogger.logSecurityEvent(this.getClass(), "PLAYER_ID_UPDATED",
                "OneSignal Player ID updated for user: " + userIdOrUsername);
    }

}
