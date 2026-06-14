# Production module entity/DTO co-location migration
$ErrorActionPreference = "Stop"
. "$PSScriptRoot\Write-Utf8NoBom.ps1"
$base = "C:\OSM 2.0\oosm\modules\production\src\main\java\com\xdev\ooms\production"

# file -> feature/subdir mapping (subdir: entity|dto|service|controller|repository|enums)
$moves = @{
    "model\BaseType.java" = "basetype\entity"
    "dto\BaseTypeDto.java" = "basetype\dto"
    "service\GenericTypeService.java" = "basetype\service"
    "controller\GenericTypeController.java" = "basetype\controller"
    "repository\GenericRepository.java" = "basetype\repository"

    "model\OilTransaction.java" = "oiltransaction\entity"
    "dto\OilTransactionDTO.java" = "oiltransaction\dto"
    "service\OilTransactionService.java" = "oiltransaction\service"
    "controller\OilTransactionController.java" = "oiltransaction\controller"
    "repository\OilTransactionRepository.java" = "oiltransaction\repository"

    "model\UnifiedDelivery.java" = "unifieddelivery\entity"
    "dto\UnifiedDeliveryDTO.java" = "unifieddelivery\dto"
    "dto\DeliveryWithNextIdDto.java" = "unifieddelivery\dto"
    "dto\ExchangePricingDto.java" = "unifieddelivery\dto"
    "dto\PaymentDTO.java" = "unifieddelivery\dto"
    "service\UnifiedDeliveryService.java" = "unifieddelivery\service"
    "controller\UnifiedDeliveryController.java" = "unifieddelivery\controller"
    "repository\DeliveryRepository.java" = "unifieddelivery\repository"

    "model\StorageUnit.java" = "storageunit\entity"
    "dto\StorageUnitDto.java" = "storageunit\dto"
    "service\StorageUnitService.java" = "storageunit\service"
    "controller\StorageUnitController.java" = "storageunit\controller"
    "repository\StorageUnitRepo.java" = "storageunit\repository"

    "model\Supplier.java" = "supplier\entity"
    "dto\SupplierDto.java" = "supplier\dto"
    "service\SupplierTypeService.java" = "supplier\service"
    "controller\SupplierTypeController.java" = "supplier\controller"
    "repository\SupplierRepository.java" = "supplier\repository"

    "model\Waste.java" = "waste\entity"
    "dto\WasteDTO.java" = "waste\dto"
    "service\WasteService.java" = "waste\service"
    "controller\WasteController.java" = "waste\controller"
    "repository\WasteRepository.java" = "waste\repository"

    "model\OilSale.java" = "oilsale\entity"
    "dto\OilSaleDTO.java" = "oilsale\dto"
    "dto\OilSaleCreateRequest.java" = "oilsale\dto"
    "service\OilSaleService.java" = "oilsale\service"
    "controller\OilSaleController.java" = "oilsale\controller"
    "repository\OilSaleRepository.java" = "oilsale\repository"

    "model\OilContainer.java" = "oilcontainer\entity"
    "dto\OilContainerDTO.java" = "oilcontainer\dto"
    "service\OilContainerService.java" = "oilcontainer\service"
    "controller\OilContainerController.java" = "oilcontainer\controller"
    "repository\OilContainerRepository.java" = "oilcontainer\repository"

    "model\OilContainerSale.java" = "oilcontainersale\entity"
    "dto\OilContainerSaleDto.java" = "oilcontainersale\dto"
    "service\OilContainerSalesService.java" = "oilcontainersale\service"
    "controller\OilContainerSalesController.java" = "oilcontainersale\controller"
    "repository\OilContainerSaleRepo.java" = "oilcontainersale\repository"

    "model\MillMachine.java" = "millmachine\entity"
    "dto\MillMachineDto.java" = "millmachine\dto"
    "service\MillMachineService.java" = "millmachine\service"
    "controller\MillMachineController.java" = "millmachine\controller"
    "repository\MillMachineRepository.java" = "millmachine\repository"

    "model\MachinePlan.java" = "machineplan\entity"
    "dto\MachinePlanDto.java" = "machineplan\dto"
    "service\MachinePlanService.java" = "machineplan\service"
    "repository\MachinePlanRepository.java" = "machineplan\repository"

    "dto\MillPlanDTO.java" = "planning\dto"
    "dto\PlanItemDTO.java" = "planning\dto"
    "dto\PlanItemType.java" = "planning\dto"
    "dto\PlanningSaveRequest.java" = "planning\dto"
    "service\PlanningService.java" = "planning\service"
    "controller\PlanningController.java" = "planning\controller"

    "model\Transporter.java" = "transporter\entity"
    "dto\TransporterDTO.java" = "transporter\dto"
    "service\TransporterService.java" = "transporter\service"
    "controller\TransporterController.java" = "transporter\controller"
    "repository\TransporterRepository.java" = "transporter\repository"

    "model\Parameter.java" = "parameter\entity"
    "dto\ParameterDto.java" = "parameter\dto"
    "service\ParameterService.java" = "parameter\service"
    "controller\ParameterController.java" = "parameter\controller"
    "repository\ParameterRepo.java" = "parameter\repository"

    "model\QualityControlResult.java" = "qualitycontrol\entity"
    "model\QualityControlRule.java" = "qualitycontrol\entity"
    "dto\QualityControlResultDto.java" = "qualitycontrol\dto"
    "dto\QualityControlRuleDto.java" = "qualitycontrol\dto"
    "service\QualityControlResultService.java" = "qualitycontrol\service"
    "service\QualityControlRuleService.java" = "qualitycontrol\service"
    "controller\QualityControlResultController.java" = "qualitycontrol\controller"
    "controller\QualityControlRuleController.java" = "qualitycontrol\controller"
    "repository\QualityControlResultRepository.java" = "qualitycontrol\repository"
    "repository\QualityControlRuleRepository.java" = "qualitycontrol\repository"

    "model\FiltrationOperation.java" = "filtration\entity"
    "dto\FiltrationRequestDto.java" = "filtration\dto"
    "dto\FiltrationResultDto.java" = "filtration\dto"
    "dto\FiltrationStatus.java" = "filtration\dto"
    "dto\FiltrationStepDto.java" = "filtration\dto"
    "dto\FiltrationOperationDto.java" = "filtration\dto"
    "dto\FiltrationAnalyticsDto.java" = "filtration\dto"
    "dto\FiltrationCompletionDto.java" = "filtration\dto"
    "dto\UpdateFiltrationStatusDto.java" = "filtration\dto"
    "service\FiltrationService.java" = "filtration\service"
    "controller\FiltrationController.java" = "filtration\controller"
    "repository\FiltrationOperationRepo.java" = "filtration\repository"

    "model\TraceabilityLot.java" = "genealogy\entity"
    "model\TraceabilitySourceType.java" = "genealogy\enums"
    "dto\GenealogyDto.java" = "genealogy\dto"
    "dto\RootSourceDto.java" = "genealogy\dto"
    "dto\GlobalLotDto.java" = "genealogy\dto"
    "dto\IntakeStepDto.java" = "genealogy\dto"
    "dto\SyncRequestDTO.java" = "genealogy\dto"
    "dto\SyncStatisticsDTO.java" = "genealogy\dto"
    "service\GenealogyService.java" = "genealogy\service"
    "service\TraceabilityLotService.java" = "genealogy\service"
    "controller\GenealogyController.java" = "genealogy\controller"
    "repository\TraceabilityLotRepository.java" = "genealogy\repository"

    "service\ProdAnalyticsService.java" = "analytics\service"
    "controller\AnalyticsController.java" = "analytics\controller"
}

