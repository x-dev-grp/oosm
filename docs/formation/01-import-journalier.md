# Import journalier

## Objectif

Préparer, contrôler et valider un classeur d'import sans créer de doublons ni dépasser les limites de stock ou de paiement.

## Prérequis

- société active correcte ;
- permission de création des réceptions ;
- fournisseurs, cuves et données de référence déjà présents ;
- modèle de classeur généré par l'application.

## Procédure

1. Télécharger un nouveau modèle depuis l'application.
2. Conserver les noms et la structure des feuilles.
3. Remplir les références avant les lignes qui les utilisent.
4. Lancer l'analyse à blanc.
5. Corriger toutes les lignes invalides.
6. Vérifier les totaux de stock entrant, stock sortant et dépenses.
7. Lancer la validation définitive une seule fois.
8. Archiver le rapport produit.

## Contrôles appliqués

- isolation par société ;
- refus des fournisseurs inconnus ;
- refus des quantités négatives ;
- refus des clés dupliquées dans le classeur ;
- refus du dépassement de capacité d'une cuve ;
- refus d'un paiement supérieur au solde restant ;
- validation des valeurs de méthode de paiement, devise et qualité pour les ventes d'huile ;
- détection des lignes déjà importées.

Une ligne existante sans entrée de registre peut être reconnue comme doublon et enregistrée comme telle lors de la validation. Elle ne doit pas être recréée manuellement.

## Vérification après import

- le rapport indique le nombre de lignes créées, ignorées et rejetées ;
- les réceptions d'huile atteignent `IN_STOCK` si une entrée de stock existe, sinon `STOCK_READY` ;
- les opérations `BASE` atteignent `PROD_READY` ;
- les autres opérations olive atteignent `COMPLETED` ;
- un second import du même classeur ne crée aucune nouvelle opération.

## Liste de contrôle

- [ ] L'analyse à blanc ne modifie aucune donnée.
- [ ] Une référence inconnue produit une erreur lisible.
- [ ] Un dépassement de cuve est refusé.
- [ ] Un paiement excessif est refusé.
- [ ] La seconde validation est idempotente.
- [ ] Le rapport final est conservé.
