package com.xdev.ooms.production.qualitycontrol.defaults;

import com.xdev.ooms.sharedkernel.Enum.RuleType;

import java.util.List;

public final class TunisiaQualityControlDefaults {

    public static final String REGULATORY_MARKER = "Tunisia default";

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
                        "Extra Vierge,Vierge,Lampante",
                        "COI/Tunisia — classification huile d'olive vierge"),
                oilRule("Acidite", "Acidité (% maaa)", RuleType.NUMERIC, 0f, 2.0f, null,
                        "COI/Tunisia — EVOO ≤0.8%, Vierge ≤2%"),
                oilRule("K232", "K232", RuleType.NUMERIC, 0f, 2.60f, null,
                        "COI/Tunisia — EVOO ≤2.50, Vierge ≤2.60"),
                oilRule("K270", "K270", RuleType.NUMERIC, 0f, 0.25f, null,
                        "COI/Tunisia — EVOO ≤0.22, Vierge ≤0.25"),
                oilRule("DeltaK", "Delta K", RuleType.NUMERIC, 0f, 0.01f, null,
                        "COI/Tunisia — Delta K ≤0.01"),
                oilRule("IndicePreoxyde", "Indice peroxyde (meq O2/kg)", RuleType.NUMERIC, 0f, 20f, null,
                        "COI/Tunisia — indice de peroxyde ≤20"),
                oliveRule("Infestees", "Infestées %", RuleType.NUMERIC, 0f, 100f, null,
                        "Tunisia default — olives infestées (%)"),
                oliveRule("Fermentees", "Fermentées %", RuleType.NUMERIC, 0f, 100f, null,
                        "Tunisia default — olives fermentées (%)"),
                oliveRule("Endommagees", "Endommagées %", RuleType.NUMERIC, 0f, 100f, null,
                        "Tunisia default — olives endommagées (%)"),
                oliveRule("Categorie", "Catégorie Olive", RuleType.STRING, null, null,
                        "Vierge Extra,Vierge,Lampante",
                        "Tunisia default — catégorie olives à réception")
        );
    }

    private static QcRuleTemplate oilRule(
            String ruleKey,
            String ruleName,
            RuleType ruleType,
            Float minValue,
            Float maxValue,
            String ruleTextValue,
            String description
    ) {
        return new QcRuleTemplate(ruleKey, ruleName, ruleType, true, minValue, maxValue, ruleTextValue,
                REGULATORY_MARKER + " — " + description);
    }

    private static QcRuleTemplate oliveRule(
            String ruleKey,
            String ruleName,
            RuleType ruleType,
            Float minValue,
            Float maxValue,
            String ruleTextValue,
            String description
    ) {
        return new QcRuleTemplate(ruleKey, ruleName, ruleType, false, minValue, maxValue, ruleTextValue,
                REGULATORY_MARKER + " — " + description);
    }
}
