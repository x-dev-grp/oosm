package com.xdev.ooms.sharedkernel.qr.model;


/**
 * Réponse standardisée de l'API de résolution.
 * Contient toutes les informations nécessaires au mobile pour afficher l'entité.
 */
public class QrResolveResponse {
    private String entityType;
    private String publicCode;
    private String entityId;
    private String label;
    private String status;
    private String mobileRoute;
    private String webRoute;
    private Object data;

    public String getEntityType() {
        return entityType;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getLabel() {
        return label;
    }

    public String getStatus() {
        return status;
    }

    public String getMobileRoute() {
        return mobileRoute;
    }

    public String getWebRoute() {
        return webRoute;
    }

    public Object getData() {
        return data;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public void setPublicCode(String publicCode) {
        this.publicCode = publicCode;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMobileRoute(String mobileRoute) {
        this.mobileRoute = mobileRoute;
    }

    public void setWebRoute(String webRoute) {
        this.webRoute = webRoute;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
