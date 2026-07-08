# Provision New Relic resources for OOSM via nrq CLI (browser app + ingest keys + OOSM settings output).
# Usage:
#   $env:NEWRELIC_API_KEY = "<user-api-key>"
#   powershell -File scripts/Setup-NewRelic.ps1 -AccountId 12345678 -Region EU
#
# Optional:
#   -ApplyToDatabase   upsert non-secret app_setting rows (requires local DB in .env)
#   -SkipBrowserCreate reuse existing browser app by name

param(
    [string]$ApiKey = $env:NEWRELIC_API_KEY,
    [Parameter(Mandatory = $true)]
    [int]$AccountId,
    [ValidateSet('US', 'EU')]
    [string]$Region = 'EU',
    [string]$ApmAppName = 'oosm-monolith',
    [string]$BrowserAppName = 'oosm-frontend',
    [string]$LicenseKey = $env:NEW_RELIC_LICENSE_KEY,
    [switch]$ApplyToDatabase,
    [switch]$SkipBrowserCreate
)

$ErrorActionPreference = 'Stop'
$scriptsDir = $PSScriptRoot
$repoRoot = Split-Path $scriptsDir -Parent

function Require-Command($name) {
    if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
        throw "Missing command '$name'. Install: winget install OpenCLICollective.newrelic-cli"
    }
}

function Invoke-NrqJson {
    param([Parameter(Mandatory = $true)][string[]]$NrqArgs)
    $raw = & nrq @NrqArgs 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw ($raw | Out-String).Trim()
    }
    $text = ($raw | Out-String).Trim()
    if ([string]::IsNullOrWhiteSpace($text)) {
        return $null
    }
    return $text | ConvertFrom-Json
}

