package com.xdev.ooms.security.admin.service;

import com.xdev.ooms.security.confirmationcode.enums.ConfirmationMethod;
import com.xdev.ooms.security.role.entity.Role;
import com.xdev.ooms.security.role.repository.RoleRepository;
import com.xdev.ooms.security.user.dto.OSMUserOUTDTO;
import com.xdev.ooms.security.user.entity.OSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.security.user.service.UserService;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountLockedException;
import java.util.UUID;

@Service
public class AdminUserService {
    private static final String OSM_ADMIN_ROLE = "OSMADMIN";

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
    public OSMUserOUTDTO issueTemporaryPassword(UUID userId) throws Exception {
        OSMLogger.logMethodEntry(this.getClass(), "issueTemporaryPassword", userId);

        OSMUser user = userRepository.findByIdAndIsDeletedFalse(userId)
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

        OSMUserOUTDTO userDTO = modelMapper.map(user, OSMUserOUTDTO.class);
        if (userDTO.getConfirmationMethod() == null) {
            userDTO.setConfirmationMethod(
                    user.getEmail() != null && !user.getEmail().isBlank()
                            ? ConfirmationMethod.EMAIL
                            : ConfirmationMethod.PHONE
            );
        }

        userService.sendWelcomeCredentials(userDTO, rawPassword);
        OSMUser savedUser = userRepository.save(user);

        OSMLogger.logSecurityEvent(this.getClass(), "ADMIN_TEMP_PASSWORD_ISSUED",
                "Temporary password issued by admin for user: " + savedUser.getUsername());

        return modelMapper.map(savedUser, OSMUserOUTDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public OSMUserOUTDTO createOsmAdminUser(OSMUserOUTDTO userDTO) throws Exception {
        OSMLogger.logMethodEntry(this.getClass(), "createOsmAdminUser",
                userDTO != null ? userDTO.getUsername() : "null");

        validateOsmAdminUserDTO(userDTO);
        checkUserDoesNotExist(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPhoneNumber());

        Role osmAdminRole = roleRepository.findByRoleName(OSM_ADMIN_ROLE)
                .orElseThrow(() -> new IllegalArgumentException("OSMADMIN role not found"));

        String rawPassword = userService.generateSecureCode(8);
        String hashedPassword = passwordEncoder.encode(rawPassword);

        OSMUser user = modelMapper.map(userDTO, OSMUser.class);
        user.setRole(osmAdminRole);
        user.setPassword(hashedPassword);
        user.setNewUser(true);
        user.setLocked(userDTO.isLocked());

        userService.sendWelcomeCredentials(userDTO, rawPassword);
        OSMUser savedUser = userRepository.save(user);
        userRepository.clearTenantId(savedUser.getId());
        savedUser.setTenantId(null);

        OSMLogger.logSecurityEvent(this.getClass(), "OSM_ADMIN_USER_CREATED",
                "New OSM admin user created: " + savedUser.getUsername());

        return modelMapper.map(savedUser, OSMUserOUTDTO.class);
    }

    private void validateOsmAdminUserDTO(OSMUserOUTDTO userDTO) {
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
