package com.xdev.ooms.production.dayimport.service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Visible workbook headings and their stable importer keys. */
final class DayImportColumnLabels {
    private static final Map<String, String[]> COMMON = new HashMap<>();
    private static final Map<String, Map<String, String[]>> BY_SHEET = new HashMap<>();
    private static final Map<String, String[]> SHEETS = new LinkedHashMap<>();
    private static final Map<String, String[]> GUIDE = new HashMap<>();

    static {
        // Tab names are limited to 31 characters and must not contain : \ / ? * [ ]
        tab("Guide", "Guide", "Guide", "الدليل");
        tab("ImportMeta", "Journée", "Day", "اليوم");
        tab("Regions", "Régions", "Regions", "المناطق");
        tab("Parcels", "Parcelles", "Parcels", "القطع الفلاحية");
        tab("SupplierTypes", "Types de fournisseur", "Supplier types", "أنواع المورّدين");
        tab("QcRules", "Contrôles qualité", "Quality checks", "فحوص الجودة");
        tab("Suppliers", "Fournisseurs", "Suppliers", "المورّدون");
        tab("OilContainers", "Contenants", "Containers", "العبوات");
        tab("StorageUnits", "Cuves", "Tanks", "الخزانات");
        tab("Receptions", "Réceptions", "Receptions", "الاستلامات");
        tab("QcResults", "Résultats qualité", "Quality results", "نتائج الجودة");
        tab("Payments", "Paiements", "Payments", "المدفوعات");
        tab("OilSales", "Ventes d'huile", "Oil sales", "مبيعات الزيت");
        tab("OilSaleContainers", "Contenants vendus", "Containers sold", "العبوات المباعة");
        tab("Expenses", "Dépenses", "Expenses", "المصاريف");
        tab("Lists", "Données existantes", "Existing data", "البيانات الحالية");

        guide("title", "Import d'une journée", "Day import workbook", "ملف استيراد يوم");
        guide("subtitle", "Réceptions, contrôles, paiements, ventes et dépenses d'une seule journée",
                "Receptions, quality checks, payments, sales and expenses for one day",
                "الاستلامات والفحوص والمدفوعات والمبيعات والمصاريف ليوم واحد");
        guide("rules", "IMPORTANT — Règles obligatoires", "IMPORTANT — Mandatory rules", "هام — قواعد إجبارية");
        guide("rules.template", "Utilisez uniquement le modèle téléchargé depuis l'application pour votre société. N'utilisez pas le fichier d'une autre société.",
                "Only use the template downloaded from the app for your company. Never use another company's file.",
                "استعمل فقط النموذج الذي نزّلته من التطبيق لشركتك. لا تستعمل ملف شركة أخرى.");
        guide("rules.oneDay", "Un fichier = une seule journée. Renseignez la date dans la feuille « Journée ».",
                "One file = one day. Enter the date on the \"Day\" sheet.",
                "ملف واحد = يوم واحد. أدخل التاريخ في ورقة «اليوم».");
        guide("rules.structure", "Ne renommez, ne supprimez et ne déplacez aucune feuille ni aucun titre de colonne. Ne retirez pas la protection.",
                "Do not rename, delete or move any sheet or column heading. Do not remove the protection.",
                "لا تغيّر اسم أي ورقة أو عنوان عمود ولا تحذفه ولا تنقله. لا تُزل الحماية.");
        guide("rules.references", "Les références (R-, P-, S-, E-) et les lots prévus sont calculés automatiquement. Ne supprimez pas et ne déplacez pas de lignes déjà importées. Les codes (colonnes rouges) doivent être uniques et ne jamais changer.",
                "References (R-, P-, S-, E-) and planned lots are calculated automatically. Do not delete or move rows that were already imported. Codes (red columns) must be unique and never change.",
                "المراجع (R-، P-، S-، E-) والدفعات المتوقعة تُحسب تلقائيًا. لا تحذف ولا تنقل أسطرًا سبق استيرادها. الرموز (الأعمدة الحمراء) يجب أن تكون فريدة ولا تتغيّر أبدًا.");
        guide("references.label", "Références", "References", "المراجع");
        guide("references", "Chaque ligne reçoit une référence automatique (ex. R-20260926-A7K2-001 = réception, date, code du fichier, ligne). Elle relie paiements et contrôles à la bonne réception, et empêche tout doublon si le fichier est importé deux fois.",
                "Each row gets an automatic reference (e.g. R-20260926-A7K2-001 = reception, date, file code, row). It links payments and checks to the right reception and prevents duplicates if the file is imported twice.",
                "كل سطر يحصل على مرجع تلقائي (مثل R-20260926-A7K2-001 = استلام، تاريخ، رمز الملف، السطر). يربط المدفوعات والفحوص بالاستلام الصحيح ويمنع التكرار إذا استُورد الملف مرتين.");
        guide("validation.title", "Valeur refusée", "Value rejected", "قيمة مرفوضة");
        guide("validation.positive", "Saisissez un nombre supérieur à 0, sans unité.", "Enter a number greater than 0, without units.",
                "أدخل رقمًا أكبر من 0 دون وحدة.");
        guide("validation.nonNegative", "Saisissez un nombre positif ou 0, sans unité.", "Enter a number of 0 or more, without units.",
                "أدخل رقمًا يساوي 0 أو أكثر دون وحدة.");
        guide("validation.whole", "Saisissez un nombre entier supérieur à 0.", "Enter a whole number greater than 0.",
                "أدخل عددًا صحيحًا أكبر من 0.");
        guide("validation.date", "Saisissez une date AAAA-MM-JJ, au plus tard aujourd'hui.", "Enter a date as YYYY-MM-DD, no later than today.",
                "أدخل تاريخًا بصيغة YYYY-MM-DD لا يتجاوز اليوم.");
        guide("validation.operation", "Choisissez d'abord le produit reçu, puis une opération de la liste.",
                "Pick the product received first, then an operation from the list.",
                "اختر المنتج المستلم أولًا ثم عملية من القائمة.");
        guide("missing.label", "Cellules rouges", "Red cells", "الخلايا الحمراء");
        guide("missing", "Une cellule devient rouge quand une information obligatoire manque sur une ligne commencée (ex. poids net pour des olives, cuve pour de l'huile).",
                "A cell turns red when required information is missing on a row you started (e.g. net weight for olives, tank for oil).",
                "تصبح الخلية حمراء عندما تنقص معلومة إجبارية في سطر بدأت ملأه (مثل الوزن الصافي للزيتون أو الخزان للزيت).");
        guide("rules.dropdowns", "Choisissez les valeurs dans les listes déroulantes. Saisissez les nouveaux éléments uniquement dans leur feuille dédiée.",
                "Pick values from the dropdowns. Enter new records only on their own sheet.",
                "اختر القيم من القوائم المنسدلة. أدخل العناصر الجديدة في ورقتها المخصّصة فقط.");
        guide("rules.formats", "Dates au format AAAA-MM-JJ. Montants et quantités en chiffres uniquement, sans unité ni symbole monétaire.",
                "Dates as YYYY-MM-DD. Amounts and quantities as plain numbers, without units or currency symbols.",
                "التواريخ بصيغة YYYY-MM-DD. المبالغ والكميات أرقام فقط دون وحدات أو رموز عملة.");
        guide("rules.tanks", "Les cuves doivent exister dans l'application avant l'import.",
                "Tanks must exist in the app before importing.",
                "يجب أن تكون الخزانات موجودة في التطبيق قبل الاستيراد.");
        guide("rules.lot", "Ne saisissez aucun numéro de lot : l'application l'attribue elle-même (ex. 0001OC26), comme pour une réception saisie à la main.",
                "Never enter a lot number: the app assigns it (e.g. 0001OC26), exactly as for a reception entered by hand.",
                "لا تُدخل رقم الدفعة: التطبيق يسنده بنفسه (مثل 0001OC26) كما في الاستلام اليدوي.");
        guide("rules.review", "Vérifiez toujours le rapport de contrôle avant de confirmer. Rien n'est enregistré avant votre confirmation.",
                "Always check the validation report before confirming. Nothing is saved until you confirm.",
                "راجع دائمًا تقرير التحقق قبل التأكيد. لا يُحفظ أي شيء قبل تأكيدك.");
        guide("rules.responsibility", "Les données importées relèvent de la responsabilité de votre société. Une fois confirmé, l'import crée de vraies opérations (stock, paiements, dépenses) : corrigez les erreurs dans l'application, pas en réimportant un fichier modifié.",
                "Imported data is your company's responsibility. Once confirmed, the import creates real operations (stock, payments, expenses): fix mistakes in the app, not by re-importing an edited file.",
                "البيانات المستوردة من مسؤولية شركتك. بعد التأكيد ينشئ الاستيراد عمليات حقيقية (المخزون، المدفوعات، المصاريف): صحّح الأخطاء داخل التطبيق وليس بإعادة استيراد ملف معدَّل.");
        guide("company", "Société", "Company", "الشركة");
        guide("company.name", "Raison sociale", "Legal name", "الاسم القانوني");
        guide("company.address", "Adresse", "Address", "العنوان");
        guide("company.taxId", "Matricule fiscal", "Tax ID", "المعرّف الجبائي");
        guide("company.phone", "Téléphone", "Phone", "الهاتف");
        guide("company.website", "Site web", "Website", "الموقع");
        guide("generated", "Fichier généré le", "File generated on", "تاريخ إنشاء الملف");
        guide("legend", "Code couleur", "Colour code", "دليل الألوان");
        guide("legend.locked.label", "Verrouillé", "Locked", "مقفل");
        guide("legend.locked", "Rempli ou calculé par l'application (titres, références, lots prévus, cuves, données existantes). Ne pas modifier : la feuille est protégée.",
                "Filled or calculated by the app (headings, references, planned lots, tanks, existing data). Do not change: the sheet is protected.",
                "يملؤه التطبيق أو يحسبه (العناوين، المراجع، الدفعات المتوقعة، الخزانات، البيانات الحالية). لا تعدّله: الورقة محمية.");
        guide("legend.reference.label", "Code unique", "Unique code", "رمز فريد");
        guide("legend.reference", "Code unique (fournisseur, contenant, contrôle) : obligatoire. Ne jamais le modifier après l'import.",
                "Unique code (supplier, container, check): required. Never change it after importing.",
                "رمز فريد (مورّد، عبوة، فحص): إجباري. لا تغيّره أبدًا بعد الاستيراد.");
        guide("legend.required.label", "Obligatoire", "Required", "إجباري");
        guide("legend.required", "À remplir pour chaque ligne utilisée.", "Fill in for every row you use.",
                "يجب ملؤه في كل سطر مستعمل.");
        guide("legend.optional.label", "Facultatif", "Optional", "اختياري");
        guide("legend.optional", "Peut rester vide.", "Can be left empty.", "يمكن تركه فارغًا.");
        guide("instructions", "Mode d'emploi", "How to fill in", "طريقة الملء");
        guide("workflow.label", "Étapes", "Steps", "الخطوات");
        guide("workflow", "1) Saisir la date de la journée  2) Remplir les feuilles  3) Vérifier dans l'application  4) Importer",
                "1) Enter the day's date  2) Fill in the sheets  3) Check in the app  4) Import",
                "1) أدخل تاريخ اليوم  2) املأ الأوراق  3) تحقّق في التطبيق  4) استورد");
        guide("day.label", "Journée", "Day", "اليوم");
        guide("day", "Un fichier = une journée. Toutes les lignes utilisent la date de la feuille « Journée ».",
                "One file = one day. Every row uses the date on the \"Day\" sheet.",
                "ملف واحد = يوم واحد. كل الأسطر تستعمل التاريخ المذكور في ورقة «اليوم».");
        guide("existing.label", "Données existantes", "Existing data", "البيانات الحالية");
        guide("existing", "Vos fournisseurs, régions, parcelles, variétés, contenants et contrôles existants sont proposés dans les listes déroulantes. Ne les recopiez pas.",
                "Your existing suppliers, regions, parcels, varieties, containers and checks are offered in the dropdowns. Do not copy them again.",
                "المورّدون والمناطق والقطع والأصناف والعبوات والفحوص الموجودة تظهر في القوائم المنسدلة. لا تعد نسخها.");
        guide("masters.label", "Nouveaux éléments", "New records", "عناصر جديدة");
        guide("masters", "Ajoutez seulement les nouveaux fournisseurs, régions, parcelles, contenants ou contrôles de la journée.",
                "Only add suppliers, regions, parcels, containers or checks that are new today.",
                "أضف فقط المورّدين أو المناطق أو القطع أو العبوات أو الفحوص الجديدة لهذا اليوم.");
        guide("tanks.label", "Cuves", "Tanks", "الخزانات");
        guide("tanks", "La feuille « Cuves » liste vos cuves. Créez les nouvelles cuves dans l'application avant l'import.",
                "The \"Tanks\" sheet lists your tanks. Create new tanks in the app before importing.",
                "ورقة «الخزانات» تعرض خزاناتك. أنشئ الخزانات الجديدة في التطبيق قبل الاستيراد.");
        guide("lot.label", "Numéro de lot", "Lot number", "رقم الدفعة");
        guide("lot", "Attribué par l'application comme pour une saisie manuelle : numéro suivant + type + année, par ex. 0001OC26. OC = conventionnel, OB = bio (olives et huile). Le rapport de contrôle affiche le lot prévu pour chaque réception.",
                "Assigned by the app exactly as for a manual entry: next number + type + year, e.g. 0001OC26. OC = conventional, OB = organic (olives and oil). The validation report shows the planned lot for each reception.",
                "يسنده التطبيق كما في الإدخال اليدوي: الرقم التالي + النوع + السنة، مثل 0001OC26. OC = تقليدي، OB = بيولوجي (زيتون وزيت). يعرض تقرير التحقق الدفعة المتوقعة لكل استلام.");
        guide("duplicates.label", "Doublons", "Duplicates", "التكرار");
        guide("duplicates", "Chaque référence est importée une seule fois. Réimporter le même fichier ne crée pas de doublons.",
                "Each reference is imported once. Importing the same file again does not create duplicates.",
                "كل مرجع يُستورد مرة واحدة. إعادة استيراد نفس الملف لا تنشئ تكرارًا.");
        guide("sample.label", "Exemple", "Sample", "مثال");
        guide("sample", "Lignes de démonstration pour le ", "Demo rows for ", "أسطر تجريبية ليوم ");

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
        add("oliveOilType", "Type (OC conventionnel / OB bio)", "Type (OC conventional / OB organic)", "النوع (OC تقليدي / OB بيولوجي)");
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
        sheet("Lists", "suppliers", "Fournisseurs", "Suppliers", "المورّدون");
        sheet("Lists", "regions", "Régions", "Regions", "المناطق");
        sheet("Lists", "parcels", "Parcelles", "Parcels", "القطع الفلاحية");
        sheet("Lists", "supplierTypes", "Types de fournisseur", "Supplier types", "أنواع المورّدين");
        sheet("Lists", "varieties", "Variétés", "Varieties", "الأصناف");
        sheet("Lists", "containers", "Contenants", "Containers", "العبوات");
        sheet("Lists", "qcRules", "Contrôles qualité", "Quality checks", "فحوص الجودة");
        sheet("Lists", "expenseCategories", "Catégories de dépense", "Expense categories", "أصناف المصاريف");
        sheet("ImportMeta", "key", "Paramètre", "Setting", "الإعداد");
        sheet("ImportMeta", "value", "Valeur", "Value", "القيمة");
        sheet("ImportMetaValues", "businessDate", "Date d'activité", "Business date", "تاريخ النشاط");
        sheet("ImportMetaValues", "timezone", "Fuseau horaire", "Time zone", "المنطقة الزمنية");
        sheet("ImportMetaValues", "templateVersion", "Version du modèle", "Template version", "إصدار النموذج");
        sheet("ImportMetaValues", "fileCode", "Code du fichier", "File code", "رمز الملف");
        sheet("Receptions", "plannedLotNumber", "N° de lot prévu (auto)", "Planned lot number (auto)", "رقم الدفعة المتوقع (تلقائي)");
        sheet("Lists", "nextLotNumbers", "Prochains numéros", "Next numbers", "الأرقام التالية");
        sheet("Lists", "oliveOperations", "Opérations olives", "Olive operations", "عمليات الزيتون");
        sheet("Lists", "oilOperations", "Opérations huile", "Oil operations", "عمليات الزيت");
    }

