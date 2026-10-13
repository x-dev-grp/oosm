package com.xdev.ooms.production.dayimport.service;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Tenant records written into a blank template so dropdowns offer values the importer already recognises. */
public record DayImportReferenceData(
        List<String> tanks,
        List<String> suppliers,
        List<String> regions,
        List<String> parcels,
        List<String> supplierTypes,
        List<String> varieties,
        List<String> containers,
        List<String> qcRules,
        /** Delivery sequences the next receptions will take, so the workbook can show planned lot numbers. */
        List<Integer> nextLotSequences) {

    public static final DayImportReferenceData EMPTY =
            new DayImportReferenceData(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

    public DayImportReferenceData {
        nextLotSequences = nextLotSequences == null ? List.of() : List.copyOf(nextLotSequences);
        tanks = distinct(tanks);
        suppliers = distinct(suppliers);
        regions = distinct(regions);
        parcels = distinct(parcels);
        supplierTypes = distinct(supplierTypes);
        varieties = distinct(varieties);
        containers = distinct(containers);
        qcRules = distinct(qcRules);
    }

    /** Trimmed, case-insensitively unique, sorted; the first spelling seen wins. */
    static List<String> distinct(Collection<String> values) {
        if (values == null) return List.of();
        Map<String, String> unique = new TreeMap<>();
        values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .forEach(v -> unique.putIfAbsent(v.toLowerCase(Locale.ROOT), v));
        return List.copyOf(unique.values());
    }
}
