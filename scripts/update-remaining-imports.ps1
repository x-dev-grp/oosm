# Bulk import updates after remaining module co-location
$ErrorActionPreference = "Stop"
. "$PSScriptRoot\Write-Utf8NoBom.ps1"
$root = "C:\OSM 2.0\oosm"

$replacements = [ordered]@{
    # Production
    "com.xdev.ooms.production.dto.ApiResponse" = "com.xdev.ooms.production.common.dto.ApiResponse"

    # Finance
    "com.xdev.ooms.finance.service.WasteFinancialService" = "com.xdev.ooms.finance.financialtransaction.service.WasteFinancialService"

    # Conditioning - ordrefabrication
    "com.xdev.ooms.conditioning.model.OrdreFabrication" = "com.xdev.ooms.conditioning.ordrefabrication.entity.OrdreFabrication"
    "com.xdev.ooms.conditioning.model.LigneOF" = "com.xdev.ooms.conditioning.ordrefabrication.entity.LigneOF"
    "com.xdev.ooms.conditioning.dto.OrdreFabricationDto" = "com.xdev.ooms.conditioning.ordrefabrication.dto.OrdreFabricationDto"
    "com.xdev.ooms.conditioning.dto.LigneOFDto" = "com.xdev.ooms.conditioning.ordrefabrication.dto.LigneOFDto"
    "com.xdev.ooms.conditioning.dto.SaisieProductionDto" = "com.xdev.ooms.conditioning.ordrefabrication.dto.SaisieProductionDto"
    "com.xdev.ooms.conditioning.dto.AjustementConsommationDto" = "com.xdev.ooms.conditioning.ordrefabrication.dto.AjustementConsommationDto"
    "com.xdev.ooms.conditioning.service.OFService" = "com.xdev.ooms.conditioning.ordrefabrication.service.OFService"
    "com.xdev.ooms.conditioning.controller.OFController" = "com.xdev.ooms.conditioning.ordrefabrication.controller.OFController"
    "com.xdev.ooms.conditioning.repository.OrdreFabricationRepository" = "com.xdev.ooms.conditioning.ordrefabrication.repository.OrdreFabricationRepository"
    "com.xdev.ooms.conditioning.repository.LigneOFRepository" = "com.xdev.ooms.conditioning.ordrefabrication.repository.LigneOFRepository"

    # Conditioning - qualitycontrol
    "com.xdev.ooms.conditioning.model.QCControlPoint" = "com.xdev.ooms.conditioning.qualitycontrol.entity.QCControlPoint"
    "com.xdev.ooms.conditioning.model.QCPlan" = "com.xdev.ooms.conditioning.qualitycontrol.entity.QCPlan"
    "com.xdev.ooms.conditioning.model.QCResult" = "com.xdev.ooms.conditioning.qualitycontrol.entity.QCResult"
    "com.xdev.ooms.conditioning.dto.QCControlPointDTO" = "com.xdev.ooms.conditioning.qualitycontrol.dto.QCControlPointDTO"
    "com.xdev.ooms.conditioning.dto.QCPlanDTO" = "com.xdev.ooms.conditioning.qualitycontrol.dto.QCPlanDTO"
    "com.xdev.ooms.conditioning.dto.QCResultDTO" = "com.xdev.ooms.conditioning.qualitycontrol.dto.QCResultDTO"
    "com.xdev.ooms.conditioning.service.QCPlanService" = "com.xdev.ooms.conditioning.qualitycontrol.service.QCPlanService"
    "com.xdev.ooms.conditioning.service.QCResultService" = "com.xdev.ooms.conditioning.qualitycontrol.service.QCResultService"
    "com.xdev.ooms.conditioning.controller.QualityController" = "com.xdev.ooms.conditioning.qualitycontrol.controller.QualityController"
    "com.xdev.ooms.conditioning.repository.QCControlPointRepository" = "com.xdev.ooms.conditioning.qualitycontrol.repository.QCControlPointRepository"
    "com.xdev.ooms.conditioning.repository.QCPlanRepository" = "com.xdev.ooms.conditioning.qualitycontrol.repository.QCPlanRepository"
    "com.xdev.ooms.conditioning.repository.QCResultRepository" = "com.xdev.ooms.conditioning.qualitycontrol.repository.QCResultRepository"

    # Conditioning - certification
    "com.xdev.ooms.conditioning.model.Certification" = "com.xdev.ooms.conditioning.certification.entity.Certification"
    "com.xdev.ooms.conditioning.dto.CertificationDto" = "com.xdev.ooms.conditioning.certification.dto.CertificationDto"
    "com.xdev.ooms.conditioning.service.CertificationService" = "com.xdev.ooms.conditioning.certification.service.CertificationService"
    "com.xdev.ooms.conditioning.controller.CertificationController" = "com.xdev.ooms.conditioning.certification.controller.CertificationController"
    "com.xdev.ooms.conditioning.repository.CertificationRepository" = "com.xdev.ooms.conditioning.certification.repository.CertificationRepository"

    # Conditioning - label
    "com.xdev.ooms.conditioning.model.LabelContent" = "com.xdev.ooms.conditioning.label.entity.LabelContent"
    "com.xdev.ooms.conditioning.model.LabelSource" = "com.xdev.ooms.conditioning.label.entity.LabelSource"
    "com.xdev.ooms.conditioning.service.LabelContentService" = "com.xdev.ooms.conditioning.label.service.LabelContentService"
    "com.xdev.ooms.conditioning.controller.LabelContentController" = "com.xdev.ooms.conditioning.label.controller.LabelContentController"
    "com.xdev.ooms.conditioning.repository.LabelContentRepository" = "com.xdev.ooms.conditioning.label.repository.LabelContentRepository"
    "com.xdev.ooms.conditioning.repository.LabelSourceRepository" = "com.xdev.ooms.conditioning.label.repository.LabelSourceRepository"

    # Conditioning - sync
    "com.xdev.ooms.conditioning.model.OfflineOperation" = "com.xdev.ooms.conditioning.sync.entity.OfflineOperation"
    "com.xdev.ooms.conditioning.dto.SyncRequestDto" = "com.xdev.ooms.conditioning.sync.dto.SyncRequestDto"
    "com.xdev.ooms.conditioning.service.SyncService" = "com.xdev.ooms.conditioning.sync.service.SyncService"
    "com.xdev.ooms.conditioning.controller.MobileSyncController" = "com.xdev.ooms.conditioning.sync.controller.MobileSyncController"
    "com.xdev.ooms.conditioning.repository.OfflineOperationRepository" = "com.xdev.ooms.conditioning.sync.repository.OfflineOperationRepository"

    # Conditioning - analytics
    "com.xdev.ooms.conditioning.dto.analytics.BomGapDto" = "com.xdev.ooms.conditioning.analytics.dto.BomGapDto"
    "com.xdev.ooms.conditioning.dto.analytics.FiltrationReportDto" = "com.xdev.ooms.conditioning.analytics.dto.FiltrationReportDto"
    "com.xdev.ooms.conditioning.dto.analytics.GlobalOfReportDto" = "com.xdev.ooms.conditioning.analytics.dto.GlobalOfReportDto"
    "com.xdev.ooms.conditioning.dto.analytics.OfAnalyticsProjection" = "com.xdev.ooms.conditioning.analytics.dto.OfAnalyticsProjection"
    "com.xdev.ooms.conditioning.dto.analytics.OfYieldDto" = "com.xdev.ooms.conditioning.analytics.dto.OfYieldDto"
    "com.xdev.ooms.conditioning.dto.analytics.QualityReportDto" = "com.xdev.ooms.conditioning.analytics.dto.QualityReportDto"
    "com.xdev.ooms.conditioning.dto.analytics.ReportRequestDto" = "com.xdev.ooms.conditioning.analytics.dto.ReportRequestDto"
    "com.xdev.ooms.conditioning.service.AnalyticsService" = "com.xdev.ooms.conditioning.analytics.service.AnalyticsService"
    "com.xdev.ooms.conditioning.controller.AnalyticsController" = "com.xdev.ooms.conditioning.analytics.controller.AnalyticsController"

    # Conditioning - audit
    "com.xdev.ooms.conditioning.service.AuditService" = "com.xdev.ooms.conditioning.audit.service.AuditService"
    "com.xdev.ooms.conditioning.controller.AuditController" = "com.xdev.ooms.conditioning.audit.controller.AuditController"

    # Conditioning - inventoryusage
    "com.xdev.ooms.conditioning.dto.ArticleSecDto" = "com.xdev.ooms.conditioning.inventoryusage.dto.ArticleSecDto"
    "com.xdev.ooms.conditioning.dto.StockSecDto" = "com.xdev.ooms.conditioning.inventoryusage.dto.StockSecDto"
    "com.xdev.ooms.conditioning.dto.BOMDto" = "com.xdev.ooms.conditioning.inventoryusage.dto.BOMDto"
    "com.xdev.ooms.conditioning.dto.BomLineDto" = "com.xdev.ooms.conditioning.inventoryusage.dto.BomLineDto"
    "com.xdev.ooms.conditioning.dto.ProduitFinalDto" = "com.xdev.ooms.conditioning.inventoryusage.dto.ProduitFinalDto"
    "com.xdev.ooms.conditioning.dto.LigneConditionnementDto" = "com.xdev.ooms.conditioning.inventoryusage.dto.LigneConditionnementDto"
    "com.xdev.ooms.conditioning.dto.MaterialNeedLineDto" = "com.xdev.ooms.conditioning.inventoryusage.dto.MaterialNeedLineDto"
    "com.xdev.ooms.conditioning.dto.AssignableUserDTO" = "com.xdev.ooms.conditioning.inventoryusage.dto.AssignableUserDTO"
    "com.xdev.ooms.conditioning.service.InventoryUsageService" = "com.xdev.ooms.conditioning.inventoryusage.service.InventoryUsageService"
    "com.xdev.ooms.conditioning.controller.InventoryUsageController" = "com.xdev.ooms.conditioning.inventoryusage.controller.InventoryUsageController"

    # Conditioning - common/search/reporting
    "com.xdev.ooms.conditioning.service.UserContext" = "com.xdev.ooms.conditioning.common.service.UserContext"
    "com.xdev.ooms.conditioning.service.GlobalSearch" = "com.xdev.ooms.conditioning.search.service.GlobalSearch"
    "com.xdev.ooms.conditioning.service.PdfReportService" = "com.xdev.ooms.conditioning.reporting.service.PdfReportService"

    # Conditioning - expedition/shipping model -> entity
    "com.xdev.ooms.conditioning.expedition.model.Expedition" = "com.xdev.ooms.conditioning.expedition.entity.Expedition"
    "com.xdev.ooms.conditioning.expedition.model.ExpeditionArticle" = "com.xdev.ooms.conditioning.expedition.entity.ExpeditionArticle"
    "com.xdev.ooms.conditioning.shipping.model.ShippingInfo" = "com.xdev.ooms.conditioning.shipping.entity.ShippingInfo"
    "com.xdev.ooms.conditioning.shipping.model.ShippingEvent" = "com.xdev.ooms.conditioning.shipping.entity.ShippingEvent"
    "com.xdev.ooms.conditioning.shipping.model.ShippingLine" = "com.xdev.ooms.conditioning.shipping.entity.ShippingLine"

    # Inventory
    "com.xdev.ooms.inventory.entity.ArticleSec" = "com.xdev.ooms.inventory.articlesec.entity.ArticleSec"
    "com.xdev.ooms.inventory.dto.ArticleSecDto" = "com.xdev.ooms.inventory.articlesec.dto.ArticleSecDto"
    "com.xdev.ooms.inventory.dto.ArticleStockSummaryDto" = "com.xdev.ooms.inventory.articlesec.dto.ArticleStockSummaryDto"
    "com.xdev.ooms.inventory.dto.ArticleCritiqueDto" = "com.xdev.ooms.inventory.articlesec.dto.ArticleCritiqueDto"
    "com.xdev.ooms.inventory.service.ArticleSecService" = "com.xdev.ooms.inventory.articlesec.service.ArticleSecService"
    "com.xdev.ooms.inventory.controller.ArticleSecController" = "com.xdev.ooms.inventory.articlesec.controller.ArticleSecController"
    "com.xdev.ooms.inventory.repository.ArticleSecRepository" = "com.xdev.ooms.inventory.articlesec.repository.ArticleSecRepository"

    "com.xdev.ooms.inventory.entity.StockSec" = "com.xdev.ooms.inventory.stocksec.entity.StockSec"
    "com.xdev.ooms.inventory.entity.MouvementStockSec" = "com.xdev.ooms.inventory.stocksec.entity.MouvementStockSec"
    "com.xdev.ooms.inventory.dto.StockSecDto" = "com.xdev.ooms.inventory.stocksec.dto.StockSecDto"
    "com.xdev.ooms.inventory.dto.MouvementStockSecDto" = "com.xdev.ooms.inventory.stocksec.dto.MouvementStockSecDto"
    "com.xdev.ooms.inventory.dto.MouvementRecentDto" = "com.xdev.ooms.inventory.stocksec.dto.MouvementRecentDto"
    "com.xdev.ooms.inventory.dto.StockDashboardPayloadDto" = "com.xdev.ooms.inventory.stocksec.dto.StockDashboardPayloadDto"
    "com.xdev.ooms.inventory.service.StockSecService" = "com.xdev.ooms.inventory.stocksec.service.StockSecService"
    "com.xdev.ooms.inventory.controller.StockSecController" = "com.xdev.ooms.inventory.stocksec.controller.StockSecController"
    "com.xdev.ooms.inventory.repository.StockSecRepository" = "com.xdev.ooms.inventory.stocksec.repository.StockSecRepository"
    "com.xdev.ooms.inventory.repository.MouvementStockSecRepository" = "com.xdev.ooms.inventory.stocksec.repository.MouvementStockSecRepository"

    "com.xdev.ooms.inventory.entity.ProduitFinal" = "com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal"
    "com.xdev.ooms.inventory.dto.ProduitFinalDto" = "com.xdev.ooms.inventory.produitfinal.dto.ProduitFinalDto"
    "com.xdev.ooms.inventory.service.ProduitFinalService" = "com.xdev.ooms.inventory.produitfinal.service.ProduitFinalService"
    "com.xdev.ooms.inventory.controller.ProduitFinalController" = "com.xdev.ooms.inventory.produitfinal.controller.ProduitFinalController"
    "com.xdev.ooms.inventory.repository.ProduitFinalRepository" = "com.xdev.ooms.inventory.produitfinal.repository.ProduitFinalRepository"

    "com.xdev.ooms.inventory.entity.Fournisseur" = "com.xdev.ooms.inventory.fournisseur.entity.Fournisseur"
    "com.xdev.ooms.inventory.dto.FournisseurDto" = "com.xdev.ooms.inventory.fournisseur.dto.FournisseurDto"
    "com.xdev.ooms.inventory.service.FournisseurService" = "com.xdev.ooms.inventory.fournisseur.service.FournisseurService"
    "com.xdev.ooms.inventory.controller.FournisseurController" = "com.xdev.ooms.inventory.fournisseur.controller.FournisseurController"
    "com.xdev.ooms.inventory.repository.FournisseurRepository" = "com.xdev.ooms.inventory.fournisseur.repository.FournisseurRepository"

    "com.xdev.ooms.inventory.entity.EmplacementStock" = "com.xdev.ooms.inventory.emplacementstock.entity.EmplacementStock"
    "com.xdev.ooms.inventory.dto.EmplacementStockDto" = "com.xdev.ooms.inventory.emplacementstock.dto.EmplacementStockDto"
    "com.xdev.ooms.inventory.service.EmplacementStockService" = "com.xdev.ooms.inventory.emplacementstock.service.EmplacementStockService"
    "com.xdev.ooms.inventory.controller.EmplacementStockController" = "com.xdev.ooms.inventory.emplacementstock.controller.EmplacementStockController"
    "com.xdev.ooms.inventory.repository.EmplacementStockRepository" = "com.xdev.ooms.inventory.emplacementstock.repository.EmplacementStockRepository"

    "com.xdev.ooms.inventory.entity.BOM" = "com.xdev.ooms.inventory.bom.entity.BOM"
    "com.xdev.ooms.inventory.entity.BomLine" = "com.xdev.ooms.inventory.bom.entity.BomLine"
    "com.xdev.ooms.inventory.dto.BOMDto" = "com.xdev.ooms.inventory.bom.dto.BOMDto"
    "com.xdev.ooms.inventory.dto.BomLineDto" = "com.xdev.ooms.inventory.bom.dto.BomLineDto"
    "com.xdev.ooms.inventory.service.BomService" = "com.xdev.ooms.inventory.bom.service.BomService"
    "com.xdev.ooms.inventory.controller.BomController" = "com.xdev.ooms.inventory.bom.controller.BomController"
    "com.xdev.ooms.inventory.repository.BomRepository" = "com.xdev.ooms.inventory.bom.repository.BomRepository"
    "com.xdev.ooms.inventory.repository.BomLineRepository" = "com.xdev.ooms.inventory.bom.repository.BomLineRepository"

    "com.xdev.ooms.inventory.entity.BonCommande" = "com.xdev.ooms.inventory.boncommande.entity.BonCommande"
    "com.xdev.ooms.inventory.entity.LigneBonCommande" = "com.xdev.ooms.inventory.boncommande.entity.LigneBonCommande"
    "com.xdev.ooms.inventory.dto.BonCommandeDto" = "com.xdev.ooms.inventory.boncommande.dto.BonCommandeDto"
    "com.xdev.ooms.inventory.dto.LigneBonCommandeDto" = "com.xdev.ooms.inventory.boncommande.dto.LigneBonCommandeDto"
    "com.xdev.ooms.inventory.service.BonCommandeService" = "com.xdev.ooms.inventory.boncommande.service.BonCommandeService"
    "com.xdev.ooms.inventory.controller.BonCommandeController" = "com.xdev.ooms.inventory.boncommande.controller.BonCommandeController"
    "com.xdev.ooms.inventory.repository.BonCommandeRepository" = "com.xdev.ooms.inventory.boncommande.repository.BonCommandeRepository"
    "com.xdev.ooms.inventory.repository.LigneBonCommandeRepository" = "com.xdev.ooms.inventory.boncommande.repository.LigneBonCommandeRepository"

    "com.xdev.ooms.inventory.entity.LigneConditionnement" = "com.xdev.ooms.inventory.ligneconditionnement.entity.LigneConditionnement"
    "com.xdev.ooms.inventory.dto.LigneConditionnementDto" = "com.xdev.ooms.inventory.ligneconditionnement.dto.LigneConditionnementDto"
    "com.xdev.ooms.inventory.service.LigneConditionnementService" = "com.xdev.ooms.inventory.ligneconditionnement.service.LigneConditionnementService"
    "com.xdev.ooms.inventory.controller.LigneConditionnementController" = "com.xdev.ooms.inventory.ligneconditionnement.controller.LigneConditionnementController"
    "com.xdev.ooms.inventory.repository.LigneConditionnementRepository" = "com.xdev.ooms.inventory.ligneconditionnement.repository.LigneConditionnementRepository"

    "com.xdev.ooms.inventory.dto.StatistiquesDTO" = "com.xdev.ooms.inventory.statistique.dto.StatistiquesDTO"
    "com.xdev.ooms.inventory.service.StatistiqueService" = "com.xdev.ooms.inventory.statistique.service.StatistiqueService"
    "com.xdev.ooms.inventory.controller.StatistiqueController" = "com.xdev.ooms.inventory.statistique.controller.StatistiqueController"

    "com.xdev.ooms.inventory.dto.MaterialNeedLineDto" = "com.xdev.ooms.inventory.materialneeds.dto.MaterialNeedLineDto"
    "com.xdev.ooms.inventory.service.MaterialNeedsService" = "com.xdev.ooms.inventory.materialneeds.service.MaterialNeedsService"

    "com.xdev.ooms.inventory.service.AuditService" = "com.xdev.ooms.inventory.audit.service.AuditService"
    "com.xdev.ooms.inventory.controller.AuditController" = "com.xdev.ooms.inventory.audit.controller.AuditController"
    "com.xdev.ooms.inventory.service.InventoryDeleteGuardService" = "com.xdev.ooms.inventory.common.service.InventoryDeleteGuardService"

    # HR
    "com.xdev.ooms.hr.model.Employee" = "com.xdev.ooms.hr.employee.entity.Employee"
    "com.xdev.ooms.hr.Dtos.EmployeeDto" = "com.xdev.ooms.hr.employee.dto.EmployeeDto"
    "com.xdev.ooms.hr.service.EmployeeService" = "com.xdev.ooms.hr.employee.service.EmployeeService"
    "com.xdev.ooms.hr.controller.EmployeeController" = "com.xdev.ooms.hr.employee.controller.EmployeeController"
    "com.xdev.ooms.hr.repo.EmployeeRepository" = "com.xdev.ooms.hr.employee.repository.EmployeeRepository"

    "com.xdev.ooms.hr.model.Department" = "com.xdev.ooms.hr.department.entity.Department"
    "com.xdev.ooms.hr.Dtos.DepartmentDto" = "com.xdev.ooms.hr.department.dto.DepartmentDto"
    "com.xdev.ooms.hr.service.departementService" = "com.xdev.ooms.hr.department.service.departementService"
    "com.xdev.ooms.hr.controller.DepartementController" = "com.xdev.ooms.hr.department.controller.DepartementController"
    "com.xdev.ooms.hr.repo.DepartementRepository" = "com.xdev.ooms.hr.department.repository.DepartementRepository"

    "com.xdev.ooms.hr.model.Contract" = "com.xdev.ooms.hr.contract.entity.Contract"
    "com.xdev.ooms.hr.Dtos.ContractDto" = "com.xdev.ooms.hr.contract.dto.ContractDto"
    "com.xdev.ooms.hr.service.ContractService" = "com.xdev.ooms.hr.contract.service.ContractService"
    "com.xdev.ooms.hr.controller.ContractController" = "com.xdev.ooms.hr.contract.controller.ContractController"
    "com.xdev.ooms.hr.repo.ContractRepository" = "com.xdev.ooms.hr.contract.repository.ContractRepository"

    "com.xdev.ooms.hr.model.LeaveRequest" = "com.xdev.ooms.hr.leave.entity.LeaveRequest"
    "com.xdev.ooms.hr.Dtos.LeaveRequestDto" = "com.xdev.ooms.hr.leave.dto.LeaveRequestDto"
    "com.xdev.ooms.hr.service.LeaveRequestService" = "com.xdev.ooms.hr.leave.service.LeaveRequestService"
    "com.xdev.ooms.hr.controller.LeaveRequestController" = "com.xdev.ooms.hr.leave.controller.LeaveRequestController"
    "com.xdev.ooms.hr.repo.LeaveRequestRepository" = "com.xdev.ooms.hr.leave.repository.LeaveRequestRepository"

    "com.xdev.ooms.hr.model.PayRolls" = "com.xdev.ooms.hr.payroll.entity.PayRolls"
    "com.xdev.ooms.hr.Dtos.PayRollsDto" = "com.xdev.ooms.hr.payroll.dto.PayRollsDto"
    "com.xdev.ooms.hr.service.PayRollsService" = "com.xdev.ooms.hr.payroll.service.PayRollsService"
    "com.xdev.ooms.hr.controller.PayRollsController" = "com.xdev.ooms.hr.payroll.controller.PayRollsController"
    "com.xdev.ooms.hr.repo.PayRollsRepository" = "com.xdev.ooms.hr.payroll.repository.PayRollsRepository"

    "com.xdev.ooms.hr.model.Pointage" = "com.xdev.ooms.hr.pointage.entity.Pointage"
    "com.xdev.ooms.hr.Dtos.PointageDto" = "com.xdev.ooms.hr.pointage.dto.PointageDto"
    "com.xdev.ooms.hr.service.PointageService" = "com.xdev.ooms.hr.pointage.service.PointageService"
    "com.xdev.ooms.hr.controller.PointageController" = "com.xdev.ooms.hr.pointage.controller.PointageController"
    "com.xdev.ooms.hr.repo.PointageRepository" = "com.xdev.ooms.hr.pointage.repository.PointageRepository"

    "com.xdev.ooms.hr.model.Poste" = "com.xdev.ooms.hr.poste.entity.Poste"
    "com.xdev.ooms.hr.Dtos.PosteDto" = "com.xdev.ooms.hr.poste.dto.PosteDto"
    "com.xdev.ooms.hr.service.PosteService" = "com.xdev.ooms.hr.poste.service.PosteService"
    "com.xdev.ooms.hr.controller.PosteController" = "com.xdev.ooms.hr.poste.controller.PosteController"
    "com.xdev.ooms.hr.repo.PosteRepository" = "com.xdev.ooms.hr.poste.repository.PosteRepository"

    # Security
    "com.xdev.ooms.security.userManagement.models.OSMUser" = "com.xdev.ooms.security.user.entity.OSMUser"
    "com.xdev.ooms.security.userManagement.dtos.OUTDTO.OSMUserDTO" = "com.xdev.ooms.security.user.dto.OSMUserDTO"
    "com.xdev.ooms.security.userManagement.dtos.OUTDTO.OSMUserOUTDTO" = "com.xdev.ooms.security.user.dto.OSMUserOUTDTO"
    "com.xdev.ooms.security.userManagement.dtos.OUTDTO.AssignableUserDTO" = "com.xdev.ooms.security.user.dto.AssignableUserDTO"
    "com.xdev.ooms.security.userManagement.dtos.OUTDTO.UpdatePasswordDTO" = "com.xdev.ooms.security.user.dto.UpdatePasswordDTO"
    "com.xdev.ooms.security.userManagement.service.UserService" = "com.xdev.ooms.security.user.service.UserService"
    "com.xdev.ooms.security.userManagement.controller.UserController" = "com.xdev.ooms.security.user.controller.UserController"
    "com.xdev.ooms.security.userManagement.data.UserRepository" = "com.xdev.ooms.security.user.repository.UserRepository"

    "com.xdev.ooms.security.userManagement.models.Role" = "com.xdev.ooms.security.role.entity.Role"
    "com.xdev.ooms.security.userManagement.dtos.OUTDTO.RoleDTO" = "com.xdev.ooms.security.role.dto.RoleDTO"
    "com.xdev.ooms.security.userManagement.service.RoleService" = "com.xdev.ooms.security.role.service.RoleService"
    "com.xdev.ooms.security.userManagement.controller.RoleController" = "com.xdev.ooms.security.role.controller.RoleController"
    "com.xdev.ooms.security.userManagement.data.RoleRepository" = "com.xdev.ooms.security.role.repository.RoleRepository"

    "com.xdev.ooms.security.userManagement.models.Permission" = "com.xdev.ooms.security.permission.entity.Permission"
    "com.xdev.ooms.security.userManagement.dtos.OUTDTO.PermissionDTO" = "com.xdev.ooms.security.permission.dto.PermissionDTO"
    "com.xdev.ooms.security.userManagement.service.PermissionService" = "com.xdev.ooms.security.permission.service.PermissionService"
    "com.xdev.ooms.security.userManagement.controller.PermissionController" = "com.xdev.ooms.security.permission.controller.PermissionController"
    "com.xdev.ooms.security.userManagement.data.PermissionRepository" = "com.xdev.ooms.security.permission.repository.PermissionRepository"

    "com.xdev.ooms.security.userManagement.models.CompanyProfile" = "com.xdev.ooms.security.companyprofile.entity.CompanyProfile"
    "com.xdev.ooms.security.userManagement.dtos.OUTDTO.CompanyProfileDTO" = "com.xdev.ooms.security.companyprofile.dto.CompanyProfileDTO"
    "com.xdev.ooms.security.userManagement.dtos.OUTDTO.CompanyUserDTO" = "com.xdev.ooms.security.companyprofile.dto.CompanyUserDTO"
    "com.xdev.ooms.security.userManagement.service.CompanyProfileService" = "com.xdev.ooms.security.companyprofile.service.CompanyProfileService"
    "com.xdev.ooms.security.userManagement.controller.CompanyProfileController" = "com.xdev.ooms.security.companyprofile.controller.CompanyProfileController"
    "com.xdev.ooms.security.userManagement.data.CompanyProfileRepository" = "com.xdev.ooms.security.companyprofile.repository.CompanyProfileRepository"

    "com.xdev.ooms.security.userManagement.models.ConfirmationCode" = "com.xdev.ooms.security.confirmationcode.entity.ConfirmationCode"
    "com.xdev.ooms.security.userManagement.models.enums.ConfirmationMethod" = "com.xdev.ooms.security.confirmationcode.enums.ConfirmationMethod"
    "com.xdev.ooms.security.userManagement.models.enums.ConfirmationCodeType" = "com.xdev.ooms.security.confirmationcode.enums.ConfirmationCodeType"
    "com.xdev.ooms.security.userManagement.dtos.OUTDTO.ConfirmationCodeDTO" = "com.xdev.ooms.security.confirmationcode.dto.ConfirmationCodeDTO"
    "com.xdev.ooms.security.userManagement.dtos.OUTDTO.VerifyOtpAndSetPasswordRequest" = "com.xdev.ooms.security.confirmationcode.dto.VerifyOtpAndSetPasswordRequest"
    "com.xdev.ooms.security.userManagement.service.ConfirmationCodeService" = "com.xdev.ooms.security.confirmationcode.service.ConfirmationCodeService"
    "com.xdev.ooms.security.userManagement.data.ConfirmationCodeRepository" = "com.xdev.ooms.security.confirmationcode.repository.ConfirmationCodeRepository"

    "com.xdev.ooms.security.securityConfig.entities.Authorization" = "com.xdev.ooms.security.authorization.entity.Authorization"
    "com.xdev.ooms.security.securityConfig.data.AuthorizationRepository" = "com.xdev.ooms.security.authorization.repository.AuthorizationRepository"
}

$javaFiles = Get-ChildItem -Path $root -Recurse -Filter "*.java" | Where-Object { $_.FullName -notmatch '\\legacy\\' }
$updated = 0
foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $original = $content
    foreach ($entry in $replacements.GetEnumerator()) {
        $content = $content.Replace($entry.Key, $entry.Value)
    }
    if ($content -ne $original) {
        Write-Utf8NoBomFile -Path $file.FullName -Content $content
        $updated++
    }
}
Write-Host "Updated $updated files."
