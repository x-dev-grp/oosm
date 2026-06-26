package com.xdev.ooms.production.qualitycontrol.service;

import com.xdev.ooms.production.qualitycontrol.defaults.TunisiaQualityControlDefaults;
import com.xdev.ooms.production.qualitycontrol.entity.QualityControlRule;
import com.xdev.ooms.production.qualitycontrol.repository.QualityControlRuleRepository;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class QualityControlProvisioningService {

    private final QualityControlRuleRepository ruleRepository;

    public QualityControlProvisioningService(QualityControlRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Transactional
    public int provisionDefaultRulesForTenant(UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required to provision QC rules");
        }

        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "provisionDefaultRulesForTenant", tenantId);

        List<QualityControlRule> toSave = new ArrayList<>();
        for (TunisiaQualityControlDefaults.QcRuleTemplate template : TunisiaQualityControlDefaults.all()) {
            boolean oilQc = template.oilQc();
            if (ruleRepository.existsByTenantIdAndRuleKeyAndOilQcAndIsDeletedFalse(tenantId, template.ruleKey(), oilQc)) {
                continue;
            }

            QualityControlRule rule = new QualityControlRule();
            rule.setTenantId(tenantId);
            rule.setRuleKey(template.ruleKey());
            rule.setRuleName(template.ruleName());
            rule.setRuleType(template.ruleType());
            rule.setOilQc(oilQc);
            rule.setMinValue(template.minValue());
            rule.setMaxValue(template.maxValue());
            rule.setRuleTextValue(template.ruleTextValue());
            rule.setDescription(template.description());
            rule.setDeleted(false);
            toSave.add(rule);
        }

        if (!toSave.isEmpty()) {
            ruleRepository.saveAll(toSave);
        }

        OOSMLogger.logMethodExit(this.getClass(), "provisionDefaultRulesForTenant", toSave.size());
        OOSMLogger.logPerformance(this.getClass(), "provisionDefaultRulesForTenant", startTime, System.currentTimeMillis());
        return toSave.size();
    }
}
