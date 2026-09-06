package com.xdev.ooms.production.dayimport.controller;

import com.xdev.ooms.production.dayimport.service.GoogleDriveOAuthService;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public Google OAuth callback (no JWT). State carries signed tenant id.
 */
@RestController
@RequestMapping("/api/public/gdrive/oauth")
public class PublicGoogleDriveOAuthController {

    private final GoogleDriveOAuthService oauthService;

    public PublicGoogleDriveOAuthController(GoogleDriveOAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription) {
        OOSMLogger.logMethodEntry(getClass(), "callback",
                error != null ? "error=" + error : "codePresent=" + (code != null));
        try {
            if (error != null && !error.isBlank()) {
                OOSMLogger.warn(getClass(), "[callback] Google returned error={} desc={}", error, errorDescription);
                String redirect = oauthService.buildFrontendRedirect(false, null,
                        errorDescription != null ? errorDescription : error);
                return redirect(redirect);
            }
            if (code == null || state == null) {
                OOSMLogger.warn(getClass(), "[callback] missing OAuth code/state");
                return redirect(oauthService.buildFrontendRedirect(false, null, "Missing OAuth code/state"));
            }
            String redirect = oauthService.handleCallback(code, state);
            OOSMLogger.logBusinessEvent(getClass(), "GDRIVE_OAUTH_CALLBACK_OK", "redirect issued");
            return redirect(redirect);
        } catch (Exception e) {
            OOSMLogger.logException(getClass(), "OAuth callback failed", e);
            return redirect(oauthService.buildFrontendRedirect(false, null, e.getMessage()));
        }
    }

    private ResponseEntity<Void> redirect(String location) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, location)
                .build();
    }
}
