# Formation OOSM

Ce parcours explique les flux métier sécurisés par la série de correctifs `fix/1` à `fix/6`.
Il sert à la formation des opérateurs, responsables métier, administrateurs et équipes de support.

## Parcours

1. [Import journalier](01-import-journalier.md)
2. [Permissions et isolation des sociétés](02-permissions-et-tenants.md)
3. [Réceptions, planning et contrôle qualité](03-receptions-planning-qualite.md)
4. [Huile, transactions et cuves](04-huile-transactions-cuves.md)
5. [Filtration](05-filtration.md)
6. [Tableau de bord administrateur](06-tableau-de-bord-administrateur.md)
7. [Ventes d'huile, facturation et paiements](07-ventes-huile-facturation-paiements.md)
8. [Ressources humaines et paie](08-ressources-humaines-et-paie.md)
9. [Traçabilité, documents et QR code](09-traçabilité-documents-et-qr-code.md)

## Objectif de validation

Une personne formée doit pouvoir :

- exécuter un flux métier sans contourner les changements d'état prévus ;
- reconnaître un refus fonctionnel normal d'une panne technique ;
- vérifier qu'une donnée appartient à la société active ;
- éviter une double entrée ou un double mouvement de stock ;
- transmettre au support les informations nécessaires à un diagnostic reproductible.

## Environnement de formation

Utiliser une société de démonstration et des données jetables. Ne pas effectuer les exercices sur une base de production.

Pour chaque exercice, relever :

- la société active ;
- l'utilisateur et son rôle ;
- la date et l'heure ;
- l'identifiant de l'objet métier ;
- l'action effectuée ;
- le résultat attendu et le résultat observé.

## Codes de réponse utiles

| Code | Signification opérationnelle |
|---|---|
| `400` | Données ou transition métier refusées. Corriger la saisie. |
| `403` | Permission insuffisante. Ne pas contourner le contrôle. |
| `404` | Objet absent ou appartenant à une autre société. |
| `409` | Modification concurrente. Recharger puis recommencer. |
| `500` | Erreur technique à transmettre au support. |

## Règle de progression

Chaque module se termine par une liste de contrôle. Le module est validé lorsque tous les résultats attendus sont reproduits dans l'environnement de formation.
