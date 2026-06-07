package com.xdev.ooms.sharedkernel.qr.model;


import lombok.Data;

/**
 * Réponse standardisée de l'API de résolution.
 * Contient toutes les informations nécessaires au mobile pour afficher l'entité.
 */
@Data
public class QrResolveResponse {
    private String entityType;
    private String publicCode;
    private String entityId;
    private String label;
    private String status;
    private String mobileRoute;
    private String webRoute;
    private Object data;
}
