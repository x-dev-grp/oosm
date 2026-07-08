package com.xdev.ooms.sharedkernel.settings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_setting_meta")
public class AppSettingMeta {

    @Id
    private String id = "global";

    @Column(name = "settings_version", nullable = false)
    private long settingsVersion;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getSettingsVersion() {
        return settingsVersion;
    }

    public void setSettingsVersion(long settingsVersion) {
        this.settingsVersion = settingsVersion;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
