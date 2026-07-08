package com.xdev.ooms.sharedkernel.settings.repository;

import com.xdev.ooms.sharedkernel.settings.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppSettingRepository extends JpaRepository<AppSetting, UUID> {
    Optional<AppSetting> findBySettingKey(String settingKey);
}
