# Ressources humaines et paie

## Objectif

Gérer les dossiers employés, les contrats, les pointages, les réglages légaux et la paie de façon fiable, en tenant compte des obligations tunisiennes et de la confidentialité des informations personnelles.

## Public concerné

- gestionnaire RH ;
- responsable de paie ;
- responsable de société ;
- manager ayant besoin de contrôler les absences ou les anomalies de pointage ;
- administrateur de configuration des paramètres légaux.

## Prérequis

- société active correctement sélectionnée ;
- accès aux informations du personnel autorisé ;
- paramètres de paie et de législation disponibles ;
- documents de contrat et de pointage complets et à jour.

## Concepts métier

### Dossier employé
Le dossier employé regroupe les informations essentielles qui permettent d'identifier l'employé, de le rattacher à une société et de calculer sa rémunération.

### Contrat
Le contrat précise les conditions d'emploi, la période de validité et les éléments requis pour le calcul salarial. Un contrat non valide ou incomplet ne peut pas servir à produire une paie cohérente.

### Pointage
Le pointage est utilisé pour contrôler la présence, les absences ou les anomalies. Une anomalie de pointage doit être examinée avant la validation d'une paie.

### Paie
La paie est calculée sur la base du contrat, des données de présence, des paramètres légaux et des règles de cotisation et d'impôt. Elle distingue clairement le brut, les cotisations, l'impôt et le net.

## Procédure principale

1. Ouvrir le dossier employé dans la société active.
2. Vérifier le contrat et sa période de validité.
3. Contrôler les données de présence et repérer les anomalies.
4. Vérifier les paramètres légaux applicables au contexte tunisien.
5. Préparer la paie.
6. Calculer les éléments de salaire, cotisations et impôt.
7. Contrôler le montant final avant validation.
8. Valider la paie lorsque les données sont conformes.
9. Vérifier le net payé et les éléments de suivi associés.

## Règles de gestion

- un employé ne doit pas être traité dans une autre société que celle active ;
- un contrat invalide ou incomplet doit empêcher la paie ou déclencher un blocage métier ;
- un pointage anormal doit être vérifié avant validation ;
- la paie doit tenir compte des paramètres légaux applicables, notamment les règles tunisoises ;
- le salaire minimum et les règles contractuelles doivent être respectés ;
- les informations salariales et personnelles doivent rester accessibles uniquement aux profils autorisés ;
- une paie validée doit être conservée dans l'historique de l'employé et ne pas être recalculée sans procédure dédiée.

## Changements d'état

Un dossier employé passe d'un état de préparation à un état de validation et, si le calcul est acceptable, à une paie validée. Les anomalies de pointage ou les données incomplètes empêchent la génération d'une paie fiable.

Les effets attendus sont :

- dossier employé prêt pour la paie ;
- salaire calculé selon les règles applicables ;
- montant net correctement déterminé ;
- historique de paie et trace de validation conservés ;
- informations confidentielles protégées par les permissions.

## Contrôles et résultats attendus

### Contrôle du dossier employé
Vérifier :

- identité et données principales complètes ;
- contrat actif et conforme ;
- dates de validité cohérentes ;
- accès autorisé à l'information.

Résultat attendu : le dossier employé est exploitable pour la paie.

### Contrôle du pointage
Vérifier :

- présence ou absence cohérente ;
- anomalies repérées ;
- absence de données contradictoires.

Résultat attendu : les éléments de présence sont prêtes à être pris en compte dans le calcul.

### Contrôle de la paie
Vérifier :

- brut calculé correctement ;
- cotisations et retenues applicables ;
- impôt calculé conformément aux règles ;
- net final correctement établi ;
- salaire minimum ou montant contractuel respecté.

Résultat attendu : la paie validée ne présente ni incohérence ni donnée incomplète.

## Erreurs courantes

- contrat absent, expiré ou incomplet ;
- pointage anormal non traité ;
- paramètres légaux non configurés ou incorrects ;
- calcul de paie monté sur un dossier d'un autre employé ou d'une autre société ;
- données salariales consultées sans permission suffisante ;
- validation de paie avec un salaire minimum ou des données contractuelles non conformes.

## Exercice de formation

1. Ouvrir le dossier d'un employé de la société de démonstration.
2. Vérifier la validité du contrat et la cohérence des dates.
3. Contrôler le pointage et repérer les anomalies éventuelles.
4. Lancer la préparation de la paie.
5. Vérifier les éléments de brut, cotisations, impôt et net.
6. Tenter une validation avec des données incomplètes et vérifier qu'elle est refusée.
7. Corriger la donnée manquante ou l'anomalie.
8. Valider la paie et vérifier la cohérence du montant final.
9. Vérifier que les données salariales ne sont pas visibles depuis une autre société.
10. Vérifier qu'un cas de salaire minimum ou de contrat invalide est bien traité conformément au règle métier.

## Liste de contrôle

- [ ] le dossier employé est bien rattaché à la bonne société ;
- [ ] le contrat est actif et complet ;
- [ ] les anomalies de pointage ont été vérifiées ;
- [ ] la paie distingue bien brut, cotisations, impôt et net ;
- [ ] une paie avec données incomplètes est refusée ;
- [ ] le salaire minimum ou les règles contractuelles sont respectés ;
- [ ] la paie validée est conforme aux paramètres légaux applicables ;
- [ ] les informations personnelles et salariales restent limitées aux profils autorisés.
