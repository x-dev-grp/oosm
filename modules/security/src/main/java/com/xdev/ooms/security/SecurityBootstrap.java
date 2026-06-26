package com.xdev.ooms.security;

import com.xdev.ooms.security.role.dto.RoleDTO;
import com.xdev.ooms.security.role.entity.Role;
import com.xdev.ooms.security.role.repository.RoleRepository;
import com.xdev.ooms.security.role.service.RoleService;
import com.xdev.ooms.security.user.dto.OOSMUserDTO;
import com.xdev.ooms.security.user.entity.OOSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.security.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Configuration
@ConditionalOnProperty(name = "app.security.bootstrap.enabled", havingValue = "true")
public class SecurityBootstrap {

    private static final Logger log = LoggerFactory.getLogger(SecurityBootstrap.class);
    public static final String OOSMADMIN = "OOSMADMIN";

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminEmail;
    private final String adminPhone;
    private final boolean resetExistingAdminPassword;

    public SecurityBootstrap(UserService userService,
                             RoleService roleService,
                             PasswordEncoder passwordEncoder,
                             RoleRepository roleRepository,
                             UserRepository userRepository,
                             @Value("${app.security.bootstrap.username}") String adminUsername,
                             @Value("${app.security.bootstrap.password}") String adminPassword,
                             @Value("${app.security.bootstrap.email}") String adminEmail,
                             @Value("${app.security.bootstrap.phone}") String adminPhone,
                             @Value("${app.security.bootstrap.reset-existing-admin-password:true}") boolean resetExistingAdminPassword) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminEmail = adminEmail;
        this.adminPhone = adminPhone;
        this.resetExistingAdminPassword = resetExistingAdminPassword;
    }

    @Bean
    public CommandLineRunner initAdminUser() {
        return args -> {
            log.info("Bootstrapping default security data...");
            RoleDTO oosmAdminRole = ensureRole();
            ensureUser(oosmAdminRole);
            log.info("Security bootstrap complete.");
        };
    }

    private RoleDTO ensureRole() {
        roleRepository.findByRoleName("OSMADMIN").ifPresent(legacy -> {
            legacy.setRoleName(OOSMADMIN);
            roleRepository.save(legacy);
            log.info("Migrated legacy role OSMADMIN to {}", OOSMADMIN);
        });

        Optional<Role> existing = roleRepository.findByRoleName(OOSMADMIN);
        if (existing.isPresent()) {
            Role role = existing.get();
            log.debug("Role '{}' already exists (id={})", role.getRoleName(), role.getId());
            RoleDTO dto = new RoleDTO();
            dto.setId(role.getId());
            dto.setRoleName(role.getRoleName());
            return dto;
        }

        log.info("Creating role '{}'", OOSMADMIN);
        RoleDTO dto = new RoleDTO();
        dto.setRoleName(OOSMADMIN);
        return roleService.save(dto);
    }

    private void ensureUser(RoleDTO role) {
        if (!StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException(
                    "SECURITY_BOOTSTRAP_PASSWORD is required when SECURITY_BOOTSTRAP_ENABLED=true");
        }

        Optional<OOSMUser> existing = userRepository.findByUsername(adminUsername);
        if (existing.isPresent()) {
            OOSMUser user = existing.get();
            log.info("User '{}' already exists (id={})", adminUsername, user.getId());
            if (resetExistingAdminPassword) {
                user.setPassword(passwordEncoder.encode(adminPassword));
                user.setLocked(false);
                user.setEnabled(true);
                user.setNewUser(false);
                if (user.getRole() == null) {
                    user.setRole(roleRepository.findByRoleName(OOSMADMIN).orElseThrow());
                }
                userRepository.save(user);
                log.info("Reset bootstrap password for user '{}'", adminUsername);
            }
            return;
        }

        log.info("Creating user '{}'", adminUsername);
        OOSMUserDTO dto = new OOSMUserDTO();
        dto.setUsername(adminUsername);
        dto.setPassword(passwordEncoder.encode(adminPassword));
        dto.setEmail(adminEmail);
        dto.setPhoneNumber(adminPhone);
        dto.setRole(role);
        dto.setLocked(false);
        userService.save(dto);

        userRepository.findByUsername(adminUsername).ifPresent(user -> {
            user.setEnabled(true);
            user.setNewUser(false);
            userRepository.save(user);
        });
    }
}
