# Finance module entity/DTO co-location migration
$ErrorActionPreference = "Stop"
. "$PSScriptRoot\Write-Utf8NoBom.ps1"
$base = "C:\OSM 2.0\oosm\modules\finance\src\main\java\com\xdev\ooms\finance"

$moves = @{
    "model\BaseType.java" = "basetype\entity"
    "dto\BaseTypeDto.java" = "basetype\dto"
    "service\GenericTypeService.java" = "basetype\service"
    "controller\GenericTypeController.java" = "basetype\controller"
    "repo\GenericRepository.java" = "basetype\repository"

    "model\FinancialTransaction.java" = "financialtransaction\entity"
    "dto\FinancialTransactionDto.java" = "financialtransaction\dto"
    "service\FinancialTransactionService.java" = "financialtransaction\service"
    "controller\FinancialTransactionController.java" = "financialtransaction\controller"
    "repo\FinancialTransactionRepository.java" = "financialtransaction\repository"

    "model\Expense.java" = "expense\entity"
    "dto\ExpenseDto.java" = "expense\dto"
    "service\ExpensesService.java" = "expense\service"
    "controller\ExpensesController.java" = "expense\controller"
    "repo\ExpensesRepository.java" = "expense\repository"

    "model\BankAccount.java" = "bankaccount\entity"
    "dto\BankAccountDto.java" = "bankaccount\dto"
    "service\BankAccountService.java" = "bankaccount\service"
    "controller\BankAccountController.java" = "bankaccount\controller"
    "repo\BankAccountRepository.java" = "bankaccount\repository"

    "model\OilCredit.java" = "oilcredit\entity"
    "dto\OilCreditDto.java" = "oilcredit\dto"
    "service\OilCreditService.java" = "oilcredit\service"
    "controller\OilCreditController.java" = "oilcredit\controller"
    "repo\OilCreditRepository.java" = "oilcredit\repository"

    "model\Supplier.java" = "supplier\entity"
    "dto\SupplierDto.java" = "supplier\dto"
    "service\SupplierTypeService.java" = "supplier\service"
    "controller\SupplierTypeController.java" = "supplier\controller"
    "repo\SupplierRepository.java" = "supplier\repository"
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
    if (Test-Path $dest) { continue }
    Move-Item -Path $src -Destination $dest
    Write-Host "Moved: $($entry.Key) -> $($entry.Value)"
}

foreach ($entry in $moves.GetEnumerator()) {
    $destDir = Join-Path $base $entry.Value
    $fileName = Split-Path $entry.Key -Leaf
    $dest = Join-Path $destDir $fileName
    if (-not (Test-Path $dest)) { continue }
    $parts = $entry.Value -split '\\'
    $feature = $parts[0]
    $layer = $parts[1]
    $newPackage = "com.xdev.ooms.finance.$feature.$layer"
    $content = Get-Content $dest -Raw -Encoding UTF8
    $content = $content -replace 'package com\.xdev\.ooms\.finance\.(model|dto|service|controller|repo);', "package $newPackage;"
    Write-Utf8NoBomFile -Path $dest -Content $content
}

Write-Host "Finance file moves complete."
