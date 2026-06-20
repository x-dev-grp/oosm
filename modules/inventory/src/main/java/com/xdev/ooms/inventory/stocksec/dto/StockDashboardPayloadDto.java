package com.xdev.ooms.inventory.stocksec.dto;

import com.xdev.ooms.inventory.articlesec.dto.ArticleCritiqueDto;
import com.xdev.ooms.inventory.statistique.dto.StatistiquesDTO;
import java.util.ArrayList;
import java.util.List;

public class StockDashboardPayloadDto {
    private StatistiquesDTO statistiques = new StatistiquesDTO();
    private List<ArticleCritiqueDto> articlesCritiques = new ArrayList<>();
    private List<MouvementRecentDto> mouvementsRecents = new ArrayList<>();

    public StockDashboardPayloadDto() {
    }

    public StockDashboardPayloadDto(StatistiquesDTO statistiques, List<ArticleCritiqueDto> articlesCritiques, List<MouvementRecentDto> mouvementsRecents) {
        this.statistiques = statistiques;
        this.articlesCritiques = articlesCritiques;
        this.mouvementsRecents = mouvementsRecents;
    }

    public StatistiquesDTO getStatistiques() {
        return statistiques;
    }

    public List<ArticleCritiqueDto> getArticlesCritiques() {
        return articlesCritiques;
    }

    public List<MouvementRecentDto> getMouvementsRecents() {
        return mouvementsRecents;
    }

    public void setStatistiques(StatistiquesDTO statistiques) {
        this.statistiques = statistiques;
    }

    public void setArticlesCritiques(List<ArticleCritiqueDto> articlesCritiques) {
        this.articlesCritiques = articlesCritiques;
    }

    public void setMouvementsRecents(List<MouvementRecentDto> mouvementsRecents) {
        this.mouvementsRecents = mouvementsRecents;
    }
}
