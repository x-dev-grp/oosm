package com.xdev.ooms.security;

import com.xdev.ooms.security.userManagement.data.RoleRepository;
import com.xdev.ooms.security.userManagement.data.UserRepository;
import com.xdev.ooms.security.userManagement.dtos.OUTDTO.OSMUserDTO;
import com.xdev.ooms.security.userManagement.dtos.OUTDTO.RoleDTO;
import com.xdev.ooms.security.userManagement.models.OSMUser;
import com.xdev.ooms.security.userManagement.models.Role;
import com.xdev.ooms.security.userManagement.service.RoleService;
import com.xdev.ooms.security.userManagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class SecurityBootstrap {

    private static final Logger log = LoggerFactory.getLogger(SecurityBootstrap.class);
    public static final String RAW_PASSWORD1 = "osmAdmin123";
    public static final String OSM_ADMIN = "osmAdmin";
    public static final String MAIL1 = "osmAdmin@example.com";
    public static final String NUMBER1 = "1234567819";
    public static final String OSMADMIN = "OSMADMIN";

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public SecurityBootstrap(UserService userService,
                             RoleService roleService,
                             PasswordEncoder passwordEncoder,
                             RoleRepository roleRepository,
                             UserRepository userRepository) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
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
        Optional<OSMUser> existing = userRepository.findByUsername(OSM_ADMIN);
        if (existing.isPresent()) {
            log.debug("User '{}' already exists (id={})", OSM_ADMIN, existing.get().getId());
            return;
        }

        log.info("Creating user '{}'", OSM_ADMIN);
        OSMUserDTO dto = new OSMUserDTO();
        dto.setUsername(OSM_ADMIN);
        dto.setPassword(passwordEncoder.encode(RAW_PASSWORD1));
        dto.setEmail(MAIL1);
        dto.setPhoneNumber(NUMBER1);
        dto.setRole(role);
        dto.setLocked(false);
        userService.save(dto);
    }
}