foreach ($entry in $moves.GetEnumerator()) {
    $src = Join-Path $base $entry.Key
    $destDir = Join-Path $base $entry.Value
    if (-not (Test-Path $src)) {
        Write-Warning "Skip missing: $src"
        continue
    }
    New-Item -ItemType Directory -Force -Path $destDir | Out-Null
    $dest = Join-Path $destDir (Split-Path $src -Leaf)
    if (Test-Path $dest) {
        Write-Warning "Already exists: $dest"
        continue
    }
    Move-Item -Path $src -Destination $dest
    Write-Host "Moved: $($entry.Key) -> $($entry.Value)"
}

# Update package declarations in moved files
foreach ($entry in $moves.GetEnumerator()) {
    $destDir = Join-Path $base $entry.Value
    $fileName = Split-Path $entry.Key -Leaf
    $dest = Join-Path $destDir $fileName
    if (-not (Test-Path $dest)) { continue }

    $parts = $entry.Value -split '\\'
    $feature = $parts[0]
    $layer = $parts[1]
    $newPackage = "com.xdev.ooms.production.$feature.$layer"

    $content = Get-Content $dest -Raw -Encoding UTF8
    $content = $content -replace 'package com\.xdev\.ooms\.production\.(model|dto|service|controller|repository);', "package $newPackage;"
    Write-Utf8NoBomFile -Path $dest -Content $content
}

Write-Host "File moves and package updates complete."
