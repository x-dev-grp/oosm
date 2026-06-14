# Update imports after finance DTO co-location
$ErrorActionPreference = "Stop"
. "$PSScriptRoot\Write-Utf8NoBom.ps1"
$root = "C:\OSM 2.0\oosm"

$replacements = [ordered]@{
    "com.xdev.ooms.finance.dto.FinancialTransactionDto" = "com.xdev.ooms.finance.financialtransaction.dto.FinancialTransactionDto"
    "com.xdev.ooms.finance.dto.BankAccountDto" = "com.xdev.ooms.finance.bankaccount.dto.BankAccountDto"
    "com.xdev.ooms.finance.dto.BaseTypeDto" = "com.xdev.ooms.finance.basetype.dto.BaseTypeDto"
    "com.xdev.ooms.finance.dto.OilCreditDto" = "com.xdev.ooms.finance.oilcredit.dto.OilCreditDto"
    "com.xdev.ooms.finance.dto.ExpenseDto" = "com.xdev.ooms.finance.expense.dto.ExpenseDto"
    "com.xdev.ooms.finance.dto.SupplierDto" = "com.xdev.ooms.finance.supplier.dto.SupplierDto"

    "com.xdev.ooms.finance.model.FinancialTransaction" = "com.xdev.ooms.finance.financialtransaction.entity.FinancialTransaction"
    "com.xdev.ooms.finance.model.BankAccount" = "com.xdev.ooms.finance.bankaccount.entity.BankAccount"
    "com.xdev.ooms.finance.model.BaseType" = "com.xdev.ooms.finance.basetype.entity.BaseType"
    "com.xdev.ooms.finance.model.OilCredit" = "com.xdev.ooms.finance.oilcredit.entity.OilCredit"
    "com.xdev.ooms.finance.model.Expense" = "com.xdev.ooms.finance.expense.entity.Expense"
    "com.xdev.ooms.finance.model.Supplier" = "com.xdev.ooms.finance.supplier.entity.Supplier"

    "com.xdev.ooms.finance.repo.FinancialTransactionRepository" = "com.xdev.ooms.finance.financialtransaction.repository.FinancialTransactionRepository"
    "com.xdev.ooms.finance.repo.BankAccountRepository" = "com.xdev.ooms.finance.bankaccount.repository.BankAccountRepository"
    "com.xdev.ooms.finance.repo.GenericRepository" = "com.xdev.ooms.finance.basetype.repository.GenericRepository"
    "com.xdev.ooms.finance.repo.OilCreditRepository" = "com.xdev.ooms.finance.oilcredit.repository.OilCreditRepository"
    "com.xdev.ooms.finance.repo.ExpensesRepository" = "com.xdev.ooms.finance.expense.repository.ExpensesRepository"
    "com.xdev.ooms.finance.repo.SupplierRepository" = "com.xdev.ooms.finance.supplier.repository.SupplierRepository"

    "com.xdev.ooms.finance.service.FinancialTransactionService" = "com.xdev.ooms.finance.financialtransaction.service.FinancialTransactionService"
    "com.xdev.ooms.finance.service.BankAccountService" = "com.xdev.ooms.finance.bankaccount.service.BankAccountService"
    "com.xdev.ooms.finance.service.GenericTypeService" = "com.xdev.ooms.finance.basetype.service.GenericTypeService"
    "com.xdev.ooms.finance.service.OilCreditService" = "com.xdev.ooms.finance.oilcredit.service.OilCreditService"
    "com.xdev.ooms.finance.service.ExpensesService" = "com.xdev.ooms.finance.expense.service.ExpensesService"
    "com.xdev.ooms.finance.service.SupplierTypeService" = "com.xdev.ooms.finance.supplier.service.SupplierTypeService"

    "com.xdev.ooms.finance.controller.FinancialTransactionController" = "com.xdev.ooms.finance.financialtransaction.controller.FinancialTransactionController"
    "com.xdev.ooms.finance.controller.BankAccountController" = "com.xdev.ooms.finance.bankaccount.controller.BankAccountController"
    "com.xdev.ooms.finance.controller.GenericTypeController" = "com.xdev.ooms.finance.basetype.controller.GenericTypeController"
    "com.xdev.ooms.finance.controller.OilCreditController" = "com.xdev.ooms.finance.oilcredit.controller.OilCreditController"
    "com.xdev.ooms.finance.controller.ExpensesController" = "com.xdev.ooms.finance.expense.controller.ExpensesController"
    "com.xdev.ooms.finance.controller.SupplierTypeController" = "com.xdev.ooms.finance.supplier.controller.SupplierTypeController"
}

$javaFiles = Get-ChildItem -Path $root -Recurse -Filter "*.java" | Where-Object { $_.FullName -notmatch '\\legacy\\' }
foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $original = $content
    foreach ($entry in $replacements.GetEnumerator()) {
        $content = $content.Replace($entry.Key, $entry.Value)
    }
    if ($content -ne $original) {
        Write-Utf8NoBomFile -Path $file.FullName -Content $content
        Write-Host "Updated: $($file.Name)"
    }
}

Write-Host "Finance import replacement complete."
