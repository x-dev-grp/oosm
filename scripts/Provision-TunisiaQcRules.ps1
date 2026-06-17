<#
.SYNOPSIS
  Provision missing Tunisia default QC rules for a tenant (idempotent).

.DESCRIPTION
  Option 1 - API (uses logged-in tenant from JWT):
    Calls POST /api/production/qualitycontrolrules/provision-defaults

  Option 2 - SQL (direct database):
    Runs 20260617_provision_tunisia_qc_rules_for_tenant.sql via psql

.PARAMETER TenantId
  Company / tenant UUID. Required for -Mode Sql. Optional for -Mode Api if token carries tenant.

.PARAMETER Mode
  Api | Sql | CheckOnly

.PARAMETER ApiBaseUrl
  Backend base URL (default http://localhost:8080)

.PARAMETER AccessToken
  Bearer JWT. For Api mode. Or set env OSM_ACCESS_TOKEN.

.PARAMETER DatabaseUrl
  PostgreSQL URL. Or set env DATABASE_URL / DB_URL.

.EXAMPLE
  # Check existing rules in DB for a tenant
  .\Provision-TunisiaQcRules.ps1 -Mode CheckOnly -TenantId "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"

.EXAMPLE
  # Provision via SQL
  .\Provision-TunisiaQcRules.ps1 -Mode Sql -TenantId "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"

.EXAMPLE
  # Provision via API (current tenant from your session token)
  $env:OSM_ACCESS_TOKEN = "<paste-jwt>"
  .\Provision-TunisiaQcRules.ps1 -Mode Api
#>
param(
    [string]$TenantId,
    [ValidateSet('Api', 'Sql', 'CheckOnly')]
    [string]$Mode = 'Sql',
    [string]$ApiBaseUrl = $(if ($env:OSM_API_URL) { $env:OSM_API_URL } else { 'http://localhost:8080' }),
    [string]$AccessToken = $env:OSM_ACCESS_TOKEN,
    [string]$DatabaseUrl = $(if ($env:DATABASE_URL) { $env:DATABASE_URL } elseif ($env:DB_URL) { $env:DB_URL } else { $null })
)

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$sqlFile = Join-Path $repoRoot 'modules/production/src/main/resources/legacy/osm-prod/db/migration/20260617_provision_tunisia_qc_rules_for_tenant.sql'

$expectedRules = @(
    @{ ruleKey = 'Categorie'; oilQc = $true;  ruleName = 'Catégorie' },
    @{ ruleKey = 'Acidite'; oilQc = $true;  ruleName = 'Acidité (% maaa)' },
    @{ ruleKey = 'K232'; oilQc = $true;  ruleName = 'K232' },
    @{ ruleKey = 'K270'; oilQc = $true;  ruleName = 'K270' },
    @{ ruleKey = 'DeltaK'; oilQc = $true;  ruleName = 'Delta K' },
    @{ ruleKey = 'IndicePreoxyde'; oilQc = $true;  ruleName = 'Indice peroxyde (meq O2/kg)' },
    @{ ruleKey = 'Infestees'; oilQc = $false; ruleName = 'Infestées %' },
    @{ ruleKey = 'Fermentees'; oilQc = $false; ruleName = 'Fermentées %' },
    @{ ruleKey = 'Endommagees'; oilQc = $false; ruleName = 'Endommagées %' },
    @{ ruleKey = 'Categorie'; oilQc = $false; ruleName = 'Catégorie Olive' }
)

function Write-ExpectedSummary {
    Write-Host ''
    Write-Host 'Expected Tunisia defaults (10 rules):' -ForegroundColor Cyan
    foreach ($r in $expectedRules) {
        $scope = if ($r.oilQc) { 'oil' } else { 'olive' }
        Write-Host ("  - [{0}] {1} ({2})" -f $scope, $r.ruleKey, $r.ruleName)
    }
}

function Invoke-CheckSql {
    param([string]$Tenant)

    if (-not (Get-Command psql -ErrorAction SilentlyContinue)) {
        throw 'psql not found in PATH.'
    }
    if (-not $DatabaseUrl) {
        throw 'Set -DatabaseUrl or DATABASE_URL for Sql/CheckOnly mode.'
    }
    if (-not $Tenant) {
        throw 'TenantId is required for Sql/CheckOnly mode.'
    }

    $checkSql = @"
SELECT rule_key, rule_name, oil_qc, rule_type, min_value, max_value
FROM public.quality_control_rule
WHERE tenant_id = '$Tenant'::uuid AND is_deleted = false
ORDER BY oil_qc DESC, rule_key;
"@

    Write-Host "Existing rules for tenant $Tenant" -ForegroundColor Yellow
    $checkSql | psql $DatabaseUrl -v ON_ERROR_STOP=1
    Write-ExpectedSummary
}

function Invoke-ProvisionSql {
    param([string]$Tenant)

    if (-not (Test-Path $sqlFile)) {
        throw "SQL file not found: $sqlFile"
    }
    if (-not (Get-Command psql -ErrorAction SilentlyContinue)) {
        throw 'psql not found in PATH.'
    }
    if (-not $DatabaseUrl) {
        throw 'Set -DatabaseUrl or DATABASE_URL for Sql mode.'
    }
    if (-not $Tenant) {
        throw 'TenantId is required for Sql mode.'
    }

    Write-Host "Running idempotent SQL provisioning for tenant $Tenant" -ForegroundColor Green
    psql $DatabaseUrl -v ON_ERROR_STOP=1 -v "tenant_id='$Tenant'" -f $sqlFile
}

function Invoke-ProvisionApi {
    if (-not $AccessToken) {
        throw 'Set -AccessToken or OSM_ACCESS_TOKEN for Api mode.'
    }

    $uri = ($ApiBaseUrl.TrimEnd('/')) + '/api/production/qualitycontrolrules/provision-defaults'
    Write-Host "POST $uri" -ForegroundColor Green

    $headers = @{
        Authorization = "Bearer $AccessToken"
        'Content-Type' = 'application/json'
    }

    $response = Invoke-RestMethod -Method Post -Uri $uri -Headers $headers -Body '{}' 
    $response | ConvertTo-Json -Depth 5 | Write-Host

    if ($response.success -ne $true) {
        throw 'Provisioning API returned success=false'
    }

    Write-Host ("Created {0} rule(s)." -f $response.created) -ForegroundColor Green
    if ($response.created -eq 0) {
        Write-Host 'All Tunisia defaults were already present for the current tenant.' -ForegroundColor Yellow
    }
}

Write-ExpectedSummary

switch ($Mode) {
    'CheckOnly' {
        Invoke-CheckSql -Tenant $TenantId
    }
    'Sql' {
        Invoke-CheckSql -Tenant $TenantId
        Invoke-ProvisionSql -Tenant $TenantId
    }
    'Api' {
        Invoke-ProvisionApi
    }
}

Write-Host ''
Write-Host 'Done.' -ForegroundColor Green