function Escape-GraphQl([string]$value) {
    return $value.Replace('\', '\\').Replace('"', '\"')
}

Write-Host '=== OOSM New Relic CLI setup (nrq) ===' -ForegroundColor Cyan

Require-Command 'nrq'

if ([string]::IsNullOrWhiteSpace($ApiKey)) {
    throw @"
NEWRELIC_API_KEY is required.
1. New Relic UI -> Profile -> API keys -> Create a User key
2. Run:
   `$env:NEWRELIC_API_KEY = 'your-user-key'
   powershell -File scripts/Setup-NewRelic.ps1 -AccountId YOUR_ACCOUNT_ID -Region EU
"@
}

Write-Host 'Configuring nrq credentials...' -ForegroundColor DarkGray
$env:NEWRELIC_API_KEY = $ApiKey
& nrq set-credential --ref newrelic-cli/default --key api_key --from-env NEWRELIC_API_KEY --overwrite | Out-Null
& nrq config set --account-id $AccountId --region $Region | Out-Null

$me = Invoke-NrqJson -NrqArgs @('me')
Write-Host ("Authenticated as {0}" -f $me.data.actor.user.email) -ForegroundColor Green

if ([string]::IsNullOrWhiteSpace($LicenseKey)) {
    Write-Host 'Creating APM ingest (license) key...' -ForegroundColor DarkGray
    $licenseResult = Invoke-NrqJson -NrqArgs @('keys', 'create', '--type', 'ingest', '--ingest-type', 'license', '--name', 'oosm-apm-license')
    $LicenseKey = $licenseResult.ingestKey.key
    if (-not $LicenseKey) { $LicenseKey = $licenseResult.key }
    if ([string]::IsNullOrWhiteSpace($LicenseKey)) {
        throw 'Failed to create license ingest key. Create one in New Relic UI -> API keys -> Ingest license key.'
    }
    Write-Host 'Created license ingest key.' -ForegroundColor Green
}

$browserGuid = $null
if (-not $SkipBrowserCreate) {
    $escapedBrowserName = Escape-GraphQl $BrowserAppName
    $searchQuery = "name = '$escapedBrowserName' AND domain = 'BROWSER'"
    Write-Host "Searching for browser app '$BrowserAppName'..." -ForegroundColor DarkGray
    $search = Invoke-NrqJson -NrqArgs @('entities', 'search', $searchQuery)
    $existing = @()
    if ($search.entities) { $existing = @($search.entities) }
    elseif ($search.results) { $existing = @($search.results) }
    if ($existing.Count -gt 0) {
        $browserGuid = $existing[0].guid
        Write-Host "Reusing existing browser app (guid=$browserGuid)." -ForegroundColor Yellow
    }
    else {
        Write-Host "Creating browser SPA app '$BrowserAppName'..." -ForegroundColor DarkGray
        $mutation = @"
mutation { agentApplicationCreateBrowser(accountId: $AccountId, name: \"$escapedBrowserName\", settings: { loaderType: SPA, distributedTracingEnabled: true, cookiesEnabled: true }) { guid name settings { loaderType } } }
"@
        $create = Invoke-NrqJson -NrqArgs @('nerdgraph', 'query', $mutation)
        $browserGuid = $create.data.agentApplicationCreateBrowser.guid
        if ([string]::IsNullOrWhiteSpace($browserGuid)) {
            throw 'Browser app creation failed. Check account permissions.'
        }
        Write-Host "Created browser app (guid=$browserGuid)." -ForegroundColor Green
    }
}

if ([string]::IsNullOrWhiteSpace($browserGuid)) {
    throw 'Browser app GUID is required. Remove -SkipBrowserCreate or pass an existing app name.'
}

Write-Host 'Fetching browser agent configuration...' -ForegroundColor DarkGray
$configQuery = @"
{ actor { entity(guid: \"$browserGuid\") { ... on BrowserApplicationEntity { guid name browserProperties { jsConfig } } } } }
"@
$browserConfig = Invoke-NrqJson -NrqArgs @('nerdgraph', 'query', $configQuery)
$jsConfigRaw = $browserConfig.data.actor.entity.browserProperties.jsConfig
if ([string]::IsNullOrWhiteSpace($jsConfigRaw)) {
    throw 'Could not read browser jsConfig from New Relic.'
}
$jsConfig = $jsConfigRaw | ConvertFrom-Json

$browserAccountId = [string]$jsConfig.info.accountID
$browserApplicationId = [string]$jsConfig.info.applicationID
$browserLicenseKey = [string]$jsConfig.info.licenseKey

if ([string]::IsNullOrWhiteSpace($browserAccountId) -or
    [string]::IsNullOrWhiteSpace($browserApplicationId) -or
    [string]::IsNullOrWhiteSpace($browserLicenseKey)) {
    throw 'Browser jsConfig is missing accountID, applicationID, or licenseKey.'
}

$renderEnv = @"
# --- New Relic (generated by scripts/Setup-NewRelic.ps1) ---
NEW_RELIC_APM_ENABLED=true
NEW_RELIC_LICENSE_KEY=$LicenseKey
NEW_RELIC_APP_NAME=$ApmAppName
NEW_RELIC_REGION=$Region
NEW_RELIC_LOG_FORWARDING_ENABLED=true
"@

$adminSettings = [ordered]@{
    NEW_RELIC_APM_ENABLED                  = 'true'
    NEW_RELIC_APP_NAME                     = $ApmAppName
    NEW_RELIC_LOG_FORWARDING_ENABLED       = 'true'
    NEW_RELIC_REGION                       = $Region
    NEW_RELIC_BROWSER_ENABLED              = 'true'
    NEW_RELIC_BROWSER_ACCOUNT_ID           = $browserAccountId
    NEW_RELIC_BROWSER_APPLICATION_ID       = $browserApplicationId
    NEW_RELIC_BROWSER_LICENSE_KEY          = $browserLicenseKey
}

$output = [ordered]@{
    generatedAt            = (Get-Date).ToString('o')
    accountId              = $AccountId
    region                 = $Region
    apmAppName             = $ApmAppName
    browserAppName         = $BrowserAppName
    browserEntityGuid      = $browserGuid
    licenseKey             = $LicenseKey
    browserAccountId       = $browserAccountId
    browserApplicationId   = $browserApplicationId
    browserLicenseKey      = $browserLicenseKey
    renderEnv              = $renderEnv
    adminSettings          = $adminSettings
}

$outFile = Join-Path $repoRoot 'newrelic-oosm-config.json'
$output | ConvertTo-Json -Depth 6 | Set-Content -Path $outFile -Encoding UTF8

Write-Host ''
Write-Host '=== Render / host environment ===' -ForegroundColor Cyan
Write-Host $renderEnv
Write-Host ''
Write-Host '=== OOSM Administration -> Settings -> Integrations ===' -ForegroundColor Cyan
foreach ($entry in $adminSettings.GetEnumerator()) {
    if ($entry.Key -eq 'NEW_RELIC_LICENSE_KEY') {
        Write-Host ('{0} = <rotate secret in admin UI>' -f $entry.Key)
    }
    else {
        Write-Host ('{0} = {1}' -f $entry.Key, $entry.Value)
    }
}
Write-Host ''
Write-Host "Full output saved to: $outFile" -ForegroundColor Green
Write-Host 'Next steps:' -ForegroundColor Yellow
Write-Host '  1. Add Render env vars above and redeploy backend'
Write-Host '  2. Paste Integrations settings in OOSM admin (license key via Rotate secret)'
Write-Host '  3. Restart backend; reload frontend'
Write-Host "  4. Verify: nrq apps list | nrq entities search `"domain = 'BROWSER'`""

if ($ApplyToDatabase) {
    Write-Host ''
    Write-Host 'Applying non-secret settings to app_setting...' -ForegroundColor DarkGray
    $applyScript = Join-Path $scriptsDir 'apply-newrelic-settings.cjs'
    if (-not (Test-Path $applyScript)) {
        throw "Missing $applyScript"
    }
    $payload = @{
        settings = $adminSettings
        licenseKey = $LicenseKey
    } | ConvertTo-Json -Depth 4 -Compress
    $payload | node $applyScript
    Write-Host 'Database settings applied (license key stored encrypted if supported).' -ForegroundColor Green
}
