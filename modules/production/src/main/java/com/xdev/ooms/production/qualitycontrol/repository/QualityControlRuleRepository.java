package com.xdev.ooms.production.qualitycontrol.repository;



import com.xdev.ooms.production.qualitycontrol.entity.QualityControlRule;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QualityControlRuleRepository extends BaseRepository<QualityControlRule> {

    boolean existsByTenantIdAndRuleKeyAndOilQcAndIsDeletedFalse(UUID tenantId, String ruleKey, Boolean oilQc);

    java.util.Optional<QualityControlRule> findFirstByTenantIdAndRuleKeyIgnoreCaseAndOilQcAndIsDeletedFalse(
            UUID tenantId, String ruleKey, Boolean oilQc);

    java.util.Optional<QualityControlRule> findFirstByRuleKeyIgnoreCaseAndOilQcAndIsDeletedFalse(
            String ruleKey, Boolean oilQc);
}