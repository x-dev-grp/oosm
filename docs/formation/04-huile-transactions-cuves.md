# Huile, transactions et cuves

## Objectif

Gérer les mouvements d'huile et les cuves sans double comptabilisation ni écrasement concurrent de solde.

## Cycle d'une transaction

1. Créer la transaction.
2. Contrôler la cuve source, la cuve cible, le volume et la société.
3. Conserver la transaction en `PENDING` pendant la vérification.
4. Valider avec la permission `VALIDATE`.
5. Vérifier les nouveaux volumes.

Une transaction `PENDING` ne déplace aucun stock. Seule sa validation applique le mouvement.

## Protections

- une réception d'huile ne peut entrer en stock qu'une fois ;
- seule une transaction `PENDING` peut être approuvée ;
- une transaction terminée ne peut pas être modifiée ;
- une transaction liée à une vente ne peut pas être supprimée ;
- la suppression d'une vente terminée restitue l'huile à la cuve source au coût moyen ;
- la capacité d'une cuve ne peut pas passer sous son volume courant ;
- le coût, le fournisseur et les dates techniques d'une cuve sont protégés pendant une mise à jour ordinaire ;
- la version du solde détecte les écritures concurrentes.

## Gestion d'un conflit `409`

1. Ne pas répéter immédiatement la même requête.
2. Recharger la cuve.
3. Vérifier le nouveau volume et la capacité restante.
4. Recalculer le mouvement.
5. Soumettre une nouvelle fois.

## Liste de contrôle

- [ ] Une transaction en attente ne change pas le volume.
- [ ] La validation change le volume une seule fois.
- [ ] Une seconde entrée de la même réception est refusée.
- [ ] Une transaction validée n'est plus modifiable.
- [ ] Une capacité inférieure au volume courant est refusée.
- [ ] Deux écritures concurrentes ne s'écrasent pas silencieusement.
