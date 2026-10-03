# Référence technique du guide de formation OOSM

## Finalité

Ce document conserve les preuves techniques utilisées pour rédiger `docs/formation/`. Il n'est pas destiné aux participants de la formation. Le guide client doit rester fonctionnel et orienté métier.

État de la référence : branche `fix/audit-2026-10-01`, vérifiée jusqu'au commit `510f838`.

## Architecture commune

OOSM est un monolithe Maven modulaire sous Spring Boot. Les entités métier dérivent généralement de `BaseEntity`. Elles portent notamment un identifiant, un `tenantId`, un indicateur de suppression logique et, lorsque la fonction est utilisée, un code et une image QR.

Les contrôleurs génériques délèguent à `BaseServiceImpl`. L'isolation est appliquée dans les services par les recherches tenant-aware ou par `TenantAccess.isAccessible`. Un appel utilisateur avec un tenant actif doit voir :

- les lignes du tenant actif ;
- les éventuelles références partagées dont `tenantId` est nul ;
- aucune ligne appartenant à un autre tenant.

Les appels sans tenant sont réservés par convention aux administrateurs plateforme et aux traitements système. Cette convention dépend de la bonne initialisation de `TenantContext` sur toutes les requêtes authentifiées.

Sources principales :

- `modules/shared-kernel/.../entities/BaseEntity.java`
- `modules/shared-kernel/.../services/impl/BaseServiceImpl.java`
- `modules/shared-kernel/.../utils/TenantAccess.java`
- `modules/shared-kernel/.../utils/PermissionSupport.java`
- `modules/shared-kernel/.../controllers/impl/BaseControllerImpl.java`

## 1. Import journalier

Le flux d'import se trouve dans `modules/production/.../dayimport/`.

Comportements confirmés :

- lecture d'un classeur structuré et production d'un aperçu sans écriture ;
- validation préalable des références, types, quantités, capacité des cuves et limites de paiement ;
- séparation stricte par tenant dans le registre d'import et les données métier ;
- traitement idempotent des références externes ;
- refus d'une référence déjà utilisée avec un contenu différent ;
- rapport structuré avec lignes créées, ignorées ou rejetées ;
- protection du commit par les permissions nécessaires aux objets produits.

Tests principaux :

- `DayImportControllerTest`
- `DayImportDriveTest`
- `DayImportServiceTest`
- `DayImportWorkbookTest`
- tests PostgreSQL optionnels, ignorés sans environnement configuré.

## 2. Permissions et isolation des tenants

Les opérations génériques utilisent le nom de ressource du contrôleur et une action. Les actions sensibles disposent de contrôles explicites dans les services ou contrôleurs métier.

Comportements confirmés :

- une entité étrangère est traitée comme absente ;
- les champs protégés ne doivent pas être remplaçables par une mise à jour générique ;
- les rôles administratifs peuvent contourner certaines permissions, mais les règles métier restent applicables ;
- les versions optimistes des entités sensibles produisent un conflit plutôt qu'un écrasement silencieux.

Tests principaux :

- `TenantAccessTest`
- `PermissionSupportTest`
- tests de règles dans les modules production et sécurité.

## 3. Réceptions, planning et contrôle qualité

### Provision des règles

La création d'une société dans `CompanyProfileService.save` publie un `TenantCreatedEvent` après l'enregistrement du tenant, de ses modules et de son premier utilisateur.

`TenantCreatedEventListener` exécute `QualityControlProvisioningService.provisionDefaultRulesForTenant` après le commit de création. Le service exige un tenant non nul puis parcourt `TunisiaQualityControlDefaults.all()`.

La clé d'idempotence applicative est :

```text
tenantId + ruleKey + oilQc + isDeleted=false
```

Une règle active existante est conservée. Une règle supprimée logiquement n'est pas considérée comme existante et sera recréée. Il n'existe pas de mise à jour automatique d'une règle existante lorsque le modèle par défaut change.

Le listener capture les exceptions et les journalise. La création du tenant reste validée si le provisioning QC échoue. Le rattrapage s'effectue par :

```http
POST /api/production/qualitycontrolrules/provision-defaults
```

Cette route exige un tenant actif et l'action `CREATE` sur la ressource `QUALITYCONTROLRULE`.

### Catalogue livré

Sept règles huile :

