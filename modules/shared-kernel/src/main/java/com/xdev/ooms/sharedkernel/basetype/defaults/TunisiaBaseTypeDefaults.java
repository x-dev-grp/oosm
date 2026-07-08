package com.xdev.ooms.sharedkernel.basetype.defaults;

import com.xdev.ooms.sharedkernel.Enum.TypeCategory;

import java.util.List;

public final class TunisiaBaseTypeDefaults {

    private TunisiaBaseTypeDefaults() {
    }

    public record BaseTypeTemplate(
            TypeCategory type,
            String name,
            String description
    ) {
    }

    public static List<BaseTypeTemplate> all() {
        return List.of(
                oliveType("Oil olives", "Olives intended primarily for oil extraction"),
                oliveType("Table olives", "Olives intended primarily for table consumption"),
                oliveType("Dual-purpose olives", "Olives suitable for both oil extraction and table use"),

                oliveVariety("Chemlali", "Common Tunisian olive cultivar used mainly for oil production"),
                oliveVariety("Chetoui", "Northern Tunisian olive cultivar known for robust oil"),
                oliveVariety("Oueslati", "Central Tunisian olive cultivar used for quality oil"),
                oliveVariety("Zalmati", "Southern Tunisian olive cultivar adapted to arid zones"),
                oliveVariety("Zarrazi", "Tunisian olive cultivar used for oil and table production"),
                oliveVariety("Meski", "Tunisian table olive cultivar"),
                oliveVariety("Picholine", "Mediterranean olive cultivar used for table olives and oil"),
                oliveVariety("Arbequina", "Cultivar used in intensive olive production"),
                oliveVariety("Koroneiki", "Cultivar used in intensive olive production"),

                oilType("Extra virgin olive oil", "Virgin olive oil with low acidity and no sensory defects"),
                oilType("Virgin olive oil", "Virgin olive oil within accepted quality limits"),
                oilType("Lampante olive oil", "Virgin olive oil requiring refining before consumption"),
                oilType("Refined olive oil", "Refined olive oil product"),
                oilType("Olive pomace oil", "Oil produced from olive pomace"),

                oilVariety("Chemlali oil", "Olive oil produced mainly from Chemlali olives"),
                oilVariety("Chetoui oil", "Olive oil produced mainly from Chetoui olives"),
                oilVariety("Blend", "Olive oil produced from multiple olive varieties"),
                oilVariety("Organic olive oil", "Olive oil produced under organic production rules"),
                oilVariety("Early harvest oil", "Olive oil produced from early-harvest olives"),

                wasteType("Olive pomace", "Solid residue from olive oil extraction"),
                wasteType("Vegetation water", "Liquid wastewater from olive oil extraction"),
                wasteType("Olive leaves", "Leaves separated during reception or cleaning"),
                wasteType("Olive pits", "Olive stones separated during processing"),
                wasteType("Filter cake", "Residue from filtration operations"),
                wasteType("Sludge", "Settled residue from tanks or wastewater handling"),

                supplierType("Olive farmer", "Producer supplying olives"),
                supplierType("Olive collector", "Intermediary collecting olives from producers"),
                supplierType("Oil mill", "Mill or processor supplying olive oil services or products"),
                supplierType("Storage provider", "Provider of oil or material storage services"),
                supplierType("Packaging supplier", "Supplier of bottles, cans, labels, or packaging materials"),
                supplierType("Transport provider", "Provider of transport or logistics services"),

                productionMethod("Traditional press", "Olive oil extraction using traditional press equipment"),
                productionMethod("Two-phase extraction", "Modern two-phase olive oil extraction process"),
                productionMethod("Three-phase extraction", "Modern three-phase olive oil extraction process"),
                productionMethod("Cold extraction", "Extraction process controlled to preserve oil quality"),
                productionMethod("Organic production", "Production following organic standards")
        );
    }

    private static BaseTypeTemplate oliveType(String name, String description) {
        return template(TypeCategory.OLIVE_TYPE, name, description);
    }

    private static BaseTypeTemplate oliveVariety(String name, String description) {
        return template(TypeCategory.OLIVE_VARIETY, name, description);
    }

    private static BaseTypeTemplate oilType(String name, String description) {
        return template(TypeCategory.OIL_TYPE, name, description);
    }

    private static BaseTypeTemplate oilVariety(String name, String description) {
        return template(TypeCategory.OIL_VARIETY, name, description);
    }

    private static BaseTypeTemplate wasteType(String name, String description) {
        return template(TypeCategory.WASTE_TYPE, name, description);
    }

    private static BaseTypeTemplate supplierType(String name, String description) {
        return template(TypeCategory.SUPPLIER_TYPE, name, description);
    }

    private static BaseTypeTemplate productionMethod(String name, String description) {
        return template(TypeCategory.PRODUCTION_METHOD, name, description);
    }

    private static BaseTypeTemplate template(TypeCategory type, String name, String description) {
        return new BaseTypeTemplate(type, name, "Tunisia olive oil default - " + description);
    }
}
