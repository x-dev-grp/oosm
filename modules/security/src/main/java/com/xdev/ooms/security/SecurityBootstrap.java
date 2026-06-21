package com.xdev.ooms.security;

import com.xdev.ooms.sharedkernel.utils.OSMLogger;

import com.xdev.ooms.security.role.entity.Role;
import com.xdev.ooms.security.role.repository.RoleRepository;
import com.xdev.ooms.security.user.entity.OSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
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
    public static final String OSMADMIN = "OSMADMIN";

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminEmail;
    private final String adminPhone;
    private final boolean resetExistingAdminPassword;

    public SecurityBootstrap(PasswordEncoder passwordEncoder,
                             RoleRepository roleRepository,
                             UserRepository userRepository,
                             @Value("${app.security.bootstrap.username}") String adminUsername,
                             @Value("${app.security.bootstrap.password}") String adminPassword,
                             @Value("${app.security.bootstrap.email}") String adminEmail,
                             @Value("${app.security.bootstrap.phone}") String adminPhone,
                             @Value("${app.security.bootstrap.reset-existing-admin-password}") boolean resetExistingAdminPassword) {
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
            OSMLogger.info(SecurityBootstrap.class, "Bootstrapping default security data...");
            validateConfiguration();
            Role osmAdminRole = ensureRole();

          ensureUser(osmAdminRole);
            OSMLogger.info(SecurityBootstrap.class, "Security bootstrap complete.");
        };
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException(
                    "SECURITY_BOOTSTRAP_PASSWORD is required when SECURITY_BOOTSTRAP_ENABLED=true");
        }
    }

    private Role ensureRole() {
        Optional<Role> existing = roleRepository.findByRoleName(OSMADMIN);
        if (existing.isPresent()) {
            Role role = existing.get();
            OSMLogger.debug(SecurityBootstrap.class, "Role '{}' already exists (id={})", role.getRoleName(), role.getId());
            return role;
        }

        OSMLogger.info(SecurityBootstrap.class, "Creating role '{}'", OSMADMIN);
        Role role = new Role();
        role.setRoleName(OSMADMIN);
        return roleRepository.saveAndFlush(role);
    }

    private void ensureUser(Role role) {
        Optional<OSMUser> existing = userRepository.findByUsername(adminUsername);
        if (existing.isPresent()) {
            OSMUser user = existing.get();
            OSMLogger.info(SecurityBootstrap.class, "User '{}' already exists (id={})", adminUsername, user.getId());
            if (resetExistingAdminPassword) {
                user.setPassword(passwordEncoder.encode(adminPassword));
                OSMLogger.info(SecurityBootstrap.class, "Reset bootstrap password for user '{}'", adminUsername);
            }
            user.setLocked(false);
            user.setEnabled(true);
            user.setNewUser(false);
            user.setRole(role);
            userRepository.save(user);
            return;
        }

        OSMLogger.info(SecurityBootstrap.class, "Creating user '{}'", adminUsername);
        OSMUser user = new OSMUser();
        user.setUsername(adminUsername);
        user.setPassword(passwordEncoder.encode(adminPassword));
        user.setEmail(adminEmail);
        user.setPhoneNumber(adminPhone);
        user.setRole(role);
        user.setLocked(false);
        user.setEnabled(true);
        user.setNewUser(false);
        userRepository.save(user);
    }
}
