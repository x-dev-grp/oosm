# Entity/DTO co-location for remaining OSM 2.0 modules
$ErrorActionPreference = "Stop"
. "$PSScriptRoot\Write-Utf8NoBom.ps1"
$oosm = "C:\OSM 2.0\oosm"

function Move-FeatureFile {
    param(
        [string]$Base,
        [string]$RelativePath,
        [string]$FeatureLayer,
        [string]$OldPackagePattern,
        [string]$ModuleName
    )
    $src = Join-Path $Base $RelativePath
    if (-not (Test-Path $src)) {
        Write-Warning "Skip missing: $src"
        return $false
    }
    $destDir = Join-Path $Base $FeatureLayer
    New-Item -ItemType Directory -Force -Path $destDir | Out-Null
    $dest = Join-Path $destDir (Split-Path $src -Leaf)
    if (Test-Path $dest) {
        Write-Warning "Already exists: $dest"
        return $false
    }
    Move-Item -Path $src -Destination $dest
    $parts = $FeatureLayer -split '\\'
    $feature = $parts[0]
    $layer = $parts[1]
    $newPackage = "com.xdev.ooms.$ModuleName.$feature.$layer"
    $content = Get-Content $dest -Raw -Encoding UTF8
    $content = $content -replace $OldPackagePattern, "package $newPackage;"
    Write-Utf8NoBomFile -Path $dest -Content $content
    Write-Host "Moved: $ModuleName/$RelativePath -> $FeatureLayer"
    return $true
}

function Invoke-ModuleMoves {
    param(
        [string]$ModuleName,
        [hashtable]$Moves,
        [string]$OldPackagePattern
    )
    $base = Join-Path $oosm "modules\$ModuleName\src\main\java\com\xdev\ooms\$ModuleName"
    $count = 0
    foreach ($entry in $Moves.GetEnumerator()) {
        if (Move-FeatureFile -Base $base -RelativePath $entry.Key -FeatureLayer $entry.Value -OldPackagePattern $OldPackagePattern -ModuleName $ModuleName) {
            $count++
        }
    }
    return $count
}

# --- Production cleanup ---
$prodMoves = @{
    "dto\ApiResponse.java" = "common\dto"
}
$prodCount = Invoke-ModuleMoves -ModuleName "production" -Moves $prodMoves -OldPackagePattern 'package com\.xdev\.ooms\.production\.(model|dto|service|controller|repository);'

# --- Finance cleanup ---
$financeMoves = @{
    "service\WasteFinancialService.java" = "financialtransaction\service"
}
$financeCount = Invoke-ModuleMoves -ModuleName "finance" -Moves $financeMoves -OldPackagePattern 'package com\.xdev\.ooms\.finance\.(model|dto|service|controller|repo);'

