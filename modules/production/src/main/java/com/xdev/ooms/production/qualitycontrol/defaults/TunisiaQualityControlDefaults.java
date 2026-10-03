package com.xdev.ooms.production.qualitycontrol.defaults;

import com.xdev.ooms.sharedkernel.Enum.RuleType;

import java.util.List;

public final class TunisiaQualityControlDefaults {

    private TunisiaQualityControlDefaults() {
    }

    public record QcRuleTemplate(
            String ruleKey,
            String ruleName,
            RuleType ruleType,
            boolean oilQc,
            Float minValue,
            Float maxValue,
            String ruleTextValue,
            String description
    ) {
    }

    public static List<QcRuleTemplate> all() {
        return List.of(
                oilRule("Categorie", "Catégorie", RuleType.STRING, null, null,
                        "Extra Vierge,Vierge,Lampante"),
                oilRule("Acidite", "Acidité (% maaa)", RuleType.NUMERIC, 0f, 2.0f, null),
                oilRule("K232", "K232", RuleType.NUMERIC, 0f, 2.60f, null),
                oilRule("K270", "K270", RuleType.NUMERIC, 0f, 0.25f, null),
                oilRule("DeltaK", "Delta K", RuleType.NUMERIC, 0f, 0.01f, null),
                oilRule("IndicePreoxyde", "Indice peroxyde (meq O2/kg)", RuleType.NUMERIC, 0f, 20f, null),
                oilRule("EtatCamion", "État camion", RuleType.STRING, null, null,
                        "Conforme,Non conforme"),
                oliveRule("Infestees", "Infestées %", RuleType.NUMERIC, 0f, 100f, null),
                oliveRule("Fermentees", "Fermentées %", RuleType.NUMERIC, 0f, 100f, null),
                oliveRule("Endommagees", "Endommagées %", RuleType.NUMERIC, 0f, 100f, null),
                oliveRule("Categorie", "Catégorie Olive", RuleType.STRING, null, null,
                        "Vierge Extra,Vierge,Lampante"),
                oliveRule("EtatCamion", "État camion", RuleType.STRING, null, null,
                        "Conforme,Non conforme")
        );
    }

    private static QcRuleTemplate oilRule(
            String ruleKey,
            String ruleName,
            RuleType ruleType,
            Float minValue,
            Float maxValue,
            String ruleTextValue
    ) {
        return new QcRuleTemplate(ruleKey, ruleName, ruleType, true, minValue, maxValue, ruleTextValue,
                null);
    }

    private static QcRuleTemplate oliveRule(
            String ruleKey,
            String ruleName,
            RuleType ruleType,
            Float minValue,
            Float maxValue,
            String ruleTextValue
    ) {
        return new QcRuleTemplate(ruleKey, ruleName, ruleType, false, minValue, maxValue, ruleTextValue,
                null);
    }
}
