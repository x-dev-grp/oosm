package com.xdev.ooms.documents.form;

import com.xdev.ooms.production.qualitycontrol.entity.QualityControlResult;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Mirrors {@code reception-oil-pdf.config.ts} oil grade extraction logic.
 */
public final class OilGradeExtractor {

    public enum OilGrade {
        EXTRA_VIRGIN, VIRGIN, LAMPANTE, REFINED, POMACE, UNKNOWN
    }

    private static final Map<OilGrade, List<String>> OIL_GRADE_ALIASES = Map.of(
            OilGrade.EXTRA_VIRGIN, List.of(
                    "extra virgin", "extra-virgin", "extra_virgin", "evoo", "extra vierge", "extra-virge",
                    "extravierge", "زيت بكر ممتاز", "بكر ممتاز"),
            OilGrade.VIRGIN, List.of("virgin", "vierge", "زيت بكر", "بكر"),
            OilGrade.LAMPANTE, List.of("lampante", "lampant", "لامبانتي", "لامبانتيه"),
            OilGrade.REFINED, List.of("refined", "raffiné", "raffine", "مكرر"),
            OilGrade.POMACE, List.of("pomace", "huile de grignons", "grignons", "مخلفات الزيتون", "الجفت"));

    private static final Set<String> GRADE_RULE_HINTS = Set.of(
            "quality", "qualite", "qualité", "oil_grade", "grade", "dégustation", "degustation",
            "classe", "categorie", "catégorie", "category", "نوع", "جودة");

    private OilGradeExtractor() {
    }

    public static OilGrade extractGrade(UnifiedDelivery delivery) {
        if (delivery == null || delivery.getQualityControlResults() == null) {
            return OilGrade.UNKNOWN;
        }
        for (QualityControlResult qc : delivery.getQualityControlResults()) {
            OilGrade grade = extractGradeFromQcItem(qc);
            if (grade != null && grade != OilGrade.UNKNOWN) {
                return grade;
            }
        }
        return OilGrade.UNKNOWN;
    }

    public static String gradeLabel(OilGrade grade) {
        if (grade == null) {
            return FormPdfLabels.OIL_GRADE_UNKNOWN;
        }
        return switch (grade) {
            case EXTRA_VIRGIN -> FormPdfLabels.OIL_GRADE_EXTRA_VIRGIN;
            case VIRGIN -> FormPdfLabels.OIL_GRADE_VIRGIN;
            case LAMPANTE -> FormPdfLabels.OIL_GRADE_LAMPANTE;
            case REFINED -> FormPdfLabels.OIL_GRADE_REFINED;
            case POMACE -> FormPdfLabels.OIL_GRADE_POMACE;
            default -> FormPdfLabels.OIL_GRADE_UNKNOWN;
        };
    }

    private static OilGrade extractGradeFromQcItem(QualityControlResult qc) {
        if (qc == null || qc.getRule() == null) {
            return null;
        }
        String ruleName = normalize(firstNonBlank(qc.getRule().getRuleKey(), qc.getRule().getRuleName()));
        boolean ruleLooksLikeGrade = GRADE_RULE_HINTS.stream().anyMatch(ruleName::contains);

        String candidate = firstNonBlank(qc.getMeasuredValue(), qc.getRule().getRuleKey());

        if (ruleLooksLikeGrade && hasText(candidate)) {
            OilGrade grade = toOilGrade(candidate);
            if (grade != null) {
                return grade;
            }
        }

        if (hasText(candidate)) {
            OilGrade grade = toOilGrade(candidate);
            if (grade != null) {
                return grade;
            }
        }
        return null;
    }

    private static OilGrade toOilGrade(String str) {
        if (!hasText(str)) {
            return null;
        }
        String normalized = normalize(str);
        for (Map.Entry<OilGrade, List<String>> entry : OIL_GRADE_ALIASES.entrySet()) {
            for (String alias : entry.getValue()) {
                if (normalized.equals(normalize(alias))) {
                    return entry.getKey();
                }
            }
        }
        return switch (normalized.replaceAll("\\s+", "_")) {
            case "extra_virgin" -> OilGrade.EXTRA_VIRGIN;
            case "virgin" -> OilGrade.VIRGIN;
            case "lampante" -> OilGrade.LAMPANTE;
            case "refined" -> OilGrade.REFINED;
            case "pomace" -> OilGrade.POMACE;
            default -> null;
        };
    }

    private static String normalize(String input) {
        if (input == null) {
            return "";
        }
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private static String firstNonBlank(String... values) {
        return Arrays.stream(values)
                .filter(OilGradeExtractor::hasText)
                .findFirst()
                .orElse(null);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty()
                && !"undefined".equalsIgnoreCase(value.trim())
                && !"null".equalsIgnoreCase(value.trim());
    }
}
