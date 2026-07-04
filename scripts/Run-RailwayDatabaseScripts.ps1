param(
    [string]$DatabaseUrl = $env:DATABASE_URL,
    [string]$DbUrl = $env:DB_URL,
    [string]$DbHost = $env:PGHOST,
    [string]$DbPort = $env:PGPORT,
    [string]$DbName = $env:PGDATABASE,
    [string]$DbUser = $(if ($env:DB_USER) { $env:DB_USER } else { $env:PGUSER }),
    [string]$DbPassword = $(if ($env:DB_PASS) { $env:DB_PASS } else { $env:PGPASSWORD }),
    [switch]$IncludeOneTimeSeeds
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command psql -ErrorAction SilentlyContinue)) {
    throw "psql was not found in PATH. Install PostgreSQL client tools first."
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

$idempotentScripts = @(
    "scripts/rename-osm-to-oosm.sql",
    "scripts/migrate-osmuser-to-oosmuser.sql",
    "modules/production/src/main/resources/legacy/osm-prod/db/migration/20260522_add_traceability_lot_table.sql",
    "modules/production/src/main/resources/legacy/osm-prod/db/migration/20260522_add_filtration_quality_control_links.sql",
    "modules/conditioning/src/main/resources/legacy/osm-cond/db/migration/20260522_add_traceability_lot_columns.sql",
    "modules/inventory/src/main/resources/legacy/osm-pack/db/migration/20260511_add_product_olive_oil_attributes.sql",
    "modules/conditioning/src/main/resources/legacy/osm-cond/db/migration/20260517_add_product_relation_to_labels.sql",
    "modules/inventory/src/main/resources/legacy/osm-pack/db/migration/V20260523__stock_bom_enhancements.sql",
    "modules/production/src/main/resources/legacy/osm-prod/db/migration/20260517_seed_production_parameters.sql",
    "modules/production/src/main/resources/legacy/osm-prod/db/migration/insert param.sql",
    "modules/production/src/main/resources/legacy/osm-prod/db/migration/insert Permissions.sql"
)

$oneTimeSeedScripts = @(
    "modules/production/src/main/resources/legacy/osm-prod/db/migration/insert qc rules.sql"
)

function Invoke-PsqlFile {
    param([string]$FilePath)

    Write-Host "Running $FilePath"

    if ($DatabaseUrl) {
        & psql $DatabaseUrl -v ON_ERROR_STOP=1 -f $FilePath
        return
    }

    if ($DbUrl) {
        $psqlUrl = $DbUrl -replace '^jdbc:', ''
        $previousPassword = $env:PGPASSWORD
        $env:PGPASSWORD = $DbPassword
        try {
            & psql $psqlUrl -U $DbUser -v ON_ERROR_STOP=1 -f $FilePath
        } finally {
            $env:PGPASSWORD = $previousPassword
        }
        return
    }

    if (-not $DbHost -or -not $DbPort -or -not $DbName -or -not $DbUser) {
        throw "Provide DATABASE_URL, DB_URL with DB_USER/DB_PASS, or PGHOST, PGPORT, PGDATABASE, PGUSER, and PGPASSWORD."
    }

    $previousPassword = $env:PGPASSWORD
    $env:PGPASSWORD = $DbPassword
    try {
        & psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -v ON_ERROR_STOP=1 -f $FilePath
    } finally {
        $env:PGPASSWORD = $previousPassword
    }
}

foreach ($relativePath in $idempotentScripts) {
    $fullPath = Join-Path $repoRoot $relativePath
    Invoke-PsqlFile -FilePath $fullPath
}

if ($IncludeOneTimeSeeds) {
    foreach ($relativePath in $oneTimeSeedScripts) {
        $fullPath = Join-Path $repoRoot $relativePath
        Invoke-PsqlFile -FilePath $fullPath
    }
} else {
    Write-Host "Skipped one-time seed scripts. Re-run with -IncludeOneTimeSeeds only on a fresh database."
}
