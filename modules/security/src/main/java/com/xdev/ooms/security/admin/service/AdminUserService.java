package com.xdev.ooms.security.admin.service;

import com.xdev.ooms.security.confirmationcode.enums.ConfirmationMethod;
import com.xdev.ooms.security.role.entity.Role;
import com.xdev.ooms.security.role.repository.RoleRepository;
import com.xdev.ooms.security.user.dto.OOSMUserOUTDTO;
import com.xdev.ooms.security.user.entity.OOSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.security.user.service.UserService;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountLockedException;
import java.util.UUID;

@Service
public class AdminUserService {
    private static final String OSM_ADMIN_ROLE = "OOSMADMIN";

    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public AdminUserService(
            UserRepository userRepository,
            UserService userService,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            ModelMapper modelMapper
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public OOSMUserOUTDTO issueTemporaryPassword(UUID userId) throws Exception {
        OOSMLogger.logMethodEntry(this.getClass(), "issueTemporaryPassword", userId);

        OOSMUser user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isLocked()) {
            throw new AccountLockedException("Account is locked");
        }

        if ((user.getEmail() == null || user.getEmail().isBlank())
                && (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank())) {
            throw new IllegalArgumentException("User has no email or phone number on file");
        }

        String rawPassword = userService.generateSecureCode(8);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNewUser(true);

        OOSMUserOUTDTO userDTO = modelMapper.map(user, OOSMUserOUTDTO.class);
        if (userDTO.getConfirmationMethod() == null) {
            userDTO.setConfirmationMethod(
                    user.getEmail() != null && !user.getEmail().isBlank()
                            ? ConfirmationMethod.EMAIL
                            : ConfirmationMethod.PHONE
            );
        }

        userService.sendAdminPasswordReset(userDTO, rawPassword);
        OOSMUser savedUser = userRepository.save(user);

        OOSMLogger.logSecurityEvent(this.getClass(), "ADMIN_TEMP_PASSWORD_ISSUED",
                "Temporary password issued by admin for user: " + savedUser.getUsername());

        return modelMapper.map(savedUser, OOSMUserOUTDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public OOSMUserOUTDTO createOosmAdminUser(OOSMUserOUTDTO userDTO) throws Exception {
        OOSMLogger.logMethodEntry(this.getClass(), "createOosmAdminUser",
                userDTO != null ? userDTO.getUsername() : "null");

        validateOosmAdminUserDTO(userDTO);
        checkUserDoesNotExist(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPhoneNumber());

        Role oosmAdminRole = roleRepository.findByRoleName(OSM_ADMIN_ROLE)
                .orElseThrow(() -> new IllegalArgumentException("OOSMADMIN role not found"));

        String rawPassword = userService.generateSecureCode(8);
        String hashedPassword = passwordEncoder.encode(rawPassword);

        OOSMUser user = modelMapper.map(userDTO, OOSMUser.class);
        user.setRole(oosmAdminRole);
        user.setPassword(hashedPassword);
        user.setNewUser(true);
        user.setLocked(userDTO.isLocked());

        userService.sendWelcomeCredentials(userDTO, rawPassword);
        OOSMUser savedUser = userRepository.save(user);
        userRepository.clearTenantId(savedUser.getId());
        savedUser.setTenantId(null);

        OOSMLogger.logSecurityEvent(this.getClass(), "OSM_ADMIN_USER_CREATED",
                "New OOSM admin user created: " + savedUser.getUsername());

        return modelMapper.map(savedUser, OOSMUserOUTDTO.class);
    }

    private void validateOosmAdminUserDTO(OOSMUserOUTDTO userDTO) {
        if (userDTO == null) {
            throw new IllegalArgumentException("User data must not be null");
        }
        if (userDTO.getUsername() == null || userDTO.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (userDTO.getConfirmationMethod() == null) {
            throw new IllegalArgumentException("Confirmation method is required");
        }
        if (userDTO.getConfirmationMethod() == ConfirmationMethod.EMAIL
                && (userDTO.getEmail() == null || userDTO.getEmail().isBlank())) {
            throw new IllegalArgumentException("Email is required for email confirmation");
        }
        if (userDTO.getConfirmationMethod() == ConfirmationMethod.PHONE
                && (userDTO.getPhoneNumber() == null || userDTO.getPhoneNumber().isBlank())) {
            throw new IllegalArgumentException("Phone number is required for phone confirmation");
        }
    }

    private void checkUserDoesNotExist(String username, String email, String phoneNumber) {
        if (username != null && userRepository.findByUsernameAndIsDeletedFalse(username).isPresent()) {
            throw new IllegalArgumentException("Username is already in use");
        }
        if (email != null && userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(email).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }
        if (phoneNumber != null && userRepository.findByPhoneNumberAndIsDeletedFalse(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("Phone number is already in use");
        }
    }
}
