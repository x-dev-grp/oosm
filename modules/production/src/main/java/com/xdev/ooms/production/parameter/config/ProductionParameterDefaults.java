package com.xdev.ooms.production.parameter.config;

import com.xdev.ooms.sharedkernel.Enum.ParameterType;

import java.util.List;
import java.util.Optional;

public final class ProductionParameterDefaults {

    public static final String PRODUCTION_CATEGORY = "PRODUCTION";
    public static final String FINANCE_CATEGORY = "FINANCE";
    public static final String HR_CATEGORY = "HR";
    public static final String RECEPTION_CATEGORY = "RECEPTION";
    public static final String LOCALE_CATEGORY = "LOCALE";
    public static final String PRINT_CATEGORY = "PRINT";
    public static final String NOTIFICATIONS_CATEGORY = "NOTIFICATIONS";

    private static final List<ProductionParameterDefault> DEFAULTS = List.of(
            new ProductionParameterDefault(
                    "OLIVE_UNIT_PRICE",
                    PRODUCTION_CATEGORY,
                    ParameterType.DOUBLE,
                    "3.50",
                    "Default price per KG of olives"
            ),
            new ProductionParameterDefault(
                    "DAILY_OIL_METRIC",
                    PRODUCTION_CATEGORY,
                    ParameterType.STRING,
                    "{\"current\":0,\"history\":[]}",
                    "Daily BASE oil unit price reference (current and history JSON)"
            ),
            new ProductionParameterDefault(
                    "DEFAULT_OLIVE_VARIETY",
                    PRODUCTION_CATEGORY,
                    ParameterType.STRING,
                    "",
                    "Default olive variety code/name for new receptions"
            ),
            new ProductionParameterDefault(
                    "DEFAULT_PRODUCTION_METHOD",
                    PRODUCTION_CATEGORY,
                    ParameterType.STRING,
                    "",
                    "Default production method for new receptions"
            ),
            new ProductionParameterDefault(
                    "PRIX_TRITURATION_KG",
                    FINANCE_CATEGORY,
                    ParameterType.DOUBLE,
                    "0",
                    "Milling price per kg (TND)"
            ),
            new ProductionParameterDefault(
                    "TVA_ACHAT_AUTO_CALC",
                    FINANCE_CATEGORY,
                    ParameterType.BOOLEAN,
                    "true",
                    "Auto-calculate purchase VAT from TTC prices on buy invoices"
            ),
            new ProductionParameterDefault(
                    "SEASON_PRICING_RULES",
                    FINANCE_CATEGORY,
                    ParameterType.STRING,
                    "[]",
                    "JSON array of season trituration pricing tiers: [{from,to,variety,pricePerKg}]"
            ),
            new ProductionParameterDefault(
                    "CNSS_EMPLOYEE_RATE",
                    HR_CATEGORY,
                    ParameterType.DOUBLE,
                    "9.18",
                    "Employee CNSS contribution rate (%)"
            ),
            new ProductionParameterDefault(
                    "CNSS_EMPLOYER_RATE",
                    HR_CATEGORY,
                    ParameterType.DOUBLE,
                    "16.57",
                    "Employer CNSS contribution rate (%)"
            ),
            new ProductionParameterDefault(
                    "OVERTIME_MULTIPLIER",
                    HR_CATEGORY,
                    ParameterType.DOUBLE,
                    "1.25",
                    "Overtime pay multiplier vs base hourly rate"
            ),
            new ProductionParameterDefault(
                    "RECEPTION_OPEN_TIME",
                    RECEPTION_CATEGORY,
                    ParameterType.STRING,
                    "07:00",
                    "Reception desk opening time (HH:mm)"
            ),
            new ProductionParameterDefault(
                    "RECEPTION_CLOSE_TIME",
                    RECEPTION_CATEGORY,
                    ParameterType.STRING,
                    "18:00",
                    "Reception desk closing time (HH:mm)"
            ),
            new ProductionParameterDefault(
                    "MAX_DAILY_TONNAGE_KG",
                    RECEPTION_CATEGORY,
                    ParameterType.DOUBLE,
                    "0",
                    "Optional daily reception tonnage warning threshold (kg). 0 = disabled"
            ),
            new ProductionParameterDefault(
                    "ENABLE_MILL_PLANNING",
                    RECEPTION_CATEGORY,
                    ParameterType.BOOLEAN,
                    "true",
                    "Enable kanban mill planning board. When false, assign mill manually when completing a lot"
            ),
            new ProductionParameterDefault(
                    "DEFAULT_LANGUAGE",
                    LOCALE_CATEGORY,
                    ParameterType.STRING,
                    "fr",
                    "Tenant default UI language (fr|en|ar)"
            ),
            new ProductionParameterDefault(
                    "DEFAULT_CURRENCY",
                    LOCALE_CATEGORY,
                    ParameterType.STRING,
                    "TND",
                    "Tenant default currency code"
            ),
            new ProductionParameterDefault(
                    "TIMEZONE",
                    LOCALE_CATEGORY,
                    ParameterType.STRING,
                    "Africa/Tunis",
                    "Tenant timezone for reports and campaign clocks"
            ),
            new ProductionParameterDefault(
                    "PRINT_PAPER_SIZE",
                    PRINT_CATEGORY,
                    ParameterType.STRING,
                    "A4",
                    "Default print paper size (A4|TICKET)"
            ),
            new ProductionParameterDefault(
                    "PRINT_SHOW_QR",
                    PRINT_CATEGORY,
                    ParameterType.BOOLEAN,
                    "true",
                    "Show QR code on delivery/invoice prints by default"
            ),
            new ProductionParameterDefault(
                    "PRINT_COPIES",
                    PRINT_CATEGORY,
                    ParameterType.INTEGER,
                    "1",
                    "Default number of print copies"
            ),
            new ProductionParameterDefault(
                    "NOTIFY_ON_QC_FAIL",
                    NOTIFICATIONS_CATEGORY,
                    ParameterType.BOOLEAN,
                    "true",
                    "Notify mill staff when a QC check fails"
            ),
            new ProductionParameterDefault(
                    "NOTIFY_ON_LOW_STOCK",
                    NOTIFICATIONS_CATEGORY,
                    ParameterType.BOOLEAN,
                    "true",
                    "Notify mill staff on low stock alerts"
            ),
            new ProductionParameterDefault(
                    "NOTIFY_ON_CAMPAIGN_END",
                    NOTIFICATIONS_CATEGORY,
                    ParameterType.BOOLEAN,
                    "true",
                    "Notify mill staff when campaign end date approaches"
            ),
            new ProductionParameterDefault(
                    "IMPORT_GDRIVE_ENABLED",
                    RECEPTION_CATEGORY,
                    ParameterType.BOOLEAN,
                    "false",
                    "Enable Google Drive folder polling for day Excel imports"
            ),
            new ProductionParameterDefault(
                    "IMPORT_GDRIVE_FOLDER_ID",
                    RECEPTION_CATEGORY,
                    ParameterType.STRING,
                    "",
                    "Google Drive source folder id for day import workbooks"
            ),
            new ProductionParameterDefault(
                    "IMPORT_GDRIVE_PROCESSED_FOLDER_ID",
                    RECEPTION_CATEGORY,
                    ParameterType.STRING,
                    "",
                    "Google Drive processed/archive folder id for imported workbooks"
            ),
            new ProductionParameterDefault(
                    "IMPORT_GDRIVE_CRON",
                    RECEPTION_CATEGORY,
                    ParameterType.STRING,
                    "0 0 6 * * *",
                    "Cron expression for day import Drive sync (default 06:00)"
            ),
            new ProductionParameterDefault(
                    "IMPORT_GDRIVE_FAILED_FOLDER_ID",
                    RECEPTION_CATEGORY,
                    ParameterType.STRING,
                    "",
                    "Google Drive failed/ folder id for day import workbooks that fail dry-run or commit"
            )
    );

    private ProductionParameterDefaults() {
    }

    public static List<ProductionParameterDefault> all() {
        return DEFAULTS;
    }

    public static Optional<ProductionParameterDefault> find(String code) {
        return DEFAULTS.stream()
                .filter(definition -> definition.code().equals(code))
                .findFirst();
    }
}
