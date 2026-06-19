package com.xdev.ooms.documents;

/**
 * All PDF document kinds generated for a {@link com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery}.
 */
public enum DocumentType {
    RECEPTION("reception"),
    QUALITY_CONTROL("quality-control"),
    PRODUCTION("production"),
    COMMERCIAL_INVOICE("invoice"),
    PAYMENT_NOTE("payment-note"),
    COMMERCIAL("commercial");

    private final String pathSegment;

    DocumentType(String pathSegment) {
        this.pathSegment = pathSegment;
    }

    public String pathSegment() {
        return pathSegment;
    }

    public static DocumentType fromPath(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Document type is required");
        }
        String normalized = value.trim().toLowerCase().replace('_', '-');
        for (DocumentType type : values()) {
            if (type.pathSegment.equals(normalized)
                    || type.name().equalsIgnoreCase(normalized.replace('-', '_'))) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown document type: " + value);
    }
}
