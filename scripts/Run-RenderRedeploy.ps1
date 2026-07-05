param(
    [ValidateSet('check', 'database', 'all')]
    [string]$Action = 'check',
    [switch]$IncludeOneTimeSeeds,
    [switch]$SkipHealthCheck
)

$ErrorActionPreference = 'Stop'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$feRoot = (Resolve-Path (Join-Path $repoRoot '..\osm-ms-fe')).Path

$services = [ordered]@{
    Api = 'https://oosm-api.onrender.com/actuator/health/liveness'
    WebPrimary = 'https://oosm-web.onrender.com/'
    WebBlueprint = 'https://osm-ms-fe-1.onrender.com/'
}

function Test-RenderEndpoint {
    param(
        [string]$Name,
        [string]$Url,
        [int]$TimeoutSec = 90
    )

    try {
        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec $TimeoutSec
        $sw.Stop()
        [pscustomobject]@{
            Name = $Name
            Url = $Url
            Ok = $true
            Status = $response.StatusCode
            Detail = "OK in $($sw.ElapsedMilliseconds)ms"
        }
    } catch {
        [pscustomobject]@{
            Name = $Name
            Url = $Url
            Ok = $false
            Status = $null
            Detail = $_.Exception.Message
        }
    }
}

function Write-Section {
    param([string]$Title)
    Write-Host ''
    Write-Host "=== $Title ===" -ForegroundColor Cyan
}

function Import-RenderEnvFile {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        return @{}
    }

    $values = @{}
    Get-Content $Path | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith('#')) {
            return
        }
        $parts = $line -split '=', 2
        if ($parts.Count -eq 2) {
            $values[$parts[0].Trim()] = $parts[1].Trim()
        }
    }
    return $values
}

Write-Section 'Render deployment status'

if (-not $SkipHealthCheck) {
    foreach ($entry in $services.GetEnumerator()) {
        $result = Test-RenderEndpoint -Name $entry.Key -Url $entry.Value
        $color = if ($result.Ok) { 'Green' } else { 'Red' }
        Write-Host ("[{0}] {1} -> {2}" -f $(if ($result.Ok) { 'OK' } else { 'FAIL' }), $result.Url, $result.Detail) -ForegroundColor $color
    }
}

Write-Section 'Local repository state'

Push-Location $repoRoot
try {
    $beBranch = git branch --show-current
    $beStatus = git status -sb
    $beAhead = (git rev-list --count "origin/$beBranch..HEAD" 2>$null)
    if (-not $beAhead) { $beAhead = '?' }
    Write-Host "Backend repo: $repoRoot"
    Write-Host "Branch: $beBranch | Unpushed commits: $beAhead"
    Write-Host $beStatus
} finally {
    Pop-Location
}

Push-Location $feRoot
try {
    $feBranch = git branch --show-current
    $feStatus = git status -sb
    $feDirty = (git status --porcelain | Measure-Object).Count
    Write-Host ''
    Write-Host "Frontend repo: $feRoot"
    Write-Host "Branch: $feBranch | Uncommitted files: $feDirty"
    Write-Host $feStatus
} finally {
    Pop-Location
}

if ($Action -eq 'database' -or $Action -eq 'all') {
    Write-Section 'Database bootstrap (Render PostgreSQL)'

    $renderEnv = Import-RenderEnvFile -Path (Join-Path $repoRoot '.env.render')
    if ($renderEnv.Count -eq 0) {
        throw "Missing backend .env.render. Copy oosm/.env.render.example and fill Render DB credentials."
    }

    $env:DB_URL = if ($renderEnv['DB_URL'] -notmatch 'sslmode=') {
        "$($renderEnv['DB_URL'])?sslmode=require"
    } else {
        $renderEnv['DB_URL']
    }
    $env:DB_USER = $renderEnv['DB_USER']
    $env:DB_PASS = $renderEnv['DB_PASS']

    $scriptArgs = @{}
    if ($IncludeOneTimeSeeds) {
        $scriptArgs['IncludeOneTimeSeeds'] = $true
    }

    & (Join-Path $repoRoot 'scripts/Run-RailwayDatabaseScripts.ps1') @scriptArgs
    Write-Host 'Database scripts completed.' -ForegroundColor Green
}

Write-Section 'Render dashboard redeploy order'

@'
1. PostgreSQL (Render)
   - Open the Render Postgres instance (internal host dpg-*-a).
   - Confirm status is "Available" (resume if suspended).
   - For a full reset: create a new database or restore from backup, then run:
       .\scripts\Run-RenderRedeploy.ps1 -Action database -IncludeOneTimeSeeds
   - For an existing database, run idempotent scripts only:
       .\scripts\Run-RenderRedeploy.ps1 -Action database

2. Backend (oosm-api)
   - Service branch in render.yaml must match the branch you deploy (pfe-v2-final).
   - autoDeployTrigger is OFF: click "Manual Deploy" -> "Deploy latest commit".
   - Verify env vars from oosm/.env.render:
       DB_URL, DB_USER, DB_PASS
       FRONTEND_ENTRY_POINT=https://oosm-web.onrender.com
       APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://*.onrender.com,https://oosm-web.onrender.com
   - Health check: https://oosm-api.onrender.com/actuator/health/liveness

3. Frontend (oosm-web / osm-ms-fe-1)
   - Commit and push osm-ms-fe changes on pfe-v2-final (auto deploy on commit).
   - Ensure BACKEND_URL=https://oosm-api.onrender.com
   - Health check: frontend URL returns 200 on /

4. Git push blockers detected locally
   - Backend push to x-dev-grp/oosm failed with 403 for user Smail-ssm.
   - Ask a repo admin to push pfe-v2-final or merge into main, then redeploy oosm-api.
'@ | Write-Host

if ($Action -eq 'check') {
    Write-Host ''
    Write-Host 'Health check only. To run DB scripts:' -ForegroundColor Yellow
    Write-Host '  .\scripts\Run-RenderRedeploy.ps1 -Action database' -ForegroundColor Yellow
}
