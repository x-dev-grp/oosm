package com.xdev.ooms.sharedkernel.settings.repository;

import com.xdev.ooms.sharedkernel.settings.entity.AppSettingAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppSettingAuditRepository extends JpaRepository<AppSettingAudit, UUID> {
    Page<AppSettingAudit> findBySettingKeyOrderByChangedAtDesc(String settingKey, Pageable pageable);

    Page<AppSettingAudit> findAllByOrderByChangedAtDesc(Pageable pageable);
}