- `Categorie`, chaîne parmi Extra Vierge, Vierge, Lampante ;
- `Acidite`, numérique de 0 à 2,0 ;
- `K232`, numérique de 0 à 2,60 ;
- `K270`, numérique de 0 à 0,25 ;
- `DeltaK`, numérique de 0 à 0,01 ;
- `IndicePreoxyde`, numérique de 0 à 20 ;
- `EtatCamion`, Conforme ou Non conforme.

Cinq règles olives :

- `Infestees`, numérique de 0 à 100 ;
- `Fermentees`, numérique de 0 à 100 ;
- `Endommagees`, numérique de 0 à 100 ;
- `Categorie`, Vierge Extra, Vierge ou Lampante ;
- `EtatCamion`, Conforme ou Non conforme.

Le service de résultat vérifie l'existence des règles, l'accès tenant et la correspondance du drapeau `oilQc` avec le type de réception.

### Enregistrement des résultats

`QualityControlResultService.saveAll` exige que tous les DTO concernent la même réception. La réception est chargée par une recherche propriétaire tenant-aware.

États acceptés :

- `NEW`
- `WAITING`
- `OLIVE_CONTROLLED`
- `OIL_CONTROLLED`
- `PROD_READY` uniquement pour une opération `BASE`.

Validation par type :

- `NUMERIC` : la valeur doit être convertible en nombre ;
- `BOOLEAN` : `true` ou `false` ;
- `STRING` et `RAW_STRING` : si une liste est configurée, la valeur doit lui appartenir exactement.

Après sauvegarde :

- `hasQualityControl=true` ;
- opération `BASE` + olives : `PROD_READY` ;
- huile : `OIL_CONTROLLED` ;
- autres olives : `OLIVE_CONTROLLED` ;
- publication d'une notification de fin et, si nécessaire, de non-conformité.

Autres variantes :

- `saveOilQcForOliveRec` crée une réception huile issue d'une réception olives puis rattache les résultats huile ;
- `saveForFiltration` rattache des résultats huile à une filtration et, si disponible, à un lot de traçabilité.

Limites techniques :

- le provisioning automatique ne bloque pas la création de société en cas d'échec ;
- l'idempotence repose sur une vérification applicative ; une contrainte unique en base doit être vérifiée avant d'affirmer une protection contre deux provisionings strictement concurrents ;
- les bornes numériques servent à l'évaluation de conformité, tandis que la validation de saisie confirme principalement le type numérique ;
- les choix texte sont sensibles à la casse et aux accents car la comparaison est exacte.

Sources :

- `modules/security/.../companyprofile/service/CompanyProfileService.java`
- `modules/production/.../qualitycontrol/listener/TenantCreatedEventListener.java`
- `modules/production/.../qualitycontrol/service/QualityControlProvisioningService.java`
- `modules/production/.../qualitycontrol/defaults/TunisiaQualityControlDefaults.java`
- `modules/production/.../qualitycontrol/service/QualityControlResultService.java`
- `modules/production/.../unifieddelivery/service/UnifiedDeliveryService.java`

## 4. Huile, transactions et cuves

Les services de transaction et de stockage séparent la création de la validation. Une transaction en attente ne doit pas appliquer le mouvement final. La validation contrôle le tenant, l'état courant, les volumes et les capacités.

Comportements confirmés :

- protection contre une seconde mise en stock de la même réception ;
- validation limitée aux transactions `PENDING` ;
- interdiction de modifier une transaction terminée ;
- protection des transactions liées à une vente ;
- restitution de stock lors des inversions prévues par le domaine ;
- contrôle optimiste sur les soldes de cuve ;
- préservation des champs techniques lors d'une mise à jour générique.

Sources principales :

- `modules/production/.../oiltransaction/`
- `modules/production/.../oilstorage/`
- `modules/production/.../oilsale/`
- `modules/production/.../unifieddelivery/`

## 5. Filtration

Le flux spécialisé de filtration contrôle la cuve source, la cuve cible, les volumes et le coût moyen. Le tableau de filtration bloque les modifications génériques qui pourraient reproduire le mouvement de stock.

Comportements confirmés :

- refus d'un volume supérieur au disponible source ;
- refus d'un volume supérieur à la capacité cible ;
- volume terminé strictement positif et inférieur ou égal au volume prévu ;
- transfert au coût moyen de la source ;
- interdiction de supprimer une filtration terminée ;
- résultats QC filtrés rattachables à l'opération et au lot de traçabilité.

Tests principaux :

- `FiltrationDashServiceRulesTest`
- tests de mouvements de cuves dans le module production.