# --- Conditioning ---
$condMoves = @{
    "model\OrdreFabrication.java" = "ordrefabrication\entity"
    "model\LigneOF.java" = "ordrefabrication\entity"
    "dto\OrdreFabricationDto.java" = "ordrefabrication\dto"
    "dto\LigneOFDto.java" = "ordrefabrication\dto"
    "dto\SaisieProductionDto.java" = "ordrefabrication\dto"
    "dto\AjustementConsommationDto.java" = "ordrefabrication\dto"
    "service\OFService.java" = "ordrefabrication\service"
    "controller\OFController.java" = "ordrefabrication\controller"
    "repository\OrdreFabricationRepository.java" = "ordrefabrication\repository"
    "repository\LigneOFRepository.java" = "ordrefabrication\repository"

    "model\QCControlPoint.java" = "qualitycontrol\entity"
    "model\QCPlan.java" = "qualitycontrol\entity"
    "model\QCResult.java" = "qualitycontrol\entity"
    "dto\QCControlPointDTO.java" = "qualitycontrol\dto"
    "dto\QCPlanDTO.java" = "qualitycontrol\dto"
    "dto\QCResultDTO.java" = "qualitycontrol\dto"
    "service\QCPlanService.java" = "qualitycontrol\service"
    "service\QCResultService.java" = "qualitycontrol\service"
    "controller\QualityController.java" = "qualitycontrol\controller"
    "repository\QCControlPointRepository.java" = "qualitycontrol\repository"
    "repository\QCPlanRepository.java" = "qualitycontrol\repository"
    "repository\QCResultRepository.java" = "qualitycontrol\repository"

    "model\Certification.java" = "certification\entity"
    "dto\CertificationDto.java" = "certification\dto"
    "service\CertificationService.java" = "certification\service"
    "controller\CertificationController.java" = "certification\controller"
    "repository\CertificationRepository.java" = "certification\repository"

    "model\LabelContent.java" = "label\entity"
    "model\LabelSource.java" = "label\entity"
    "service\LabelContentService.java" = "label\service"
    "controller\LabelContentController.java" = "label\controller"
    "repository\LabelContentRepository.java" = "label\repository"
    "repository\LabelSourceRepository.java" = "label\repository"

    "model\OfflineOperation.java" = "sync\entity"
    "dto\SyncRequestDto.java" = "sync\dto"
    "service\SyncService.java" = "sync\service"
    "controller\MobileSyncController.java" = "sync\controller"
    "repository\OfflineOperationRepository.java" = "sync\repository"

    "dto\analytics\BomGapDto.java" = "analytics\dto"
    "dto\analytics\FiltrationReportDto.java" = "analytics\dto"
    "dto\analytics\GlobalOfReportDto.java" = "analytics\dto"
    "dto\analytics\OfAnalyticsProjection.java" = "analytics\dto"
    "dto\analytics\OfYieldDto.java" = "analytics\dto"
    "dto\analytics\QualityReportDto.java" = "analytics\dto"
    "dto\analytics\ReportRequestDto.java" = "analytics\dto"
    "service\AnalyticsService.java" = "analytics\service"
    "controller\AnalyticsController.java" = "analytics\controller"

    "service\AuditService.java" = "audit\service"
    "controller\AuditController.java" = "audit\controller"

    "dto\ArticleSecDto.java" = "inventoryusage\dto"
    "dto\StockSecDto.java" = "inventoryusage\dto"
    "dto\BOMDto.java" = "inventoryusage\dto"
    "dto\BomLineDto.java" = "inventoryusage\dto"
    "dto\ProduitFinalDto.java" = "inventoryusage\dto"
    "dto\LigneConditionnementDto.java" = "inventoryusage\dto"
    "dto\MaterialNeedLineDto.java" = "inventoryusage\dto"
    "dto\AssignableUserDTO.java" = "inventoryusage\dto"
    "service\InventoryUsageService.java" = "inventoryusage\service"
    "controller\InventoryUsageController.java" = "inventoryusage\controller"

    "service\UserContext.java" = "common\service"
    "service\GlobalSearch.java" = "search\service"
    "service\PdfReportService.java" = "reporting\service"

    "expedition\model\Expedition.java" = "expedition\entity"
    "expedition\model\ExpeditionArticle.java" = "expedition\entity"

    "shipping\model\ShippingInfo.java" = "shipping\entity"
    "shipping\model\ShippingEvent.java" = "shipping\entity"
    "shipping\model\ShippingLine.java" = "shipping\entity"
}
$condCount = Invoke-ModuleMoves -ModuleName "conditioning" -Moves $condMoves -OldPackagePattern 'package com\.xdev\.ooms\.conditioning\.(model|dto|service|controller|repository|expedition\.model|shipping\.model)(\.analytics)?;'

# Fix expedition/shipping package patterns separately
$condBase = Join-Path $oosm "modules\conditioning\src\main\java\com\xdev\ooms\conditioning"
@(
    @{ Path = "expedition\entity\Expedition.java"; Package = "com.xdev.ooms.conditioning.expedition.entity" },
    @{ Path = "expedition\entity\ExpeditionArticle.java"; Package = "com.xdev.ooms.conditioning.expedition.entity" },
    @{ Path = "shipping\entity\ShippingInfo.java"; Package = "com.xdev.ooms.conditioning.shipping.entity" },
    @{ Path = "shipping\entity\ShippingEvent.java"; Package = "com.xdev.ooms.conditioning.shipping.entity" },
    @{ Path = "shipping\entity\ShippingLine.java"; Package = "com.xdev.ooms.conditioning.shipping.entity" }
) | ForEach-Object {
    $file = Join-Path $condBase $_.Path
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        $content = $content -replace 'package com\.xdev\.ooms\.conditioning\.(expedition|shipping)\.model;', "package $($_.Package);"
        Write-Utf8NoBomFile -Path $file -Content $content
    }
}

