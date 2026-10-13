# Permissions et isolation des sociétés

## Objectif

Comprendre la différence entre accès générique, action sensible et isolation des données.

## Principes

Chaque donnée métier appartient à une société. L'application vérifie cette appartenance au niveau des services, même si un identifiant valide est envoyé directement à l'API.

Les opérations génériques restent compatibles avec le fonctionnement existant. Les actions sensibles exigent une permission dédiée, notamment :

- modification d'un prix ;
- paiement ;
- changement d'état ;
- validation ou approbation ;
- finalisation d'une opération.

Les rôles `ADMIN` et `OOSMADMIN` contournent les vérifications de permission, mais pas les règles métier ni l'isolation des sociétés.

## Comportements attendus

| Situation | Résultat attendu |
|---|---|
| Permission absente | `403` |
| Objet d'une autre société | `404` |
| Mise à jour concurrente | `409`, puis rechargement |
| Transition métier invalide | `400` ou `409` avec message lisible |

## Exercice

1. Créer deux sociétés de démonstration A et B.
2. Créer une donnée métier dans A.
3. Tenter de lire puis modifier son identifiant depuis B.
4. Vérifier qu'aucun contenu de A n'est exposé.
5. Retirer une permission sensible à un utilisateur de A.
6. Vérifier que la consultation reste possible si prévue, mais que l'action sensible retourne `403`.
7. Modifier simultanément une même donnée depuis deux sessions.
8. Vérifier que la seconde écriture retourne `409`.

## Liste de contrôle

- [ ] Aucun identifiant d'une autre société ne révèle de données.
- [ ] Les champs `tenantId`, identifiants et champs d'audit ne sont pas remplaçables par le client.
- [ ] Les actions sensibles sont refusées sans permission.
- [ ] Le conflit concurrent demande un rechargement au lieu d'écraser les données.
