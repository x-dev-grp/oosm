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

function Invoke-NerdGraph {
    param(
        [Parameter(Mandatory = $true)][string]$Query,
        [Parameter(Mandatory = $true)][string]$Region,
        [Parameter(Mandatory = $true)][string]$ApiKey
    )
    $endpoint = if ($Region -eq 'EU') { 'https://api.eu.newrelic.com/graphql' } else { 'https://api.newrelic.com/graphql' }
    $body = @{ query = $Query } | ConvertTo-Json -Compress
    $headers = @{
        'API-Key'      = $ApiKey
        'Content-Type' = 'application/json'
    }
    $response = Invoke-RestMethod -Method Post -Uri $endpoint -Headers $headers -Body $body
    if ($response.errors) {
        $message = ($response.errors | ForEach-Object { $_.message }) -join '; '
        throw "NerdGraph error: $message"
    }
    return $response
}

function Invoke-NerdGraphData {
    param(
        [Parameter(Mandatory = $true)][string]$Query,
        [Parameter(Mandatory = $true)][string]$Region,
        [Parameter(Mandatory = $true)][string]$ApiKey
    )
    $response = Invoke-NerdGraph -Query $Query -Region $Region -ApiKey $ApiKey
    if ($null -ne $response.data) {
        return $response.data
    }
    return $response
}

function Invoke-NrqOutput {
    param([Parameter(Mandatory = $true)][string[]]$NrqArgs)
    $previousPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $raw = & nrq @NrqArgs 2>&1
        $exitCode = $LASTEXITCODE
    }
    finally {
        $ErrorActionPreference = $previousPreference
    }
    if ($exitCode -ne 0) {
        throw ($raw | Out-String).Trim()
    }
    return $raw
}