# --- Inventory ---
$invMoves = @{
    "entity\ArticleSec.java" = "articlesec\entity"
    "dto\ArticleSecDto.java" = "articlesec\dto"
    "dto\ArticleStockSummaryDto.java" = "articlesec\dto"
    "dto\ArticleCritiqueDto.java" = "articlesec\dto"
    "service\ArticleSecService.java" = "articlesec\service"
    "controller\ArticleSecController.java" = "articlesec\controller"
    "repository\ArticleSecRepository.java" = "articlesec\repository"

    "entity\StockSec.java" = "stocksec\entity"
    "entity\MouvementStockSec.java" = "stocksec\entity"
    "dto\StockSecDto.java" = "stocksec\dto"
    "dto\MouvementStockSecDto.java" = "stocksec\dto"
    "dto\MouvementRecentDto.java" = "stocksec\dto"
    "dto\StockDashboardPayloadDto.java" = "stocksec\dto"
    "service\StockSecService.java" = "stocksec\service"
    "controller\StockSecController.java" = "stocksec\controller"
    "repository\StockSecRepository.java" = "stocksec\repository"
    "repository\MouvementStockSecRepository.java" = "stocksec\repository"

    "entity\ProduitFinal.java" = "produitfinal\entity"
    "dto\ProduitFinalDto.java" = "produitfinal\dto"
    "service\ProduitFinalService.java" = "produitfinal\service"
    "controller\ProduitFinalController.java" = "produitfinal\controller"
    "repository\ProduitFinalRepository.java" = "produitfinal\repository"

    "entity\Fournisseur.java" = "fournisseur\entity"
    "dto\FournisseurDto.java" = "fournisseur\dto"
    "service\FournisseurService.java" = "fournisseur\service"
    "controller\FournisseurController.java" = "fournisseur\controller"
    "repository\FournisseurRepository.java" = "fournisseur\repository"

    "entity\EmplacementStock.java" = "emplacementstock\entity"
    "dto\EmplacementStockDto.java" = "emplacementstock\dto"
    "service\EmplacementStockService.java" = "emplacementstock\service"
    "controller\EmplacementStockController.java" = "emplacementstock\controller"
    "repository\EmplacementStockRepository.java" = "emplacementstock\repository"

    "entity\BOM.java" = "bom\entity"
    "entity\BomLine.java" = "bom\entity"
    "dto\BOMDto.java" = "bom\dto"
    "dto\BomLineDto.java" = "bom\dto"
    "service\BomService.java" = "bom\service"
    "controller\BomController.java" = "bom\controller"
    "repository\BomRepository.java" = "bom\repository"
    "repository\BomLineRepository.java" = "bom\repository"

    "entity\BonCommande.java" = "boncommande\entity"
    "entity\LigneBonCommande.java" = "boncommande\entity"
    "dto\BonCommandeDto.java" = "boncommande\dto"
    "dto\LigneBonCommandeDto.java" = "boncommande\dto"
    "service\BonCommandeService.java" = "boncommande\service"
    "controller\BonCommandeController.java" = "boncommande\controller"
    "repository\BonCommandeRepository.java" = "boncommande\repository"
    "repository\LigneBonCommandeRepository.java" = "boncommande\repository"

    "entity\LigneConditionnement.java" = "ligneconditionnement\entity"
    "dto\LigneConditionnementDto.java" = "ligneconditionnement\dto"
    "service\LigneConditionnementService.java" = "ligneconditionnement\service"
    "controller\LigneConditionnementController.java" = "ligneconditionnement\controller"
    "repository\LigneConditionnementRepository.java" = "ligneconditionnement\repository"

    "dto\StatistiquesDTO.java" = "statistique\dto"
    "service\StatistiqueService.java" = "statistique\service"
    "controller\StatistiqueController.java" = "statistique\controller"

    "dto\MaterialNeedLineDto.java" = "materialneeds\dto"
    "service\MaterialNeedsService.java" = "materialneeds\service"

    "service\AuditService.java" = "audit\service"
    "controller\AuditController.java" = "audit\controller"

    "service\InventoryDeleteGuardService.java" = "common\service"
}
$invCount = Invoke-ModuleMoves -ModuleName "inventory" -Moves $invMoves -OldPackagePattern 'package com\.xdev\.ooms\.inventory\.(entity|dto|service|controller|repository);'