    private DayImportColumnLabels() { }

    static String language(String requested) {
        String code = requested == null ? "" : requested.trim().toLowerCase(Locale.ROOT);
        if (code.startsWith("ar")) return "ar";
        if (code.startsWith("en")) return "en";
        return "fr";
    }

    /** Visible tab name for a canonical sheet name. */
    static String sheetName(String canonical, String language) {
        String[] names = SHEETS.get(canonical);
        return names == null ? canonical : names[index(language)];
    }

    /** Canonical sheet name for a canonical or translated tab name; unknown names are returned unchanged. */
    static String canonicalSheet(String tabName) {
        String normalized = normalize(tabName);
        for (var entry : SHEETS.entrySet()) {
            if (matches(normalized, entry.getKey(), entry.getValue())) return entry.getKey();
        }
        return tabName;
    }

    static String guide(String key, String language) {
        return GUIDE.get(key)[index(language)];
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

    private static void tab(String canonical, String fr, String en, String ar) {
        SHEETS.put(canonical, new String[]{fr, en, ar});
    }

    private static void guide(String key, String fr, String en, String ar) {
        GUIDE.put(key, new String[]{fr, en, ar});
    }

    private static void sheet(String sheet, String key, String fr, String en, String ar) {
        BY_SHEET.computeIfAbsent(sheet, ignored -> new HashMap<>()).put(key, new String[]{fr, en, ar});
    }
}
