package com.xdev.ooms.security.user.controller;

import com.xdev.ooms.security.user.dto.UserProfileUpdateDTO;
import com.xdev.ooms.security.user.dto.UserPhotoDTO;
import com.xdev.ooms.security.user.dto.AssignableUserDTO;
import com.xdev.ooms.security.user.dto.OOSMUserDTO;
import com.xdev.ooms.security.user.dto.OOSMUserOUTDTO;
import com.xdev.ooms.security.user.dto.SessionRefreshResponse;
import com.xdev.ooms.security.user.dto.UpdatePasswordDTO;
import com.xdev.ooms.security.user.entity.OOSMUser;
import com.xdev.ooms.security.user.service.UserService;
import com.xdev.ooms.security.user.service.UserSessionService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
import com.xdev.ooms.sharedkernel.services.BaseService;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import com.xdev.ooms.sharedkernel.utils.SecurityUtils;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/security/user")
public class UserController extends BaseControllerImpl<OOSMUser, OOSMUserDTO, OOSMUserOUTDTO> {
    private final UserService userService;
    private final UserSessionService userSessionService;

    public UserController(BaseService<OOSMUser, OOSMUserDTO, OOSMUserOUTDTO> baseService,
                          ModelMapper modelMapper,
                          UserService userService,
                          UserSessionService userSessionService) {
        super(baseService, modelMapper);
        this.userService = userService;
        this.userSessionService = userSessionService;
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout() {
        // Idempotent public endpoint: clients may call this while clearing a dead session.
        // Revocation of refresh tokens is optional and best-effort elsewhere.
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestParam String identifier) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "resetPassword", "Password reset request for identifier: " + identifier);
        
