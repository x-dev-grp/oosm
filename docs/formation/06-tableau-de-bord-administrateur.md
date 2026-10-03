# Tableau de bord administrateur

## Objectif

Lire les indicateurs globaux sans les confondre avec les données opérationnelles d'une société.

## Sections

### Vue des sociétés

Présente une synthèse des sociétés gérées par la plateforme. Utiliser cette vue pour repérer une société inactive, incomplète ou nécessitant une intervention.

### Adoption des modules

Présente les modules activés par société. Une absence d'activité peut provenir d'un module désactivé et non d'une panne.

### Support

Présente une synthèse des tickets de support. Les totaux servent à prioriser les incidents ouverts et leur évolution.

## Vérification des chiffres

1. Choisir une société de démonstration.
2. Noter ses modules actifs.
3. Comparer avec la section d'adoption.
4. Créer un ticket de support de démonstration.
5. Recharger le tableau de bord.
6. Vérifier que le résumé évolue exactement d'une unité dans la catégorie attendue.
7. Fermer le ticket et vérifier le nouveau décompte.

## Diagnostic

- chiffre absent : vérifier les données sources et l'accès administrateur ;
- chiffre ancien : recharger puis vérifier le cache et les journaux serveur ;
- module absent : vérifier son activation pour la société ;
- total incohérent : relever les filtres, la société, l'heure et les identifiants concernés.

## Liste de contrôle

- [ ] La vue des sociétés correspond aux sociétés enregistrées.
- [ ] Les modules actifs correspondent à la configuration de chaque société.
- [ ] La création et la fermeture d'un ticket modifient les bons compteurs.
- [ ] Les utilisateurs non administrateurs n'accèdent pas aux données globales.
