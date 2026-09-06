package com.xdev.ooms.production.dayimport.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "tenant_google_drive_credential")
public class TenantGoogleDriveCredential extends BaseEntity {

    @Column(name = "google_account_email", length = 320)
    private String googleAccountEmail;

    @Column(name = "encrypted_refresh_token", nullable = false, length = 4000)
    private String encryptedRefreshToken;

    @Column(name = "scope", length = 1000)
    private String scope;

    @Column(name = "connected_at")
    private Instant connectedAt;

    @Column(name = "token_updated_at")
    private Instant tokenUpdatedAt;

    public String getGoogleAccountEmail() {
        return googleAccountEmail;
    }

    public void setGoogleAccountEmail(String googleAccountEmail) {
        this.googleAccountEmail = googleAccountEmail;
    }

    public String getEncryptedRefreshToken() {
        return encryptedRefreshToken;
    }

    public void setEncryptedRefreshToken(String encryptedRefreshToken) {
        this.encryptedRefreshToken = encryptedRefreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Instant connectedAt) {
        this.connectedAt = connectedAt;
    }

    public Instant getTokenUpdatedAt() {
        return tokenUpdatedAt;
    }

    public void setTokenUpdatedAt(Instant tokenUpdatedAt) {
        this.tokenUpdatedAt = tokenUpdatedAt;
    }
}