# --- HR ---
$hrMoves = @{
    "model\Employee.java" = "employee\entity"
    "Dtos\EmployeeDto.java" = "employee\dto"
    "service\EmployeeService.java" = "employee\service"
    "controller\EmployeeController.java" = "employee\controller"
    "repo\EmployeeRepository.java" = "employee\repository"

    "model\Department.java" = "department\entity"
    "Dtos\DepartmentDto.java" = "department\dto"
    "service\departementService.java" = "department\service"
    "controller\DepartementController.java" = "department\controller"
    "repo\DepartementRepository.java" = "department\repository"

    "model\Contract.java" = "contract\entity"
    "Dtos\ContractDto.java" = "contract\dto"
    "service\ContractService.java" = "contract\service"
    "controller\ContractController.java" = "contract\controller"
    "repo\ContractRepository.java" = "contract\repository"

    "model\LeaveRequest.java" = "leave\entity"
    "Dtos\LeaveRequestDto.java" = "leave\dto"
    "service\LeaveRequestService.java" = "leave\service"
    "controller\LeaveRequestController.java" = "leave\controller"
    "repo\LeaveRequestRepository.java" = "leave\repository"

    "model\PayRolls.java" = "payroll\entity"
    "Dtos\PayRollsDto.java" = "payroll\dto"
    "service\PayRollsService.java" = "payroll\service"
    "controller\PayRollsController.java" = "payroll\controller"
    "repo\PayRollsRepository.java" = "payroll\repository"

    "model\Pointage.java" = "pointage\entity"
    "Dtos\PointageDto.java" = "pointage\dto"
    "service\PointageService.java" = "pointage\service"
    "controller\PointageController.java" = "pointage\controller"
    "repo\PointageRepository.java" = "pointage\repository"

    "model\Poste.java" = "poste\entity"
    "Dtos\PosteDto.java" = "poste\dto"
    "service\PosteService.java" = "poste\service"
    "controller\PosteController.java" = "poste\controller"
    "repo\PosteRepository.java" = "poste\repository"
}
$hrCount = Invoke-ModuleMoves -ModuleName "hr" -Moves $hrMoves -OldPackagePattern 'package com\.xdev\.ooms\.hr\.(model|Dtos|service|controller|repo);'

