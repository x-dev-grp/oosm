# Prompt de continuation du guide de formation OOSM

Copier l'intégralité du bloc suivant dans une nouvelle conversation avec l'IA chargée de poursuivre la documentation.

---

Tu travailles dans le dépôt backend OOSM. Ta mission est de poursuivre et finaliser le guide de formation client situé dans `docs/formation/`.

## Contexte produit

OOSM est une application métier multi-sociétés pour la réception d'olives et d'huile, le contrôle qualité, la production, les cuves, les transactions d'huile, la filtration, la finance, les ressources humaines, l'administration et la traçabilité.

Le guide existant couvre actuellement :

1. import journalier ;
2. permissions et isolation des sociétés ;
3. réceptions, planning et contrôle qualité ;
4. huile, transactions et cuves ;
5. filtration ;
6. tableau de bord administrateur.

Le client attend rapidement un guide utilisable en formation. Priorise les fonctions majeures et les parcours réellement disponibles. Les prochains chapitres prioritaires sont :

7. ventes d'huile, facturation et paiements ;
8. ressources humaines, présence, contrats et paie ;
9. traçabilité, documents et recherche par QR code.

## Séparation obligatoire

Le contenu de `docs/formation/` est exclusivement destiné au client et aux utilisateurs métier.

Dans le guide client :

- expliquer les objectifs, prérequis, procédures, décisions métier, changements d'état, contrôles, résultats attendus et exercices ;
- utiliser un français clair, direct et professionnel ;
- expliquer les termes métier lors de leur première utilisation ;
- décrire ce que l'utilisateur voit et doit vérifier ;
- ne pas inclure de noms de classes Java, repositories, tables SQL, annotations, détails Spring, extraits de code, chemins internes ou diagnostics de développement ;
- ne pas transformer le guide en documentation API ;
- ne pas exposer de vulnérabilités, secrets, données réelles ou procédures de contournement ;
- conserver les constatations techniques dans `docs/formation-authoring/TECHNICAL-REFERENCE.md`.

## Règles de preuve

Ne documente jamais une fonction sur la base de son nom seulement. Vérifie son comportement dans le code, les tests et les états métier.

Pour chaque chapitre :

1. identifier les contrôleurs et services concernés ;
2. relever les permissions exigées ;
3. relever les règles d'isolation par société ;
4. relever les états d'entrée et de sortie ;
5. relever les effets sur le stock, la finance et la traçabilité ;
6. relever les opérations non répétables et les mécanismes d'annulation ;
7. comparer les tests existants au comportement documenté ;
8. consigner les détails techniques et les écarts dans la référence technique ;
9. écrire uniquement le comportement confirmé dans le guide client.

Si l'interface frontend n'est pas disponible dans ce dépôt, ne fabrique pas de libellé de bouton ou de chemin de menu. Utilise une formulation neutre telle que « ouvrir la réception » ou « lancer la validation ».

## Structure obligatoire d'un chapitre client

Chaque nouveau fichier doit utiliser cette structure lorsqu'elle est pertinente :

```markdown
# Titre métier

## Objectif

## Public concerné

## Prérequis

## Concepts métier

## Procédure principale

## Règles de gestion

## Changements d'état

## Contrôles et résultats attendus

## Erreurs courantes

## Exercice de formation

## Liste de contrôle
```

Les listes de contrôle doivent être observables. Évite les cases vagues comme « comprendre le module ». Utilise des validations telles que « une seconde validation ne crée pas un second mouvement de stock ».

## Contenu attendu des chapitres 7 à 9

### Chapitre 7 — Ventes d'huile, facturation et paiements

Documenter au minimum :

- création d'une vente et sélection de la cuve source ;
- contrôle du volume disponible ;
- calcul du montant, de la TVA et du reste à payer ;
- paiement partiel et paiement total ;
- prévention du surpaiement ;
- effets de la finalisation sur le stock et la finance ;
- annulation ou suppression et restitution éventuelle du stock ;
- restrictions sur une transaction déjà terminée ;
- isolation par société et conflits concurrents.

### Chapitre 8 — Ressources humaines et paie

Documenter au minimum :

- dossier employé ;
- contrat et dates de validité ;
- pointage et anomalies ;
- paramètres légaux tunisiens applicables ;
- préparation, calcul, contrôle et validation d'une paie ;
- distinction entre brut, cotisations, impôt et net ;
- permissions et confidentialité ;
- cas de salaire minimum ou données contractuelles incomplètes ;
- limites connues confirmées dans la référence technique.

### Chapitre 9 — Traçabilité, documents et QR code

Documenter au minimum :

- origine d'un lot et étapes de transformation ;
- rattachement des contrôles qualité ;
- génération et régénération d'un QR code ;
- recherche et résolution d'un code ;
- documents commerciaux et opérationnels disponibles ;
- données visibles dans les documents ;
- comportement lorsqu'un code est inconnu ;
- isolation stricte : un code d'une autre société doit apparaître comme introuvable ;
- permission spéciale nécessaire pour régénérer un QR existant.

## Contrôle qualité documentaire

Avant de terminer :

- vérifier tous les liens Markdown du sommaire ;
- vérifier la cohérence des termes entre les chapitres ;
- vérifier qu'aucun chapitre client ne contient de détail technique interne ;
- vérifier que les états et effets métier correspondent au code actuel ;
- vérifier que les actions destructives sont accompagnées de leur conséquence métier ;
- vérifier que les exercices utilisent une société de démonstration et des données jetables ;
- mettre à jour `docs/formation/README.md` avec les nouveaux chapitres ;
- mettre à jour `docs/formation-authoring/TECHNICAL-REFERENCE.md` avec les sources examinées, les tests exécutés, les limites et les écarts ;
- exécuter `git diff --check` ;
- ne pas modifier les changements locaux sans rapport avec la documentation.

## Livrable final

Produire des fichiers Markdown prêts à être relus par un responsable métier. Dans le compte rendu final, séparer clairement :

- chapitres client créés ou modifiés ;
- référence technique mise à jour ;
- comportements vérifiés ;
- limites ou points nécessitant une validation métier.

---