        try {
            OOSMUserOUTDTO user = userService.resetPassword(identifier);
            
            OOSMLogger.logMethodExit(this.getClass(), "resetPassword", "Password reset successful for identifier: " + identifier);
            OOSMLogger.logPerformance(this.getClass(), "resetPassword", startTime, System.currentTimeMillis());
            OOSMLogger.logSecurityEvent(this.getClass(), "PASSWORD_RESET_REQUESTED", 
                "Password reset requested successfully for identifier: " + identifier);
            
            return ResponseEntity.ok(user);
            
        } catch (AccountLockedException e) {
            OOSMLogger.logSecurityEvent(this.getClass(), "PASSWORD_RESET_ACCOUNT_LOCKED", 
                "Password reset failed - Account locked for identifier: " + identifier);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is locked");
            
        } catch (IllegalArgumentException e) {
            OOSMLogger.logSecurityEvent(this.getClass(), "PASSWORD_RESET_INVALID_INPUT", 
                "Password reset failed - Invalid input for identifier: " + identifier + ", Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input");
            
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), 
                "Unexpected error during password reset for identifier: " + identifier, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to process password reset");
        }
    }

    @PostMapping("/auth/validateResetCode/{userId}")
    public ResponseEntity<?> validateResetCode(@RequestParam String code, @PathVariable UUID userId) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "validateResetCode",
            "Validating reset code for user: " + userId + ", Code: " + (code != null ? code.substring(0, Math.min(3, code.length())) + "..." : "null"));

        try {
            userService.validateResetCode(code, userId);

            OOSMLogger.logMethodExit(this.getClass(), "validateResetCode", "Reset code validated successfully for user: " + userId);
            OOSMLogger.logPerformance(this.getClass(), "validateResetCode", startTime, System.currentTimeMillis());
            OOSMLogger.logSecurityEvent(this.getClass(), "RESET_CODE_VALIDATED",
                "Reset code validated successfully for user: " + userId);

            return ResponseEntity.ok().build();

        } catch (CredentialExpiredException e) {
            OOSMLogger.logSecurityEvent(this.getClass(), "RESET_CODE_EXPIRED",
                "Reset code validation failed - Code expired for user: " + userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Code is expired");

        } catch (IllegalArgumentException e) {
            OOSMLogger.logSecurityEvent(this.getClass(), "RESET_CODE_INVALID",
                "Reset code validation failed - Invalid input for user: " + userId + ", Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input");

        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(),
                "Unexpected error during reset code validation for user: " + userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to validate reset code");
        }
    }

    @PostMapping("/auth/updatePassword/{userId}")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto, @PathVariable UUID userId) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "updatePassword", "Updating password for user: " + userId);

        try {
            userService.updatePassword(dto, userId);

            OOSMLogger.logMethodExit(this.getClass(), "updatePassword", "Password updated successfully for user: " + userId);
            OOSMLogger.logPerformance(this.getClass(), "updatePassword", startTime, System.currentTimeMillis());
            OOSMLogger.logSecurityEvent(this.getClass(), "PASSWORD_UPDATED",
                "Password updated successfully for user: " + userId);

            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            OOSMLogger.logSecurityEvent(this.getClass(), "PASSWORD_UPDATE_INVALID",
                "Password update failed - Invalid input for user: " + userId + ", Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired reset request");

        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(),
                "Unexpected error during password update for user: " + userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to update password");
        }
    }

    @PostMapping("/addUser")
    public ResponseEntity<?> addUser(@RequestBody OOSMUserOUTDTO dto) {
        long startTime = System.currentTimeMillis();
        String username = dto != null ? dto.getUsername() : "null";
        OOSMLogger.logMethodEntry(this.getClass(), "addUser", "Adding new user: " + username);

        try {
            OOSMUserOUTDTO user = userService.addUser(dto);

            OOSMLogger.logMethodExit(this.getClass(), "addUser", "User added successfully: " + username);
            OOSMLogger.logPerformance(this.getClass(), "addUser", startTime, System.currentTimeMillis());
            OOSMLogger.logSecurityEvent(this.getClass(), "USER_ADDED",
                "New user added successfully: " + username);

            return ResponseEntity.ok(user);

        } catch (IllegalArgumentException e) {
            OOSMLogger.logSecurityEvent(this.getClass(), "USER_ADD_INVALID",
                "User addition failed - Invalid input for username: " + username + ", Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(),
                "Unexpected error during user addition for username: " + username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to add user");
        }
    }

    @PostMapping("/updateUser/{id}")
    public ResponseEntity<?> updateUser(@RequestBody OOSMUserOUTDTO dto, @PathVariable UUID id) {
        long startTime = System.currentTimeMillis();
        String username = dto != null ? dto.getUsername() : "null";
        OOSMLogger.logMethodEntry(this.getClass(), "updateUser", "Updating user: " + username + " with ID: " + id);

        try {
            OOSMUserOUTDTO user = userService.updateUser(dto, id);

            OOSMLogger.logMethodExit(this.getClass(), "updateUser", "User updated successfully: " + username + " with ID: " + id);
            OOSMLogger.logPerformance(this.getClass(), "updateUser", startTime, System.currentTimeMillis());
            OOSMLogger.logSecurityEvent(this.getClass(), "USER_UPDATED",
                "User updated successfully: " + username + " with ID: " + id);

            return ResponseEntity.ok(user);

        } catch (IllegalArgumentException e) {
            OOSMLogger.logSecurityEvent(this.getClass(), "USER_UPDATE_INVALID",
                "User update failed - Invalid input for user: " + username + " with ID: " + id + ", Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(),
                "Unexpected error during user update for username: " + username + " with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to update user");
        }
    }

    @Override
    protected String getResourceName() {
        return "USER".toUpperCase();
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }

    @GetMapping("/role/{roleName}")
    public ResponseEntity<List<OOSMUserDTO>> getUsersByRole(@PathVariable String roleName) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "getUsersByRole", "Fetching users for role: " + roleName);

        try {
            List<OOSMUserDTO> users = userService.findByRole(roleName);

            OOSMLogger.logMethodExit(this.getClass(), "getUsersByRole",
                    "Found " + users.size() + " users");
            OOSMLogger.logPerformance(this.getClass(), "getUsersByRole",
                    startTime, System.currentTimeMillis());

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(),
                    "Error fetching users by role: " + roleName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/register-device")
    public ResponseEntity<?> registerDevice(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            token = body.get("playerId"); // legacy alias
        }

        if (userId == null || token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("userId and token are required");
        }
        try {
            userService.updateFcmToken(userId, token.trim());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to register device");
        }
    }

    @PostMapping("/auth/initial-password/{userId}")
    public ResponseEntity<?> updateInitialPassword(@Valid @RequestBody UpdatePasswordDTO dto,
                                                   @PathVariable UUID userId) {
        try {
            userService.updateInitialPassword(dto, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(),
                    "Unexpected error during initial password update for user: " + userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to update password");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            String username = SecurityUtils.getCurrentUsername()
                    .orElseThrow(() -> new IllegalArgumentException("Unauthorized"));
            OOSMUserOUTDTO profile = userService.getCurrentUserProfile(username);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error fetching current user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to fetch profile");
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUserProfile(@RequestBody UserProfileUpdateDTO dto) {
        long startTime = System.currentTimeMillis();
        try {
            String username = SecurityUtils.getCurrentUsername()
                    .orElseThrow(() -> new IllegalArgumentException("Unauthorized"));
            OOSMUserOUTDTO updated = userService.updateCurrentUserProfile(username, dto);

            OOSMLogger.logMethodExit(this.getClass(), "updateCurrentUserProfile",
                    "Profile updated for user: " + username);
            OOSMLogger.logPerformance(this.getClass(), "updateCurrentUserProfile", startTime, System.currentTimeMillis());
            OOSMLogger.logSecurityEvent(this.getClass(), "USER_PROFILE_UPDATED",
                    "Current user updated their profile: " + username);

            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error updating current user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to update profile");
        }
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<?> changeOwnPassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        long startTime = System.currentTimeMillis();
        try {
            String username = SecurityUtils.getCurrentUsername()
                    .orElseThrow(() -> new IllegalArgumentException("Unauthorized"));
            userService.changeOwnPassword(username, dto);

            OOSMLogger.logMethodExit(this.getClass(), "changeOwnPassword",
                    "Password changed for user: " + username);
            OOSMLogger.logPerformance(this.getClass(), "changeOwnPassword", startTime, System.currentTimeMillis());
            OOSMLogger.logSecurityEvent(this.getClass(), "USER_PASSWORD_CHANGED",
                    "Current user changed their password: " + username);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error changing current user password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to change password");
        }
    }

    @GetMapping("/me/photo")
    public ResponseEntity<?> getCurrentUserPhoto() {
        try {
            String username = SecurityUtils.getCurrentUsername()
                    .orElseThrow(() -> new IllegalArgumentException("Unauthorized"));
            return ResponseEntity.ok(userService.getCurrentUserPhoto(username));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error fetching current user photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to fetch photo");
        }
    }

    @PostMapping("/me/photo")
    public ResponseEntity<?> updateCurrentUserPhoto(@RequestBody UserPhotoDTO dto) {
        long startTime = System.currentTimeMillis();
        try {
            String username = SecurityUtils.getCurrentUsername()
                    .orElseThrow(() -> new IllegalArgumentException("Unauthorized"));
            UserPhotoDTO updated = userService.updateCurrentUserPhoto(username, dto);

            OOSMLogger.logMethodExit(this.getClass(), "updateCurrentUserPhoto",
                    "Photo updated for user: " + username);
            OOSMLogger.logPerformance(this.getClass(), "updateCurrentUserPhoto", startTime, System.currentTimeMillis());
            OOSMLogger.logSecurityEvent(this.getClass(), "USER_PHOTO_UPDATED",
                    "Current user updated their photo: " + username);

            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error updating current user photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to update photo");
        }
    }

    @DeleteMapping("/me/photo")
    public ResponseEntity<?> removeCurrentUserPhoto() {
        long startTime = System.currentTimeMillis();
        try {
            String username = SecurityUtils.getCurrentUsername()
                    .orElseThrow(() -> new IllegalArgumentException("Unauthorized"));
            userService.removeCurrentUserPhoto(username);

            OOSMLogger.logMethodExit(this.getClass(), "removeCurrentUserPhoto",
                    "Photo removed for user: " + username);
            OOSMLogger.logPerformance(this.getClass(), "removeCurrentUserPhoto", startTime, System.currentTimeMillis());
            OOSMLogger.logSecurityEvent(this.getClass(), "USER_PHOTO_REMOVED",
                    "Current user removed their photo: " + username);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error removing current user photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to remove photo");
        }
    }

    @PostMapping("/me/refresh-session")
    public ResponseEntity<?> refreshSession(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "refreshSession", "Refreshing current user session");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String accessToken = authHeader.substring(7);
            SessionRefreshResponse response = userSessionService.refreshSession(accessToken);

            OOSMLogger.logMethodExit(this.getClass(), "refreshSession", "Session refreshed successfully");
            OOSMLogger.logPerformance(this.getClass(), "refreshSession", startTime, System.currentTimeMillis());
            OOSMLogger.logSecurityEvent(this.getClass(), "SESSION_PERMISSIONS_REFRESHED",
                    "Current user session refreshed with updated permissions");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error refreshing user session", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/assignable")
    public ResponseEntity<?> getAssignableUsersByPermission(@RequestParam String module,
                                                            @RequestParam String entity,
                                                            @RequestParam String permission) {
        try {
            OOSMModule moduleEnum = OOSMModule.valueOf(module.toUpperCase(Locale.ROOT));
            List<AssignableUserDTO> users =
                    userService.findAssignableUsersByPermissionIncludingAdmins(moduleEnum, entity, permission);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid module or permission parameters");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch assignable users");
        }
    }
    }


