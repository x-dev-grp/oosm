package com.xdev.ooms.security;

import com.xdev.ooms.security.role.dto.RoleDTO;
import com.xdev.ooms.security.role.entity.Role;
import com.xdev.ooms.security.role.repository.RoleRepository;
import com.xdev.ooms.security.role.service.RoleService;
import com.xdev.ooms.security.user.dto.OSMUserDTO;
import com.xdev.ooms.security.user.entity.OSMUser;
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
    public static final String OSMADMIN = "OSMADMIN";

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminEmail;
    private final String adminPhone;

    public SecurityBootstrap(UserService userService,
                             RoleService roleService,
                             PasswordEncoder passwordEncoder,
                             RoleRepository roleRepository,
                             UserRepository userRepository,
                             @Value("${app.security.bootstrap.username}") String adminUsername,
                             @Value("${app.security.bootstrap.password}") String adminPassword,
                             @Value("${app.security.bootstrap.email}") String adminEmail,
                             @Value("${app.security.bootstrap.phone}") String adminPhone) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminEmail = adminEmail;
        this.adminPhone = adminPhone;
    }

    @Bean
    public CommandLineRunner initAdminUser() {
        return args -> {
            log.info("Bootstrapping default security data...");
            RoleDTO osmAdminRole = ensureRole();
            ensureUser(osmAdminRole);
            log.info("Security bootstrap complete.");
        };
    }

    private RoleDTO ensureRole() {
        Optional<Role> existing = roleRepository.findByRoleName(OSMADMIN);
        if (existing.isPresent()) {
            Role role = existing.get();
            log.debug("Role '{}' already exists (id={})", role.getRoleName(), role.getId());
            RoleDTO dto = new RoleDTO();
            dto.setId(role.getId());
            dto.setRoleName(role.getRoleName());
            return dto;
        }

        log.info("Creating role '{}'", OSMADMIN);
        RoleDTO dto = new RoleDTO();
        dto.setRoleName(OSMADMIN);
        return roleService.save(dto);
    }

    private void ensureUser(RoleDTO role) {
        if (!StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException(
                    "SECURITY_BOOTSTRAP_PASSWORD is required when SECURITY_BOOTSTRAP_ENABLED=true");
        }

        Optional<OSMUser> existing = userRepository.findByUsername(adminUsername);
        if (existing.isPresent()) {
            log.debug("User '{}' already exists (id={})", adminUsername, existing.get().getId());
            return;
        }

        log.info("Creating user '{}'", adminUsername);
        OSMUserDTO dto = new OSMUserDTO();
        dto.setUsername(adminUsername);
        dto.setPassword(passwordEncoder.encode(adminPassword));
        dto.setEmail(adminEmail);
        dto.setPhoneNumber(adminPhone);
        dto.setRole(role);
        dto.setLocked(false);
        userService.save(dto);
    }
}
