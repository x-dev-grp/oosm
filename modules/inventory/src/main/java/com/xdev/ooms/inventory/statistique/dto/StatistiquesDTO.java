package com.xdev.ooms.inventory.statistique.dto;

import java.util.List;
import java.util.Map;


public class StatistiquesDTO {
    // Indicateurs stock
    private Long totalArticles;
    private Long articlesEnAlerte;
    private Double valeurTotaleStock;
    private Double tauxRupture;
    private Integer joursCouvertureMoyen;

    // Indicateurs achats
    private Long bonsEnAttente;
    private Long bonsValidesMois;
    private Double montantAchatsMois;
    private Double delaiValidationMoyen; // en heures

    // Top articles
    private List<Map<String, Object>> topArticlesValeur;
    private List<Map<String, Object>> articlesRuptureFrequente;

    // Graphiques
    private Map<String, Integer> mouvementsParMois;
    private Map<String, Double> achatsParMaterielSupplier;
    private Map<String, Integer> alertesParCategorie;

    public StatistiquesDTO() {
    }

    public StatistiquesDTO(Long totalArticles, Long articlesEnAlerte, Double valeurTotaleStock, Double tauxRupture, Integer joursCouvertureMoyen, Long bonsEnAttente, Long bonsValidesMois, Double montantAchatsMois, Double delaiValidationMoyen, List<Map<String, Object>> topArticlesValeur, List<Map<String, Object>> articlesRuptureFrequente, Map<String, Integer> mouvementsParMois, Map<String, Double> achatsParMaterielSupplier, Map<String, Integer> alertesParCategorie) {
        this.totalArticles = totalArticles;
        this.articlesEnAlerte = articlesEnAlerte;
        this.valeurTotaleStock = valeurTotaleStock;
        this.tauxRupture = tauxRupture;
        this.joursCouvertureMoyen = joursCouvertureMoyen;
        this.bonsEnAttente = bonsEnAttente;
        this.bonsValidesMois = bonsValidesMois;
        this.montantAchatsMois = montantAchatsMois;
        this.delaiValidationMoyen = delaiValidationMoyen;
        this.topArticlesValeur = topArticlesValeur;
        this.articlesRuptureFrequente = articlesRuptureFrequente;
        this.mouvementsParMois = mouvementsParMois;
        this.achatsParMaterielSupplier = achatsParMaterielSupplier;
        this.alertesParCategorie = alertesParCategorie;
    }

    public Long getTotalArticles() {
        return totalArticles;
    }

    public Long getArticlesEnAlerte() {
        return articlesEnAlerte;
    }

    public Double getValeurTotaleStock() {
        return valeurTotaleStock;
    }

    public Double getTauxRupture() {
        return tauxRupture;
    }

    public Integer getJoursCouvertureMoyen() {
        return joursCouvertureMoyen;
    }

    public Long getBonsEnAttente() {
        return bonsEnAttente;
    }

    public Long getBonsValidesMois() {
        return bonsValidesMois;
    }

    public Double getMontantAchatsMois() {
        return montantAchatsMois;
    }

    public Double getDelaiValidationMoyen() {
        return delaiValidationMoyen;
    }

    public List<Map<String, Object>> getTopArticlesValeur() {
        return topArticlesValeur;
    }

    public List<Map<String, Object>> getArticlesRuptureFrequente() {
        return articlesRuptureFrequente;
    }

    public Map<String, Integer> getMouvementsParMois() {
        return mouvementsParMois;
    }

    public Map<String, Double> getAchatsParMaterielSupplier() {
        return achatsParMaterielSupplier;
    }

    public Map<String, Integer> getAlertesParCategorie() {
        return alertesParCategorie;
    }

    public void setTotalArticles(Long totalArticles) {
        this.totalArticles = totalArticles;
    }

    public void setArticlesEnAlerte(Long articlesEnAlerte) {
        this.articlesEnAlerte = articlesEnAlerte;
    }

    public void setValeurTotaleStock(Double valeurTotaleStock) {
        this.valeurTotaleStock = valeurTotaleStock;
    }

    public void setTauxRupture(Double tauxRupture) {
        this.tauxRupture = tauxRupture;
    }

    public void setJoursCouvertureMoyen(Integer joursCouvertureMoyen) {
        this.joursCouvertureMoyen = joursCouvertureMoyen;
    }

    public void setBonsEnAttente(Long bonsEnAttente) {
        this.bonsEnAttente = bonsEnAttente;
    }

    public void setBonsValidesMois(Long bonsValidesMois) {
        this.bonsValidesMois = bonsValidesMois;
    }

    public void setMontantAchatsMois(Double montantAchatsMois) {
        this.montantAchatsMois = montantAchatsMois;
    }

    public void setDelaiValidationMoyen(Double delaiValidationMoyen) {
        this.delaiValidationMoyen = delaiValidationMoyen;
    }

    public void setTopArticlesValeur(List<Map<String, Object>> topArticlesValeur) {
        this.topArticlesValeur = topArticlesValeur;
    }

    public void setArticlesRuptureFrequente(List<Map<String, Object>> articlesRuptureFrequente) {
        this.articlesRuptureFrequente = articlesRuptureFrequente;
    }

    public void setMouvementsParMois(Map<String, Integer> mouvementsParMois) {
        this.mouvementsParMois = mouvementsParMois;
    }

    public void setAchatsParMaterielSupplier(Map<String, Double> achatsParMaterielSupplier) {
        this.achatsParMaterielSupplier = achatsParMaterielSupplier;
    }

    public void setAlertesParCategorie(Map<String, Integer> alertesParCategorie) {
        this.alertesParCategorie = alertesParCategorie;
    }
}