function Escape-GraphQl([string]$value) {
    return $value.Replace('\', '\\').Replace('"', '\"')
}

function Test-NewRelicUserApiKey([string]$key) {
    if ([string]::IsNullOrWhiteSpace($key)) {
        return $false
    }
    return $key.Trim().StartsWith('NRAK-', [System.StringComparison]::OrdinalIgnoreCase)
}

function Show-NewRelicKeyHelp {
    Write-Host ''
    Write-Host 'Wrong API key type.' -ForegroundColor Red
    Write-Host ''
    Write-Host 'nrq / Setup-NewRelic.ps1 needs a USER API key (starts with NRAK-), NOT:' -ForegroundColor Yellow
    Write-Host '  - License / ingest key  -> use for Render NEW_RELIC_LICENSE_KEY only'
    Write-Host '  - Browser license key   -> use for OOSM Admin Integrations browser field only'
    Write-Host ''
    Write-Host 'Create a User key:' -ForegroundColor Cyan
    Write-Host '  1. Open https://one.eu.newrelic.com/launcher/api-keys-ui.api-keys-launcher'
    Write-Host '  2. Create key -> User key'
    Write-Host '  3. Copy the full key (NRAK-...) — shown only once'
    Write-Host ''
    Write-Host 'Then run:' -ForegroundColor Cyan
    Write-Host '  $env:NEWRELIC_API_KEY = ''NRAK-...'''
    Write-Host '  powershell -File scripts\Setup-NewRelic.ps1 -AccountId 8261688 -Region EU'
    Write-Host ''
    Write-Host 'If you already have a license ingest key, pass it separately:' -ForegroundColor DarkGray
    Write-Host '  -LicenseKey ''your-ingest-license-key'''
    Write-Host ''
}

Write-Host '=== OOSM New Relic CLI setup (nrq) ===' -ForegroundColor Cyan

Require-Command 'nrq'

if ([string]::IsNullOrWhiteSpace($ApiKey)) {
    Show-NewRelicKeyHelp
    throw 'NEWRELIC_API_KEY is required (User key starting with NRAK-).'
}

if (-not (Test-NewRelicUserApiKey $ApiKey)) {
    Show-NewRelicKeyHelp
    throw "NEWRELIC_API_KEY must be a User API key starting with NRAK-. You may have pasted a license/ingest key by mistake."
}

Write-Host 'Configuring nrq credentials...' -ForegroundColor DarkGray
$env:NEWRELIC_API_KEY = $ApiKey.Trim()
Invoke-NrqOutput -NrqArgs @('set-credential', '--ref', 'newrelic-cli/default', '--key', 'api_key', '--from-env', 'NEWRELIC_API_KEY', '--overwrite') | Out-Null
Invoke-NrqOutput -NrqArgs @('config', 'set', '--account-id', "$AccountId", '--region', $Region) | Out-Null

try {
    Invoke-NrqOutput -NrqArgs @('me') | Out-Null
    Write-Host 'Authenticated with New Relic (nrq me OK).' -ForegroundColor Green
}
catch {
    Show-NewRelicKeyHelp
    throw "nrq authentication failed. Use a valid User API key (NRAK-) for account $AccountId region $Region. Original error: $_"
}

if ([string]::IsNullOrWhiteSpace($LicenseKey)) {
    throw 'License key is required. Pass -LicenseKey or set NEW_RELIC_LICENSE_KEY.'
}
Write-Host 'Using provided APM license ingest key.' -ForegroundColor Green

$browserGuid = $null
if (-not $SkipBrowserCreate) {
    $escapedBrowserName = Escape-GraphQl $BrowserAppName
    Write-Host "Searching for browser app '$BrowserAppName'..." -ForegroundColor DarkGray
    $searchQuery = "{ actor { entitySearch(query: `"name = '$escapedBrowserName' AND domain = 'BROWSER'`") { results { entities { guid name } } } } }"
    $search = Invoke-NerdGraphData -Query $searchQuery -Region $Region -ApiKey $ApiKey.Trim()
    $existing = @($search.actor.entitySearch.results.entities)
    if ($existing.Count -gt 0 -and $null -ne $existing[0]) {
        $browserGuid = $existing[0].guid
        Write-Host "Reusing existing browser app (guid=$browserGuid)." -ForegroundColor Yellow
    }
    else {
        Write-Host "Creating browser SPA app '$BrowserAppName'..." -ForegroundColor DarkGray
        $mutation = "mutation { agentApplicationCreateBrowser(accountId: $AccountId, name: `"$escapedBrowserName`", settings: { loaderType: SPA, distributedTracingEnabled: true, cookiesEnabled: true }) { guid name settings { loaderType } } }"
        $create = Invoke-NerdGraphData -Query $mutation -Region $Region -ApiKey $ApiKey.Trim()
        $browserGuid = $create.agentApplicationCreateBrowser.guid
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
$configQuery = "{ actor { entity(guid: `"$browserGuid`") { ... on BrowserApplicationEntity { guid name browserProperties { jsConfig } } } } }"
$browserConfig = Invoke-NerdGraphData -Query $configQuery -Region $Region -ApiKey $ApiKey.Trim()
$entity = $browserConfig.actor.entity
$jsConfigRaw = $entity.browserProperties.jsConfig
if ($null -eq $jsConfigRaw) {
    throw 'Could not read browser jsConfig from New Relic.'
}
if ($jsConfigRaw -is [string]) {
    $jsConfig = $jsConfigRaw | ConvertFrom-Json
}
else {
    $jsConfig = $jsConfigRaw
}

$browserAccountId = if ($jsConfig.loader_config.accountID) {
    [string]$jsConfig.loader_config.accountID
}
elseif ($jsConfig.info.accountID) {
    [string]$jsConfig.info.accountID
}
else {
    [string]$AccountId
}
$browserApplicationId = if ($jsConfig.info.applicationID) {
    [string]$jsConfig.info.applicationID
}
else {
    [string]$jsConfig.loader_config.applicationID
}
$browserLicenseKey = if ($jsConfig.info.licenseKey) {
    [string]$jsConfig.info.licenseKey
}
else {
    [string]$jsConfig.loader_config.licenseKey
}

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
