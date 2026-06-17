package com.xdev.ooms.conditioning.label.support;

import com.xdev.ooms.conditioning.label.entity.LabelContent;
import com.xdev.ooms.sharedkernel.Enum.QualityGrades;
import com.xdev.ooms.sharedkernel.communicator.models.shared.LabelValidationIssueDto;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class LabelComplianceSupport {

    public static final String DEFAULT_ORIGIN = "Tunisie";
    public static final String DEFAULT_STORAGE_FR =
            "A conserver dans un endroit frais et sec, a l'abri de la chaleur et de la lumiere directe du soleil.";
    public static final String DEFAULT_STORAGE_EN =
            "Store in a cool, dry place away from heat and direct sunlight.";
    public static final String EVOO_LEGAL_STATEMENT =
            "Superior category olive oil obtained directly from olives and solely by mechanical means.";

    private static final Set<String> APPROVED_CATEGORIES = Set.of(
            QualityGrades.EXTRA_VIRGIN.name(),
            QualityGrades.VIRGIN.name(),
            QualityGrades.REFINED.name(),
            "POMACE"
    );

    private static final Set<String> APPROVED_ORIGINS = Set.of(
            "tunisie", "tunisia", "tn", "product of tunisia", "origine : tunisie", "origine: tunisie"
    );

    private static final Pattern NET_QUANTITY_PATTERN = Pattern.compile(
            "^\\s*(\\d+(?:[.,]\\d+)?)\\s*(ml|cl|l|ML|CL|L)\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern BEST_BEFORE_MM_YYYY = Pattern.compile("^\\s*(0[1-9]|1[0-2])/(\\d{4})\\s*$");
    private static final Pattern BEST_BEFORE_DD_MM_YYYY = Pattern.compile(
            "^\\s*(0[1-9]|[12]\\d|3[01])/(0[1-9]|1[0-2])/(\\d{4})\\s*$"
    );

    private static final Map<String, String> INGREDIENT_BY_CATEGORY = Map.of(
            QualityGrades.EXTRA_VIRGIN.name(), "Ingredients: 100% Extra Virgin Olive Oil",
            QualityGrades.VIRGIN.name(), "Ingredients: 100% Virgin Olive Oil",
            QualityGrades.REFINED.name(), "Ingredients: 100% Olive Oil",
            "POMACE", "Ingredients: 100% Olive Pomace Oil"
    );

    private static final Map<String, String> PRODUCT_NAME_BY_CATEGORY = Map.of(
            QualityGrades.EXTRA_VIRGIN.name(), "EXTRA VIRGIN OLIVE OIL",
            QualityGrades.VIRGIN.name(), "VIRGIN OLIVE OIL",
            QualityGrades.REFINED.name(), "OLIVE OIL",
            "POMACE", "OLIVE POMACE OIL"
    );

    private LabelComplianceSupport() {
    }

    public static void applyComplianceDefaults(LabelContent labelContent) {
        if (labelContent == null) {
            return;
        }

        if (isBlank(labelContent.getOriginCountry())) {
            labelContent.setOriginCountry(DEFAULT_ORIGIN);
        }

        if (isBlank(labelContent.getStorageConditions())) {
            labelContent.setStorageConditions(DEFAULT_STORAGE_FR);
        }

        if (isBlank(labelContent.getLotNumber())) {
            labelContent.setLotNumber(generateLotNumber(labelContent.getPackagingDate()));
        }

        ensureIngredientDeclaration(labelContent);
        ensureNutritionDeclaration(labelContent);
        syncLegalDenominationFromCategory(labelContent);
    }

    public static List<LabelValidationIssueDto> validate(LabelContent labelContent) {
        List<LabelValidationIssueDto> issues = new ArrayList<>();
        if (labelContent == null) {
            issues.add(issue("labelContent", "Contenu etiquette introuvable", true));
            return issues;
        }

        validateNotBlank(issues, labelContent.getLegalDenomination(), "legalDenomination",
                "Nom du produit obligatoire");
        validateCategory(issues, labelContent.getQualityGrade());
        validateOrigin(issues, labelContent.getOriginCountry());
        validateNetQuantity(issues, labelContent.getNetQuantity());
        validateNotBlank(issues, labelContent.getResponsibleName(), "responsibleName",
                "Nom du producteur / embouteilleur obligatoire");
        validateNotBlank(issues, labelContent.getResponsibleAddress(), "responsibleAddress",
                "Adresse du producteur obligatoire");
        validateLotNumber(issues, labelContent.getLotNumber());
        validateBestBeforeDate(issues, labelContent.getBestBeforeDate());
        validateNotBlank(issues, labelContent.getStorageConditions(), "storageConditions",
                "Conditions de stockage obligatoires");
        validateNotBlank(issues, resolveIngredientDeclaration(labelContent), "ingredientDeclaration",
                "Declaration des ingredients obligatoire");
        validateNutrition(issues, labelContent.getNutritionDeclarationJson());

        if (QualityGrades.EXTRA_VIRGIN.name().equalsIgnoreCase(
                clean(labelContent.getQualityGrade()))) {
            validateNotBlank(issues, EVOO_LEGAL_STATEMENT, "evooLegalStatement",
                    "Mention legale huile vierge extra obligatoire");
        }

        if (!isBlank(labelContent.getEan13()) && !isValidEan13(labelContent.getEan13())) {
            issues.add(issue("ean13", "Code EAN-13 invalide", true));
        }

        return issues;
    }

    public static String resolveIngredientDeclaration(LabelContent labelContent) {
        if (labelContent == null) {
            return null;
        }
        if (!isBlank(labelContent.getIngredientDeclaration())) {
            return clean(labelContent.getIngredientDeclaration());
        }
        return buildIngredientDeclaration(labelContent.getQualityGrade());
    }

    public static String buildIngredientDeclaration(String qualityGrade) {
        String normalized = clean(qualityGrade);
        if (normalized == null) {
            return null;
        }
        return INGREDIENT_BY_CATEGORY.get(normalized.toUpperCase(Locale.ROOT));
    }

    public static String buildProductName(String qualityGrade) {
        String normalized = clean(qualityGrade);
        if (normalized == null) {
            return null;
        }
        return PRODUCT_NAME_BY_CATEGORY.get(normalized.toUpperCase(Locale.ROOT));
    }

    public static String buildNutritionDeclarationJson() {
        Map<String, Object> nutrition = new LinkedHashMap<>();
        nutrition.put("basis", "per100ml");
        nutrition.put("energyKj", 3389);
        nutrition.put("energyKcal", 824);
        nutrition.put("fatG", 91.6);
        nutrition.put("saturatedFatG", 13.8);
        nutrition.put("carbohydratesG", 0);
        nutrition.put("sugarsG", 0);
        nutrition.put("proteinG", 0);
        nutrition.put("saltG", 0);
        return toJson(nutrition);
    }

    public static String generateLotNumber(LocalDate packagingDate) {
        int year = packagingDate != null ? packagingDate.getYear() : Year.now().getValue();
        return year + "-" + String.format("%06d", Math.abs((int) (System.nanoTime() % 1_000_000)));
    }

    public static boolean isValidEan13(String value) {
        String digits = clean(value);
        if (digits == null || !digits.matches("\\d{13}")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = digits.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int check = (10 - (sum % 10)) % 10;
        return check == (digits.charAt(12) - '0');
    }

    private static void ensureIngredientDeclaration(LabelContent labelContent) {
        if (isBlank(labelContent.getIngredientDeclaration())) {
            labelContent.setIngredientDeclaration(buildIngredientDeclaration(labelContent.getQualityGrade()));
        }
    }

    private static void ensureNutritionDeclaration(LabelContent labelContent) {
        if (isBlank(labelContent.getNutritionDeclarationJson())) {
            labelContent.setNutritionDeclarationJson(buildNutritionDeclarationJson());
        }
    }

    private static void syncLegalDenominationFromCategory(LabelContent labelContent) {
        String productName = buildProductName(labelContent.getQualityGrade());
        if (productName != null && isBlank(labelContent.getLegalDenomination())) {
            labelContent.setLegalDenomination(productName);
        }
    }

    private static void validateCategory(List<LabelValidationIssueDto> issues, String qualityGrade) {
        String normalized = clean(qualityGrade);
        if (normalized == null) {
            issues.add(issue("qualityGrade", "Categorie huile d'olive obligatoire", true));
            return;
        }
        if (!APPROVED_CATEGORIES.contains(normalized.toUpperCase(Locale.ROOT))) {
            issues.add(issue("qualityGrade",
                    "Categorie non autorisee. Categories acceptees: Extra Virgin, Virgin, Olive Oil, Olive Pomace Oil",
                    true));
        }
        if (QualityGrades.LAMPANTE.name().equalsIgnoreCase(normalized)) {
            issues.add(issue("qualityGrade", "Huile lampante non autorisee sur etiquette consommateur", true));
        }
    }

    private static void validateOrigin(List<LabelValidationIssueDto> issues, String originCountry) {
        String normalized = clean(originCountry);
        if (normalized == null) {
            issues.add(issue("originCountry", "Origine obligatoire", true));
            return;
        }
        String key = normalized.toLowerCase(Locale.ROOT);
        boolean approved = APPROVED_ORIGINS.stream().anyMatch(key::contains)
                || key.contains("tunis");
        if (!approved) {
            issues.add(issue("originCountry", "Origine non approuvee. Utiliser Tunisie / Tunisia", true));
        }
    }

    private static void validateNetQuantity(List<LabelValidationIssueDto> issues, String netQuantity) {
        String normalized = clean(netQuantity);
        if (normalized == null) {
            issues.add(issue("netQuantity", "Quantite nette obligatoire", true));
            return;
        }
        if (!NET_QUANTITY_PATTERN.matcher(normalized).matches()) {
            issues.add(issue("netQuantity",
                    "Quantite nette invalide. Exemples: 250 ml, 500 ml, 1 L", true));
        }
    }

    private static void validateLotNumber(List<LabelValidationIssueDto> issues, String lotNumber) {
        String normalized = clean(lotNumber);
        if (normalized == null) {
            issues.add(issue("lotNumber", "Numero de lot obligatoire", true));
            return;
        }
        if (normalized.length() < 4 || normalized.length() > 50) {
            issues.add(issue("lotNumber", "Numero de lot: longueur entre 4 et 50 caracteres", true));
        }
    }

    private static void validateBestBeforeDate(List<LabelValidationIssueDto> issues, String bestBeforeDate) {
        String normalized = clean(bestBeforeDate);
        if (normalized == null) {
            issues.add(issue("bestBeforeDate", "Date limite de consommation obligatoire", true));
            return;
        }
        LocalDate futureBoundary = parseBestBefore(normalized);
        if (futureBoundary == null) {
            issues.add(issue("bestBeforeDate",
                    "Format DDM invalide. Formats acceptes: MM/YYYY ou DD/MM/YYYY", true));
            return;
        }
        if (!futureBoundary.isAfter(LocalDate.now())) {
            issues.add(issue("bestBeforeDate", "La DDM doit etre une date future", true));
        }
    }

    private static void validateNutrition(List<LabelValidationIssueDto> issues, String nutritionJson) {
        if (isBlank(nutritionJson)) {
            issues.add(issue("nutritionDeclaration", "Declaration nutritionnelle obligatoire", true));
        }
    }

    private static LocalDate parseBestBefore(String value) {
        var mmYyyy = BEST_BEFORE_MM_YYYY.matcher(value);
        if (mmYyyy.matches()) {
            int month = Integer.parseInt(mmYyyy.group(1));
            int year = Integer.parseInt(mmYyyy.group(2));
            return YearMonth.of(year, month).atEndOfMonth();
        }
        var ddMmYyyy = BEST_BEFORE_DD_MM_YYYY.matcher(value);
        if (ddMmYyyy.matches()) {
            int day = Integer.parseInt(ddMmYyyy.group(1));
            int month = Integer.parseInt(ddMmYyyy.group(2));
            int year = Integer.parseInt(ddMmYyyy.group(3));
            try {
                return LocalDate.of(year, month, day);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
        return null;
    }

    private static void validateNotBlank(
            List<LabelValidationIssueDto> issues,
            String value,
            String field,
            String message) {
        if (isBlank(value)) {
            issues.add(issue(field, message, true));
        }
    }

    private static LabelValidationIssueDto issue(String field, String message, boolean blocking) {
        LabelValidationIssueDto dto = new LabelValidationIssueDto();
        dto.setField(field);
        dto.setMessage(message);
        dto.setBlocking(blocking);
        return dto;
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(String value) {
        return clean(value) == null;
    }

    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(entry.getKey()).append('"').append(':');
            Object val = entry.getValue();
            if (val instanceof Number) {
                sb.append(val);
            } else {
                sb.append('"').append(String.valueOf(val).replace("\"", "\\\"")).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }
}
