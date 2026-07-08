package com.xdev.ooms.sharedkernel.settings.repository;

import com.xdev.ooms.sharedkernel.settings.entity.AppSettingMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingMetaRepository extends JpaRepository<AppSettingMeta, String> {
}
