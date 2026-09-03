package com.xdev.ooms.administration.settings;

import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingAuditDto;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingDto;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingsActor;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingsStatusDto;
import com.xdev.ooms.sharedkernel.settings.dto.MailTestRequest;
import com.xdev.ooms.sharedkernel.settings.dto.MailTestResponse;
import com.xdev.ooms.sharedkernel.settings.dto.NotificationTestRequest;
import com.xdev.ooms.sharedkernel.settings.dto.NotificationTestResponse;
import com.xdev.ooms.sharedkernel.settings.dto.RotateSecretRequest;
import com.xdev.ooms.sharedkernel.settings.dto.UpdateSettingRequest;
import com.xdev.ooms.sharedkernel.settings.service.AppSettingsService;
import com.xdev.ooms.sharedkernel.settings.service.AppSettingsServiceImpl.RestartRequiredException;
import com.xdev.ooms.sharedkernel.settings.service.AppSettingsServiceImpl.SettingNotFoundException;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import com.xdev.ooms.sharedkernel.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("authentication.tokenAttributes['role'] == 'OOSMADMIN' or hasAnyAuthority('OOSMADMIN', 'ROLE_OOSMADMIN')")
public class AdminSettingsController {

    private final AppSettingsService appSettingsService;
    private final AdminSettingsRateLimiter rateLimiter;