# --- Security userManagement features ---
$secBase = Join-Path $oosm "modules\security\src\main\java\com\xdev\ooms\security"
$secMoves = @{
    "userManagement\models\OSMUser.java" = "user\entity"
    "userManagement\dtos\OUTDTO\OSMUserDTO.java" = "user\dto"
    "userManagement\dtos\OUTDTO\OSMUserOUTDTO.java" = "user\dto"
    "userManagement\dtos\OUTDTO\AssignableUserDTO.java" = "user\dto"
    "userManagement\dtos\OUTDTO\UpdatePasswordDTO.java" = "user\dto"
    "userManagement\service\UserService.java" = "user\service"
    "userManagement\controller\UserController.java" = "user\controller"
    "userManagement\data\UserRepository.java" = "user\repository"

    "userManagement\models\Role.java" = "role\entity"
    "userManagement\dtos\OUTDTO\RoleDTO.java" = "role\dto"
    "userManagement\service\RoleService.java" = "role\service"
    "userManagement\controller\RoleController.java" = "role\controller"
    "userManagement\data\RoleRepository.java" = "role\repository"

    "userManagement\models\Permission.java" = "permission\entity"
    "userManagement\dtos\OUTDTO\PermissionDTO.java" = "permission\dto"
    "userManagement\service\PermissionService.java" = "permission\service"
    "userManagement\controller\PermissionController.java" = "permission\controller"
    "userManagement\data\PermissionRepository.java" = "permission\repository"

    "userManagement\models\CompanyProfile.java" = "companyprofile\entity"
    "userManagement\dtos\OUTDTO\CompanyProfileDTO.java" = "companyprofile\dto"
    "userManagement\dtos\OUTDTO\CompanyUserDTO.java" = "companyprofile\dto"
    "userManagement\service\CompanyProfileService.java" = "companyprofile\service"
    "userManagement\controller\CompanyProfileController.java" = "companyprofile\controller"
    "userManagement\data\CompanyProfileRepository.java" = "companyprofile\repository"

    "userManagement\models\ConfirmationCode.java" = "confirmationcode\entity"
    "userManagement\models\enums\ConfirmationMethod.java" = "confirmationcode\enums"
    "userManagement\models\enums\ConfirmationCodeType.java" = "confirmationcode\enums"
    "userManagement\dtos\OUTDTO\ConfirmationCodeDTO.java" = "confirmationcode\dto"
    "userManagement\dtos\OUTDTO\VerifyOtpAndSetPasswordRequest.java" = "confirmationcode\dto"
    "userManagement\service\ConfirmationCodeService.java" = "confirmationcode\service"
    "userManagement\data\ConfirmationCodeRepository.java" = "confirmationcode\repository"
}
$secCount = 0
foreach ($entry in $secMoves.GetEnumerator()) {
    $src = Join-Path $secBase $entry.Key
    if (-not (Test-Path $src)) { continue }
    $destDir = Join-Path $secBase $entry.Value
    New-Item -ItemType Directory -Force -Path $destDir | Out-Null
    $dest = Join-Path $destDir (Split-Path $src -Leaf)
    if (Test-Path $dest) { continue }
    Move-Item -Path $src -Destination $dest
    $parts = $entry.Value -split '\\'
    $feature = $parts[0]
    $layer = $parts[1]
    $newPackage = "com.xdev.ooms.security.$feature.$layer"
    $content = Get-Content $dest -Raw -Encoding UTF8
    $content = $content -replace 'package com\.xdev\.ooms\.security\.userManagement\.(models(\.enums)?|dtos\.OUTDTO|service|controller|data);', "package $newPackage;"
    Write-Utf8NoBomFile -Path $dest -Content $content
    $secCount++
    Write-Host "Moved security: $($entry.Key) -> $($entry.Value)"
}

# Security authorization entity
$authMoves = @{
    "securityConfig\entities\Authorization.java" = "authorization\entity"
    "securityConfig\data\AuthorizationRepository.java" = "authorization\repository"
}
foreach ($entry in $authMoves.GetEnumerator()) {
    $src = Join-Path $secBase $entry.Key
    if (-not (Test-Path $src)) { continue }
    $destDir = Join-Path $secBase $entry.Value
    New-Item -ItemType Directory -Force -Path $destDir | Out-Null
    $dest = Join-Path $destDir (Split-Path $src -Leaf)
    if (Test-Path $dest) { continue }
    Move-Item -Path $src -Destination $dest
    $parts = $entry.Value -split '\\'
    $newPackage = "com.xdev.ooms.security.$($parts[0]).$($parts[1])"
    $content = Get-Content $dest -Raw -Encoding UTF8
    $content = $content -replace 'package com\.xdev\.ooms\.security\.securityConfig\.(entities|data);', "package $newPackage;"
    Write-Utf8NoBomFile -Path $dest -Content $content
    $secCount++
}

Write-Host "`nMove counts: production=$prodCount finance=$financeCount conditioning=$condCount inventory=$invCount hr=$hrCount security=$secCount"
