# Réceptions, planning et contrôle qualité

## Objectif

Traiter une réception jusqu'à sa destination correcte sans doubler le stock ni réutiliser un numéro de lot.

## Principes du contrôle qualité

Chaque société dispose de son propre référentiel de contrôle qualité. Lors de la création d'une nouvelle société, l'application prépare automatiquement les règles tunisiennes standard. Un responsable autorisé peut relancer cette préparation si des règles manquent. Les règles déjà présentes sont conservées afin de ne pas écraser les adaptations de la société.

Deux familles de contrôles sont distinguées :

- contrôles des olives : fruits infestés, fermentés ou endommagés, catégorie et état du camion ;
- contrôles de l'huile : catégorie, acidité, K232, K270, Delta K, indice de peroxyde et état du camion.

Une règle destinée à l'huile ne peut pas être utilisée sur une réception d'olives, et inversement. Les règles et résultats d'une autre société ne sont jamais proposés.

## Préparation du référentiel qualité

1. Ouvrir la configuration des règles de contrôle qualité dans la société active.
2. Vérifier que les règles olives et huile sont présentes.
3. Si le référentiel est vide ou incomplet, lancer la préparation des règles par défaut avec un compte autorisé.
4. Vérifier le nombre de règles ajoutées.
5. Relancer la même action et vérifier qu'aucun doublon n'est créé.

La préparation complète ajoute douze règles : sept pour l'huile et cinq pour les olives. Une règle supprimée peut être recréée lors d'une nouvelle préparation. Une règle existante n'est pas remise à sa valeur d'origine automatiquement.

## Saisie d'un contrôle

1. Ouvrir la réception dans la société active.
2. Vérifier le type de réception : olives ou huile.
3. Ouvrir le contrôle qualité.
4. Saisir une valeur pour chaque règle nécessaire.
5. Respecter le format demandé : nombre, choix dans une liste ou valeur oui/non.
6. Enregistrer le contrôle une seule fois.
7. Vérifier le nouvel état de la réception.

Pour une réception d'huile, la catégorie peut être déterminée à partir des mesures enregistrées. Les valeurs hors limites restent visibles pour permettre la décision métier et peuvent déclencher une notification de non-conformité.

## Effets métier

Après un contrôle valide :

- la réception est marquée comme contrôlée ;
- une réception d'huile passe à l'état `OIL_CONTROLLED` ;
- une réception d'olives passe à l'état `OLIVE_CONTROLLED` ;
- une opération de base sur olives peut passer directement à `PROD_READY` ;
- les résultats restent attachés au lot et alimentent sa traçabilité ;
- les responsables peuvent recevoir une notification de contrôle terminé ou de non-conformité.

Le contrôle qualité ne déplace pas lui-même le stock. Les mouvements de stock sont réalisés par les étapes métier suivantes : mise en stock, production, transfert, vente ou filtration.

## Flux principal

1. Créer la réception dans la société active.
2. Contrôler le fournisseur, le type, le poids ou la quantité d'huile.
3. Enregistrer le contrôle qualité.
4. Affecter la réception au planning si nécessaire.
5. Finaliser le lot.
6. Définir le prix selon le type d'opération.
7. Vérifier les mouvements financiers et de stock associés.

## Règles importantes

- le numéro de lot est séquentiel par société et par année ;
- un numéro attribué n'est pas réutilisé ;
- une réception olive ne peut produire qu'une seule branche huile ;
- une réception d'huile déjà `IN_STOCK` ne peut pas être tarifée ou contrôlée une seconde fois ;
- une tarification par échange déjà réalisée ne peut pas être répétée ;
- la finalisation applique un prix uniquement à `SIMPLE_RECEPTION` ;
- la suppression inverse les transactions financières liées ;
- les heures du moulin doivent être cohérentes et positives.
- le contrôle n'est accepté que dans un état permettant encore le contrôle de la réception ;
- toutes les mesures envoyées ensemble doivent concerner la même réception ;
- une valeur numérique doit être un nombre ;
- une valeur textuelle doit appartenir aux choix autorisés lorsqu'une liste est définie ;
- une règle doit correspondre au type olives ou huile de la réception ;
- une règle et une réception doivent être accessibles dans la société active.

Les routes historiques de changement d'état et de prix acceptent encore `GET` et `POST` pour compatibilité. Les nouveaux clients doivent utiliser `POST` pour toute action qui modifie des données.

## Exercice de non-régression

1. Créer deux lots dans la même société et la même année.
2. Vérifier que leurs numéros sont différents et ordonnés.
3. Enregistrer un contrôle qualité.
4. Tenter une valeur de mauvais format et vérifier son refus.
5. Tenter une règle huile sur une réception d'olives et vérifier son refus.
6. Finaliser le lot depuis le planning.
7. Créer la branche huile.
8. Répéter la création de la branche huile.
9. Vérifier que la seconde tentative est refusée.
10. Supprimer une réception contenant une transaction financière de démonstration.
11. Vérifier que la transaction est inversée.

## Liste de contrôle

- [ ] Les numéros de lot sont uniques dans la société et l'année.
- [ ] Une autre société ne peut pas consulter le lot.
- [ ] La préparation des règles est sans doublon.
- [ ] Les règles proposées correspondent au type de réception.
- [ ] Les formats de mesure invalides sont refusés.
- [ ] Le contrôle produit l'état métier attendu.
- [ ] La double création d'huile est impossible.
- [ ] Le stock n'est enregistré qu'une fois.
- [ ] La suppression inverse correctement la finance liée.