    public AdminSettingsController(AppSettingsService appSettingsService, AdminSettingsRateLimiter rateLimiter) {
        this.appSettingsService = appSettingsService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication authentication) {
        if (!isOosmAdmin(authentication)) {
            logForbidden("list settings");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        OOSMLogger.debug(this.getClass(), "Admin settings list requested by {}", actorName(authentication));
        return ResponseEntity.ok(appSettingsService.listForAdmin());
    }

    @GetMapping("/{key}")
    public ResponseEntity<?> get(@PathVariable String key, Authentication authentication) {
        if (!isOosmAdmin(authentication)) {
            logForbidden("get setting " + key);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        OOSMLogger.debug(this.getClass(), "Admin setting read requested: key={} user={}", key, actorName(authentication));
        try {
            return ResponseEntity.ok(appSettingsService.getForAdmin(key));
        } catch (SettingNotFoundException ex) {
            OOSMLogger.warn(this.getClass(), "Admin setting not found: key={} user={}", key, actorName(authentication));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PutMapping("/{key}")
    public ResponseEntity<?> update(
            @PathVariable String key,
            @RequestBody UpdateSettingRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        if (!isOosmAdmin(authentication)) {
            logForbidden("update setting " + key);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AdminSettingsActor actor = buildActor(authentication, httpRequest);
        OOSMLogger.info(this.getClass(),
                "Admin setting update requested: key={} user={} ip={}",
                key, actor.getUsername(), actor.getIpAddress());
        try {
            AdminSettingDto updated = appSettingsService.update(key, request, actor);
            OOSMLogger.info(this.getClass(),
                    "Admin setting updated: key={} user={} source={} configured={}",
                    key, actor.getUsername(), updated.getSource(), updated.isConfigured());
            return ResponseEntity.ok(updated);
        } catch (SettingNotFoundException ex) {
            OOSMLogger.warn(this.getClass(), "Admin setting update failed, not found: key={} user={}", key, actor.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (RestartRequiredException ex) {
            OOSMLogger.warn(this.getClass(), "Admin setting update requires restart: key={} user={}", key, actor.getUsername());
            Map<String, Object> body = new HashMap<>();
            body.put("message", ex.getMessage());
            body.put("restartRequired", true);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        } catch (SecurityException ex) {
            OOSMLogger.logSecurityEvent(this.getClass(), "Admin setting update denied",
                    "key=" + key + " user=" + actor.getUsername() + " reason=" + ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            OOSMLogger.warn(this.getClass(), "Admin setting update validation failed: key={} user={} error={}",
                    key, actor.getUsername(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/{key}/rotate-secret")
    public ResponseEntity<?> rotateSecret(
            @PathVariable String key,
            @RequestBody RotateSecretRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        if (!isOosmAdmin(authentication)) {
            logForbidden("rotate secret " + key);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AdminSettingsActor actor = buildActor(authentication, httpRequest);
        OOSMLogger.info(this.getClass(),
                "Admin secret rotation requested: key={} user={} ip={}",
                key, actor.getUsername(), actor.getIpAddress());
        try {
            rateLimiter.checkSecretRotation(rateLimitKey(authentication));
            AdminSettingDto updated = appSettingsService.rotateSecret(key, request, actor);
            rateLimiter.recordSecretRotation(rateLimitKey(authentication));
            OOSMLogger.logBusinessEvent(this.getClass(), "Admin secret rotated",
                    "key=" + key + " user=" + actor.getUsername() + " configured=" + updated.isConfigured());
            return ResponseEntity.ok(updated);
        } catch (SettingNotFoundException ex) {
            OOSMLogger.warn(this.getClass(), "Admin secret rotation failed, not found: key={} user={}", key, actor.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (AdminSettingsRateLimiter.RateLimitExceededException ex) {
            OOSMLogger.warn(this.getClass(), "Admin secret rotation rate limited: key={} user={}", key, actor.getUsername());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
        } catch (SecurityException ex) {
            OOSMLogger.logSecurityEvent(this.getClass(), "Admin secret rotation denied",
                    "key=" + key + " user=" + actor.getUsername() + " reason=" + ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            OOSMLogger.warn(this.getClass(), "Admin secret rotation validation failed: key={} user={} error={}",
                    key, actor.getUsername(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/reload")
    public ResponseEntity<?> reload(Authentication authentication) {
        if (!isOosmAdmin(authentication)) {
            logForbidden("reload settings");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        OOSMLogger.info(this.getClass(), "Admin settings reload requested by {}", actorName(authentication));
        appSettingsService.reload();
        AdminSettingsStatusDto status = appSettingsService.getStatus();
        OOSMLogger.info(this.getClass(), "Admin settings reloaded: version={} mailProvider={} mailEnabled={}",
                status.getSettingsVersion(),
                status.getFeatures() != null && status.getFeatures().get("mail") != null
                        ? status.getFeatures().get("mail").getProvider() : "unknown",
                status.getFeatures() != null && status.getFeatures().get("mail") != null
                        && status.getFeatures().get("mail").isEnabled());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/mail/test")
    public ResponseEntity<?> mailTest(
            @RequestBody MailTestRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        if (!isOosmAdmin(authentication)) {
            logForbidden("mail test");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AdminSettingsActor actor = buildActor(authentication, httpRequest);
        String recipient = request != null && request.getTo() != null ? request.getTo().trim() : "";
        OOSMLogger.info(this.getClass(),
                "Admin mail test requested: recipient={} user={} ip={}",
                recipient, actor.getUsername(), actor.getIpAddress());
        try {
            String userKey = rateLimitKey(authentication);
            rateLimiter.checkMailTest(userKey);
            MailTestResponse response = appSettingsService.sendMailTest(request, actor);
            rateLimiter.recordMailTest(userKey, response.isSuccess());
            if (response.isSuccess()) {
                OOSMLogger.info(this.getClass(),
                        "Admin mail test succeeded: recipient={} provider={} user={}",
                        recipient, response.getProvider(), actor.getUsername());
                return ResponseEntity.ok(response);
            }
            OOSMLogger.warn(this.getClass(),
                    "Admin mail test failed: recipient={} provider={} user={} error={}",
                    recipient, response.getProvider(), actor.getUsername(), response.getError());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
        } catch (AdminSettingsRateLimiter.RateLimitExceededException ex) {
            OOSMLogger.warn(this.getClass(), "Admin mail test rate limited: recipient={} user={}",
                    recipient, actor.getUsername());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            OOSMLogger.warn(this.getClass(), "Admin mail test validation failed: recipient={} user={} error={}",
                    recipient, actor.getUsername(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/notifications/test")
    public ResponseEntity<?> notificationTest(
            @RequestBody NotificationTestRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        if (!isOosmAdmin(authentication)) {
            logForbidden("notification test");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AdminSettingsActor actor = buildActor(authentication, httpRequest);
        String deviceToken = request != null ? request.resolveDeviceToken() : "";
        OOSMLogger.info(this.getClass(),
                "Admin notification test requested: tokenPresent={} user={} ip={}",
                StringUtils.hasText(deviceToken), actor.getUsername(), actor.getIpAddress());
        try {
            String userKey = rateLimitKey(authentication);
            rateLimiter.checkMailTest(userKey);
            NotificationTestResponse response = appSettingsService.sendNotificationTest(request, actor);
            rateLimiter.recordMailTest(userKey, response.isSuccess());
            if (response.isSuccess()) {
                OOSMLogger.info(this.getClass(),
                        "Admin notification test succeeded: user={}",
                        actor.getUsername());
                return ResponseEntity.ok(response);
            }
            OOSMLogger.warn(this.getClass(),
                    "Admin notification test failed: user={} error={}",
                    actor.getUsername(), response.getError());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
        } catch (AdminSettingsRateLimiter.RateLimitExceededException ex) {
            OOSMLogger.warn(this.getClass(), "Admin notification test rate limited: user={}",
                    actor.getUsername());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            OOSMLogger.warn(this.getClass(), "Admin notification test validation failed: user={} error={}",
                    actor.getUsername(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/audit")
    public ResponseEntity<Page<AdminSettingAuditDto>> audit(
            @RequestParam(required = false) String key,
            Pageable pageable,
            Authentication authentication
    ) {
        if (!isOosmAdmin(authentication)) {
            logForbidden("list settings audit");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        OOSMLogger.debug(this.getClass(), "Admin settings audit requested: key={} user={}", key, actorName(authentication));
        return ResponseEntity.ok(appSettingsService.listAudit(key, pageable));
    }

    @GetMapping("/status")
    public ResponseEntity<AdminSettingsStatusDto> status(Authentication authentication) {
        if (!isOosmAdmin(authentication)) {
            logForbidden("get settings status");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        OOSMLogger.debug(this.getClass(), "Admin settings status requested by {}", actorName(authentication));
        AdminSettingsStatusDto status = appSettingsService.getStatus();
        if (status.getFeatures() != null && status.getFeatures().get("mail") != null) {
            var mail = status.getFeatures().get("mail");
            OOSMLogger.debug(this.getClass(),
                    "Admin mail status: provider={} enabled={} configured={} missing={}",
                    mail.getProvider(), mail.isEnabled(), mail.isConfigured(), mail.getMissingKeys());
        }
        return ResponseEntity.ok(status);
    }

    private void logForbidden(String action) {
        OOSMLogger.logSecurityEvent(this.getClass(), "Admin settings access denied", "action=" + action);
    }

    private static String actorName(Authentication authentication) {
        return SecurityUtils.getCurrentUsername()
                .orElse(authentication != null ? authentication.getName() : "anonymous");
    }

    private AdminSettingsActor buildActor(Authentication authentication, HttpServletRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId().orElse(null);
        String username = SecurityUtils.getCurrentUsername().orElse(authentication != null ? authentication.getName() : null);
        String ip = request != null ? request.getRemoteAddr() : null;
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        return new AdminSettingsActor(userId, username, ip, userAgent);
    }

    private String rateLimitKey(Authentication authentication) {
        return SecurityUtils.getCurrentUserId()
                .map(UUID::toString)
                .orElseGet(() -> authentication != null ? authentication.getName() : "anonymous");
    }

    private boolean isOosmAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Object role = jwtAuth.getToken().getClaims().get("role");
            if (role != null && "OOSMADMIN".equalsIgnoreCase(role.toString())) {
                return true;
            }
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority ->
                        "OOSMADMIN".equalsIgnoreCase(authority)
                                || "ROLE_OOSMADMIN".equalsIgnoreCase(authority)
                );
    }
}
