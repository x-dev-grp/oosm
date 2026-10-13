# Traçabilité, documents et QR code

## Objectif

Suivre l'origine d'un lot, ses étapes de transformation et les documents associés, tout en gardant une traçabilité fiable et isolée par société. Le QR code est un support de vérification rapide et de recherche, mais il ne doit être utilisé que pour des données autorisées et visibles dans la société active.

## Public concerné

- responsable de réception et de production ;
- gestionnaire de qualité ;
- opérateur de suivi de lot ;
- responsable de traçabilité ;
- gestionnaire de documents et d'administration.

## Prérequis

- lot, réception ou produit associé existant ;
- société active correctement sélectionnée ;
- accès aux documents et à la traçabilité autorisé ;
- le QR code ou la référence du lot à rechercher est connu.

## Concepts métier

### Origine du lot
Chaque lot porte l'information de son origine et de ses étapes de transformation. Un lot ne doit pas être suivi seulement par son numéro visible : il faut pouvoir remonter à sa source, à sa qualité et à ses mouvements.

### Qualité et traçabilité
Les contrôles qualité et les évènements liés au lot doivent rester rattachés à l'objet métier concerné. Cela permet de vérifier les conditions de réception, de production ou de filtration.

### QR code
Le QR code sert à retrouver rapidement un lot ou un document. Il doit être régénéré uniquement lorsqu'une action métier autorisée est demandée, et en respectant les règles de sécurité et d'isolation.

### Documents commerciaux et opérationnels
Les documents générés doivent refléter les informations utiles pour le suivi métier : référence, lot, société, dates, quantité, destination et résultats contrôlés si nécessaire.

## Procédure principale

1. Ouvrir le lot ou la réception concernée.
2. Vérifier son origine et son historique de transformation.
3. Contrôler les éléments de qualité rattachés.
4. Rechercher le QR code associé ou générer un QR si l'opération est autorisée.
5. Vérifier que le code correspond bien au lot attendu.
6. Utiliser la recherche par QR code pour retrouver le dossier suivi.
7. Consulter les documents associés et vérifier les informations visibles.
8. En cas d'erreur ou de code inconnu, relever le comportement attendu et ne pas ouvrir de données hors société.

## Règles de gestion

- un lot doit pouvoir être retracé jusqu'à son origine ;
- les contrôles qualité sont rattachés au lot concerné ;
- un QR généré doit correspondre au bon objet métier ;
- la régénération d'un QR existant exige une permission spéciale ;
- un code d'une autre société ne doit pas être exploitable comme si c'était un code de la société active ;
- un QR inexistant ou inconnu doit produire un résultat de recherche vide ou non trouvé ;
- les documents visibles doivent contenir uniquement les informations autorisées et pertinentes pour la société active.

## Changements d'état

La traçabilité n'est pas seulement une lecture : elle est le résultat du bon enchaînement des activités métier. Un lot validé, contrôlé, produit, transformé ou filtré doit garder la mémoire de ses étapes.

Les effets attendus sont :

- historique de transformabilité disponible ;
- qualité et contrôle mis en relation avec le lot ;
- QR code exploitable pour le suivi ;
- documents cohérents avec les données du lot ;
- accès strictement limité à la bonne société.

## Contrôles et résultats attendus

### Contrôle du lot
Vérifier :

- origine et date de création ;
- type de produit ou de réception concerné ;
- étapes de transformation déjà passées ;
- éléments qualité associés.

Résultat attendu : le lot est identifiable et traçable.

### Contrôle du QR code
Vérifier :

- le code correspond bien au bon lot ;
- la génération est autorisée ;
- la régénération d'un QR existant est clairement contrôlée ;
- le code d'une autre société ne se résout pas dans la société active.

Résultat attendu : le code est fiable et ne révèle pas de données hors périmètre.

### Contrôle des documents
Vérifier :

- les documents disponibles correspondent bien au dossier ;
- les informations visibles sont cohérentes avec le lot et la société ;
- aucun document d'une autre société n'est affiché sans autorisation.

Résultat attendu : les documents sont exploitables et conformes à l'usage métier.

## Erreurs courantes

- QR code inconnu ou de mauvaise référence ;
- recherche d'un code d'une autre société ;
- régénération d'un QR existant sans autorisation ;
- document affiché sans lien clair avec le lot ou la société active ;
- historique incomplet ou absence de rattachement entre lot et contrôle qualité ;
- lot reconstitué sans source claire.

## Exercice de formation

1. Ouvrir un lot de démonstration dans la société active.
2. Vérifier son origine et les étapes déjà effectuées.
3. Contrôler le rattachement entre le lot et ses résultats qualité.
4. Rechercher le QR code associé et vérifier le résultat obtenu.
5. Générer ou régénérer le QR code selon le cas autorisé.
6. Vérifier qu'un QR d'une autre société n'est pas résolu dans la société active.
7. Ouvrir les documents associés au lot et vérifier qu'ils correspondent bien à la transaction.
8. Tester un code inconnu et vérifier qu'il est traité comme introuvable.
9. Vérifier que l'accès aux documents et à la traçabilité reste limité à l'utilisateur autorisé.

## Liste de contrôle

- [ ] le lot est bien rattaché à son origine ;
- [ ] les données de qualité restent associées au bon lot ;
- [ ] le QR code correspond bien à l'objet attendu ;
- [ ] la régénération d'un QR existant est bien contrôlée ;
- [ ] un code d'une autre société est bien traité comme introuvable ;
- [ ] les documents visibles correspondent au lot et à la société active ;
- [ ] la recherche du code est fiable et sécurisée ;
- [ ] l'historique de traçabilité est exploitable pour le contrôle métier.
