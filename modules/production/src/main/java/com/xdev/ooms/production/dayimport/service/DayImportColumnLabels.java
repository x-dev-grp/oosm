package com.xdev.ooms.production.dayimport.service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Visible workbook headings and their stable importer keys. */
final class DayImportColumnLabels {
    private static final Map<String, String[]> COMMON = new HashMap<>();
    private static final Map<String, Map<String, String[]>> BY_SHEET = new HashMap<>();

    static {
        add("name", "Nom", "Name", "الاسم");
        add("description", "Description", "Description", "الوصف");
        add("supplierKey", "Code fournisseur", "Supplier code", "رمز المورّد");
        add("regionName", "Région", "Region", "المنطقة");
        add("supplierTypeName", "Type de fournisseur", "Supplier type", "نوع المورّد");
        add("ruleKey", "Code du contrôle", "Check code", "رمز الفحص");
        add("ruleName", "Nom du contrôle", "Check name", "اسم الفحص");
        add("oilQc", "Contrôle de l'huile", "Oil quality check", "فحص جودة الزيت");
        add("ruleType", "Type de résultat", "Result type", "نوع النتيجة");
        add("minValue", "Valeur minimale", "Minimum value", "القيمة الدنيا");
        add("maxValue", "Valeur maximale", "Maximum value", "القيمة القصوى");
        add("ruleTextValue", "Valeur attendue", "Expected value", "القيمة المتوقعة");
        add("lastname", "Nom de famille", "Family name", "اسم العائلة");
        add("phone", "Téléphone", "Phone", "الهاتف");
        add("matriculeFiscal", "Matricule fiscal", "Tax identifier", "المعرّف الجبائي");
        add("containerKey", "Code du contenant", "Container code", "رمز العبوة");
        add("capacityInLiters", "Capacité (L)", "Capacity (L)", "السعة (لتر)");
        add("stockQuantity", "Quantité en stock", "Stock quantity", "الكمية بالمخزون");
        add("buyPrice", "Prix d'achat", "Purchase price", "سعر الشراء");
        add("sellingPrice", "Prix de vente", "Selling price", "سعر البيع");
        add("storageUnitKey", "Code de la cuve", "Tank code", "رمز الخزان");
        add("externalRef", "Référence", "Reference", "المرجع");
        add("deliveryType", "Produit reçu", "Received product", "المنتج المستلم");
        add("oliveOilType", "Type d'olive ou d'huile", "Olive or oil type", "نوع الزيتون أو الزيت");
        add("varietyName", "Variété", "Variety", "الصنف");
        add("operationType", "Type d'opération", "Operation type", "نوع العملية");
        add("parcelName", "Parcelle", "Parcel", "القطعة الفلاحية");
        add("poidsNet", "Poids net (kg)", "Net weight (kg)", "الوزن الصافي (كغ)");
        add("oilQuantity", "Quantité d'huile (L)", "Oil quantity (L)", "كمية الزيت (لتر)");
        add("unitPrice", "Prix unitaire", "Unit price", "سعر الوحدة");
        add("receptionExternalRef", "Référence de réception", "Reception reference", "مرجع الاستلام");
        add("value", "Résultat", "Result", "النتيجة");
        add("amount", "Montant", "Amount", "المبلغ");
        add("paymentMethod", "Mode de paiement", "Payment method", "طريقة الدفع");
        add("paymentDate", "Date du paiement", "Payment date", "تاريخ الدفع");
        add("invoiceNumber", "Numéro de facture", "Invoice number", "رقم الفاتورة");
        add("quantity", "Quantité (L)", "Quantity (L)", "الكمية (لتر)");
        add("currency", "Devise", "Currency", "العملة");
        add("qualityGrade", "Qualité de l'huile", "Oil grade", "جودة الزيت");
        add("paidAmount", "Montant payé", "Amount paid", "المبلغ المدفوع");
        add("saleExternalRef", "Référence de vente", "Sale reference", "مرجع البيع");
        add("count", "Nombre de contenants", "Container count", "عدد العبوات");
        add("object", "Objet de la dépense", "Expense purpose", "موضوع المصروف");
        add("purchaseNature", "Nature de l'achat", "Purchase type", "نوع الشراء");
        add("category", "Catégorie", "Category", "الفئة");
        add("vendor", "Vendeur", "Vendor", "البائع");
        add("invoiceRef", "Référence de facture", "Invoice reference", "مرجع الفاتورة");
        add("notes", "Remarques", "Notes", "ملاحظات");
        sheet("Suppliers", "name", "Prénom", "First name", "الاسم الأول");
        sheet("Receptions", "externalRef", "Référence de réception", "Reception reference", "مرجع الاستلام");
        sheet("Payments", "externalRef", "Référence du paiement", "Payment reference", "مرجع الدفع");
        sheet("OilSales", "externalRef", "Référence de vente", "Sale reference", "مرجع البيع");
        sheet("Expenses", "externalRef", "Référence de dépense", "Expense reference", "مرجع المصروف");
        sheet("ImportMeta", "key", "Paramètre", "Setting", "الإعداد");
        sheet("ImportMeta", "value", "Valeur", "Value", "القيمة");
        sheet("ImportMetaValues", "businessDate", "Date d'activité", "Business date", "تاريخ النشاط");
        sheet("ImportMetaValues", "timezone", "Fuseau horaire", "Time zone", "المنطقة الزمنية");
        sheet("ImportMetaValues", "templateVersion", "Version du modèle", "Template version", "إصدار النموذج");
    }

    private DayImportColumnLabels() { }

    static String language(String requested) {
        String code = requested == null ? "" : requested.trim().toLowerCase(Locale.ROOT);
        if (code.startsWith("ar")) return "ar";
        if (code.startsWith("en")) return "en";
        return "fr";
    }

    static String heading(String sheet, String key, String language) {
        String[] labels = labels(sheet, key);
        return labels == null ? key : labels[index(language)];
    }

    static String key(String sheet, String heading) {
        String normalized = normalize(heading);
        for (var entry : BY_SHEET.getOrDefault(sheet, Map.of()).entrySet()) {
            if (matches(normalized, entry.getKey(), entry.getValue())) {
                return entry.getKey().toLowerCase(Locale.ROOT);
            }
        }
        for (var entry : COMMON.entrySet()) {
            if (matches(normalized, entry.getKey(), labels(sheet, entry.getKey()))) {
                return entry.getKey().toLowerCase(Locale.ROOT);
            }
        }
        return heading == null ? "" : heading.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean matches(String normalized, String key, String[] labels) {
        if (normalized.equals(normalize(key))) return true;
        if (labels == null) return false;
        for (String label : labels) {
            if (normalized.equals(normalize(label))) return true;
        }
        return false;
    }

    private static String[] labels(String sheet, String key) {
        return BY_SHEET.getOrDefault(sheet, Map.of()).getOrDefault(key, COMMON.get(key));
    }

    private static int index(String language) {
        return switch (language(language)) { case "en" -> 1; case "ar" -> 2; default -> 0; };
    }

    private static String normalize(String value) {
        if (value == null) return "";
        String latin = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return latin.toLowerCase(Locale.ROOT).replaceAll("[\\s\\p{Punct}]+", "").trim();
    }

    private static void add(String key, String fr, String en, String ar) {
        COMMON.put(key, new String[]{fr, en, ar});
    }

    private static void sheet(String sheet, String key, String fr, String en, String ar) {
        BY_SHEET.computeIfAbsent(sheet, ignored -> new HashMap<>()).put(key, new String[]{fr, en, ar});
    }
}