## 6. Tableau de bord administrateur

Le tableau agrège les sociétés, l'activation des modules et le support. Son accès relève du contexte administrateur plateforme et ne doit pas être confondu avec une vue opérationnelle tenant.

Sources principales :

- `modules/security/.../admin/service/AdminDashboardService.java`
- `modules/security/.../tenantmodule/`
- services de support associés.

Test principal : `AdminDashboardServiceTest`.

## QR code et isolation

Le contrôleur générique autorise la première génération avec `READ`. La régénération d'un QR déjà complet exige `REGENERATE_QR`.

Depuis le correctif `510f838`, les chemins suivants appliquent `TenantAccess` :

- génération et régénération par identifiant ;
- génération d'image par code ;
- génération depuis une entité ;
- résolution et recherche par code ;
- résolveurs spécialisés article, ordre de fabrication, projet, expédition et shipping ;
- recherche globale du module conditionnement.

Un code valide appartenant à un autre tenant doit produire le même résultat qu'un code inconnu. Les appels sans tenant restent réservés aux traitements plateforme/système selon la convention générale.

Test : `BaseServiceQrTenantIsolationTest` couvre la résolution propriétaire, le masquage d'un code étranger et les opérations QR directes.

## 7. Ventes d'huile, facturation et paiements

Le chapitre client est limité au comportement métier confirmé : une vente est préparée depuis un stock disponible, les montants sont calculés puis le paiement est validé sans dépassement du solde restant. Les règles de stock, de finance et d'isolation par société doivent être gardées cohérentes entre création, finalisation et annulation.

Comportements métier à surveiller :

- validation de la disponibilité du stock source avant finalisation ;
- calcul du total, de la TVA et du reste à payer ;
- refus du paiement supérieur au montant restant dû ;
- impossibilité de finaliser une vente déjà terminée ;
- effet d'annulation sur le stock et la finance ;
- isolation des ventes par société et prévention des conflits concurrents.

Limite : cette partie exige une vérification de code ciblée si une validation plus stricte des états et des expressions de paiement est nécessaire.

## 8. Ressources humaines et paie

Le chapitre client couvre le cycle RH : dossier employé, contrat, pointage, préparation de la paie, calcul du brut, des cotisations, de l'impôt et du net, puis validation. Le document reste orienté métier et ne décrit pas les classes ou services internes.

Comportements métier à surveiller :

- contrat actif et complet avant préparation de la paie ;
- anomalies de pointage traitées avant validation ;
- paramètres électroniques ou légaux tunisiens appliqués au calcul ;
- distinction explicite entre brut, cotisations, impôt et net ;
- blocage de la paie si des données sont incomplètes ;
- accès limité aux profils autorisés et à la société active.

Limite : le module HR nécessite un contrôle technique de la configuration légale et des règles de salaire minimum pour confirmer les cas de refus exacts.

## 9. Traçabilité, documents et QR code

Le chapitre client couvre la trace de lot, les étapes de transformation, le rattachement aux contrôles qualité et la recherche par QR code. Les règles de sécurité et d'isolation sont prioritaires : un code d'une autre société ne doit pas devenir exposable dans la société active.

Comportements métier à surveiller :

- origine du lot clairement identifiable ;
- historique de transformation et rattachement aux contrôles qualité ;
- génération et régénération de QR code seulement dans les cas autorisés ;
- recherche robuste et résultat neutre si le code est inconnu ;
- documents cohérents avec le lot et la société active ;
- accès strictement limité aux utilisateurs autorisés.

Limite : la confirmation de la permission exacte de régénération et du comportement de recherche sur code inconnu doit être vérifiée au niveau du code métier concerné.

## Vérification exécutée

Commande utilisée avec JDK 21 :

```powershell
mvn test
```

Résultat au commit `510f838` : succès du réacteur Maven complet, 14 modules. Les tests d'intégration PostgreSQL nécessitant une configuration externe restent ignorés conformément à leur configuration.

## Règles de maintenance documentaire

Pour chaque modification fonctionnelle :

1. mettre à jour cette référence avec les classes, états, permissions et tests concernés ;
2. traduire uniquement les conséquences métier dans `docs/formation/` ;
3. ne pas recopier les détails techniques dans le guide client ;
4. signaler les comportements non vérifiés comme limites, jamais comme fonctions disponibles ;
5. conserver les exercices reproductibles sur une société de démonstration.
