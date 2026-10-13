# Ventes d'huile, facturation et paiements

## Objectif

Finaliser une vente d'huile correctement, en respectant la disponibilité du stock, la facturation, le paiement et les règles de société. Le bon résultat est un mouvement de stock cohérent, un montant calculé correctement et un paiement qui ne dépasse pas le montant dû.

## Public concerné

- responsable de réception et de vente ;
- gestionnaire de stock ;
- comptable ou gestionnaire de paiements ;
- superviseur de société ;
- personne chargée du contrôle de la vente et de la clôture du dossier.

## Prérequis

- avoir une société active correctement sélectionnée ;
- disposer d'une cuve ou d'un stock d'huile disponible ;
- vérifier la quantité demandée, la date de vente et le client concerné ;
- s'assurer qu'aucune vente déjà finalisée ne soit modifiée sans autorisation.

## Concepts métier

### Vente d'huile
Une vente d'huile est une opération de sortie de stock. Elle doit être préparée à partir d'un stock réel et d'une source valide, puis finalisée pour produire les effets métier attendus.

### Cuve source
La vente est rattachée à une cuve ou à une source de stock identifiée. La quantité demandée ne peut pas dépasser la quantité disponible dans cette source.

### Facturation
Le montant de la vente est calculé à partir du volume concerné, du prix applicable et des éléments de fiscalité. La facture doit refléter le montant réel dû et ne doit pas être modifiée sans une validation métier explicite.

### Paiement
Un paiement peut être partiel ou total. Le système doit empêcher toute écriture qui dépasse le montant restant dû et empêcher une deuxième validation du même paiement de façon répétée.

## Procédure principale

1. Ouvrir la vente dans la société active.
2. Sélectionner la source de stock ou la cuve concernée.
3. Vérifier le volume disponible avant validation.
4. Saisir le client, le prix et la quantité ou le volume à vendre.
5. Vérifier le montant total, la TVA et le montant restant à payer.
6. Enregistrer la vente.
7. Effectuer un paiement partiel ou total selon le cas.
8. Contrôler que le solde est bien mis à jour.
9. Finaliser la vente lorsque les conditions sont remplies.
10. Vérifier les effets sur le stock et la finance.

## Règles de gestion

- une vente ne peut pas être créée si la quantité demandée dépasse la quantité disponible ;
- le paiement ne doit pas dépasser le montant restant dû ;
- un paiement déjà finalisé ne doit pas être réappliqué ;
- une vente déjà terminée ne doit pas être dupliquée ni validée une seconde fois ;
- l'annulation ou la suppression d'une vente doit produire l'effet métier attendu sur le stock et la finance ;
- les transactions sont isolées par société ;
- les conflits simultanés doivent être détectés et ne pas écraser des informations déjà validées.

## Changements d'état

La vente suit un processus métier cohérent : préparation, validation, encaissement, puis finalisation. Une vente finalisée ne doit plus être modifiée comme une simple opération en attente.

Le résultat attendu est un dossier de vente stable :

- la sortie de stock est prise en compte ;
- la finance est alignée sur la vente ;
- le paiement est conforme au montant dû ;
- l'annulation, si elle est autorisée, rétablit le stock et compense ou retire la finance associée.

## Contrôles et résultats attendus

### Contrôle du stock
Avant finalisation, vérifier :

- la source de stock est identifiée ;
- le volume disponible est suffisant ;
- la livraison ou la sortie ne dépasse pas la quantité réellement disponible.

Résultat attendu : aucune sortie de stock ne dépasse la capacité disponible.

### Contrôle du montant
Vérifier :

- la quantité vendue ;
- le prix unitaire ou le prix appliqué ;
- le calcul du montant total ;
- la TVA applicable ;
- le reste à payer après éventuel acompte.

Résultat attendu : le montant final est correct et cohérent avec le volume et le prix.

### Contrôle du paiement
Vérifier :

- le paiement partiel ou total ;
- la compatibilité entre le montant payé et le montant dû ;
- l'absence de surpaiement ;
- la disponibilité du justificatif ou du traitement de paiement associé.

Résultat attendu : le montant encaissé reste conforme au règlement attendu.

## Erreurs courantes

- commande de vente supérieure au stock disponible ;
- montant calculé incohérent après prise en compte de la quantité ;
- paiement supérieur au solde restant ;
- tentative de finalisation d'une vente déjà close ;
- annulation d'une vente ou d'un paiement déjà terminé sans autorisation ou sans règle de compensation ;
- vente réalisée dans une mauvaise société ou pour une source non autorisée.

## Exercice de formation

1. Créer une vente dans une société de démonstration.
2. Sélectionner une source de stock d'huile disponible.
3. Vérifier le volume de la source et la quantité demandée.
4. Valider le calcul du montant et de la TVA.
5. Réaliser un paiement partiel puis vérifier le reste à payer.
6. Vérifier que le système refuse un paiement supérieur au restant dû.
7. Finaliser la vente et vérifier que le stock et la finance reflètent la transaction.
8. Tenter une seconde validation ou une répétition du paiement et vérifier qu'elle est refusée.
9. Annuler la vente si le workflow le permet et vérifier la restitution attendue du stock et la compensation financière.
10. Vérifier que la vente ne peut pas être vue depuis une autre société.

## Liste de contrôle

- [ ] la quantité vendue ne dépasse pas le stock disponible ;
- [ ] le montant total, la TVA et le reste à payer sont cohérents ;
- [ ] un paiement partiel est bien pris en compte ;
- [ ] le surpaiement est refusé ;
- [ ] la vente finalisée ne peut pas être validée une seconde fois ;
- [ ] l'annulation rétablit bien le stock et la finance associée ;
- [ ] la transaction reste isolée dans la société active ;
- [ ] un dossier déjà terminé ne peut pas être modifié comme une vente en attente.
