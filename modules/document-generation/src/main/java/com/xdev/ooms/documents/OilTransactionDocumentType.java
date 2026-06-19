package com.xdev.ooms.documents;

public enum OilTransactionDocumentType {
    TRANSACTION("transaction"),
    SORTIE("sortie");

    private final String pathSegment;

    OilTransactionDocumentType(String pathSegment) {
        this.pathSegment = pathSegment;
    }

    public String pathSegment() {
        return pathSegment;
    }

    public static OilTransactionDocumentType fromPath(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Document type is required");
        }
        String normalized = value.trim().toLowerCase().replace('_', '-');
        for (OilTransactionDocumentType type : values()) {
            if (type.pathSegment.equals(normalized)
                    || type.name().equalsIgnoreCase(normalized.replace('-', '_'))) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown oil transaction document type: " + value);
    }
}
