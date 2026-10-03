# Filtration

## Objectif

Déplacer une quantité d'huile entre deux cuves par une opération de filtration contrôlée.

## Procédure

1. Sélectionner la cuve source.
2. Vérifier son volume disponible et son coût moyen.
3. Sélectionner la cuve cible.
4. Vérifier sa capacité libre.
5. Créer l'opération de filtration.
6. Contrôler l'état `CREATED`.
7. Saisir le volume réellement terminé.
8. Valider l'opération avec la permission `VALIDATE` ou `UPDATE`.
9. Vérifier les volumes des deux cuves.

## Règles

- les deux cuves doivent appartenir à la société active ;
- le volume demandé ne peut pas dépasser le volume source ;
- le volume demandé ne peut pas dépasser la capacité libre de la cible ;
- le volume terminé doit être strictement positif ;
- le volume terminé ne peut pas dépasser le volume filtré ;
- l'huile reçue par la cible conserve le coût moyen de la source ;
- la mise à jour générique du tableau de filtration est bloquée pour empêcher un second mouvement de stock.

La création générique reste disponible pour compatibilité. Toute modification du cycle métier doit passer par les routes spécifiques de filtration.

## Exercice de non-régression

1. Créer une filtration valide.
2. Essayer un volume supérieur au stock source.
3. Essayer un volume supérieur à la capacité cible.
4. Terminer l'opération avec un volume nul.
5. Terminer avec un volume supérieur au volume prévu.
6. Terminer correctement.
7. Tenter une mise à jour générique après la fin.

## Liste de contrôle

- [ ] Les dépassements source et cible sont refusés.
- [ ] Un volume terminé nul est refusé.
- [ ] Le mouvement final est appliqué une seule fois.
- [ ] Le coût moyen de la source est utilisé dans la cible.
- [ ] Une société ne voit pas les filtrations d'une autre société.
