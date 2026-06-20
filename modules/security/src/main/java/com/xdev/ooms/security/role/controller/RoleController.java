package com.xdev.ooms.security.role.controller;


import com.xdev.ooms.security.role.dto.RoleDTO;
import com.xdev.ooms.security.role.entity.Role;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/security/role")
public class RoleController extends BaseControllerImpl<Role, RoleDTO, RoleDTO> {
    private final UserRepository userRepository;

    public RoleController(
            BaseService<Role, RoleDTO, RoleDTO> baseService,
            ModelMapper modelMapper,
            UserRepository userRepository) {
        super(baseService, modelMapper);
        this.userRepository = userRepository;
    }

    @GetMapping("/all-with-user-count")
    public ResponseEntity<?> getAllRolesWithUserCount() {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getAllRolesWithUserCount", "Retrieving all roles with user count");
        
        try {
            List<RoleDTO> roles = baseService.findAll();
            
            if (roles != null && !roles.isEmpty()) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.DEBUG, "Found {} roles, calculating user counts", roles.size());

                Map<String, Long> userCountsByRole = userRepository.countUsersGroupedByRole().stream()
                        .collect(Collectors.toMap(
                                row -> (String) row[0],
                                row -> (Long) row[1],
                                (left, right) -> left + right));
                
                List<RoleDTO> roleDTOs = roles.stream()
                        .filter(Objects::nonNull)
                        .peek(r -> {
                            long count = userCountsByRole.getOrDefault(r.getRoleName(), 0L);
                            r.setUsersCount((int) count);
                            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.DEBUG,
                                    "Role {} has {} users", r.getRoleName(), count);
                        })
                        .toList();
                
                OSMLogger.logMethodExit(this.getClass(), "getAllRolesWithUserCount", 
                    "Retrieved " + roleDTOs.size() + " roles with user counts");
                OSMLogger.logPerformance(this.getClass(), "getAllRolesWithUserCount", startTime, System.currentTimeMillis());
                OSMLogger.logSecurityEvent(this.getClass(), "ROLES_RETRIEVED_WITH_COUNTS", 
                    "All roles retrieved with user counts successfully");
                
                return ResponseEntity.ok(roleDTOs);
            }
            
            OSMLogger.logMethodExit(this.getClass(), "getAllRolesWithUserCount", "No roles found");
            OSMLogger.logPerformance(this.getClass(), "getAllRolesWithUserCount", startTime, System.currentTimeMillis());
            OSMLogger.logSecurityEvent(this.getClass(), "NO_ROLES_FOUND", 
                "No roles found in the system");
            
            return ResponseEntity.ok(roles);
            
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), 
                "Unexpected error while retrieving roles with user counts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Message error: " + e.getMessage());
        }
    }

    @Override
    protected String getResourceName() {
        return "Role".toUpperCase();
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
