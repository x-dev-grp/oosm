param(
    [string]$SourceRoot = "C:\osmproject",
    [string]$TargetRoot = "C:\osmproject\OSM 2.0",
    [string[]]$Modules = @("inventory", "conditioning", "finance", "production", "hr"),
    [switch]$Overwrite,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Resolve-FullPath {
    param([string]$Path)
    return [System.IO.Path]::GetFullPath($Path)
}

function Convert-PackageToPath {
    param([string]$PackageName)
    return $PackageName.Replace(".", [System.IO.Path]::DirectorySeparatorChar)
}

function Get-RelativePathCompat {
    param(
        [string]$BasePath,
        [string]$Path
    )

    $baseFull = [System.IO.Path]::GetFullPath($BasePath)
    if (-not $baseFull.EndsWith([System.IO.Path]::DirectorySeparatorChar)) {
        $baseFull = $baseFull + [System.IO.Path]::DirectorySeparatorChar
    }

    $pathFull = [System.IO.Path]::GetFullPath($Path)
    $baseUri = New-Object System.Uri($baseFull)
    $pathUri = New-Object System.Uri($pathFull)
    return [System.Uri]::UnescapeDataString($baseUri.MakeRelativeUri($pathUri).ToString()).Replace("/", [System.IO.Path]::DirectorySeparatorChar)
}

function Replace-LegacyPackages {
    param([string]$Text)

    $replacements = [ordered]@{
        "com.xdev.xdevbase"             = "com.xdev.ooms.sharedkernel"
        "com.xdev.mailSender"           = "com.xdev.ooms.sharedkernel.mail"
        "com.xdev.onsignalNotifcations" = "com.xdev.ooms.sharedkernel.notifications"
        "com.osm.securityservice"       = "com.xdev.ooms.security"
        "com.osm.inventory_service"     = "com.xdev.ooms.inventory"
        "com.osm.conditioning"          = "com.xdev.ooms.conditioning"
        "com.osm.finance_service"       = "com.xdev.ooms.finance"
        "com.osm.oilproductionservice"  = "com.xdev.ooms.production"
        "com.osmrh.rh"                  = "com.xdev.ooms.hr"
        "com.osm.production"            = "com.xdev.ooms.conditioning.production"
    }

    $result = $Text
    foreach ($entry in $replacements.GetEnumerator()) {
        $result = $result.Replace($entry.Key, $entry.Value)
    }
    return $result
}

function Get-JavaPackageName {
    param([string]$Text)

    if ($Text -match "(?m)^\s*package\s+([a-zA-Z_][a-zA-Z0-9_]*(?:\.[a-zA-Z_][a-zA-Z0-9_]*)*)\s*;") {
        return $Matches[1]
    }
    return $null
}

function Copy-TextFile {
    param(
        [string]$SourcePath,
        [string]$TargetPath,
        [string]$Content
    )

    if ((Test-Path -LiteralPath $TargetPath) -and -not $Overwrite) {
        return "Skipped"
    }

    if (-not $DryRun) {
        $targetDirectory = Split-Path -Parent $TargetPath
        New-Item -ItemType Directory -Force -Path $targetDirectory | Out-Null
        Set-Content -LiteralPath $TargetPath -Value $Content -NoNewline
    }

    return "Copied"
}

function Copy-BinaryOrRawFile {
    param(
        [string]$SourcePath,
        [string]$TargetPath
    )

    if ((Test-Path -LiteralPath $TargetPath) -and -not $Overwrite) {
        return "Skipped"
    }

    if (-not $DryRun) {
        $targetDirectory = Split-Path -Parent $TargetPath
        New-Item -ItemType Directory -Force -Path $targetDirectory | Out-Null
        Copy-Item -LiteralPath $SourcePath -Destination $TargetPath -Force:$Overwrite
    }

    return "Copied"
}

$sourceRootFull = Resolve-FullPath $SourceRoot
$targetRootFull = Resolve-FullPath $TargetRoot

if (-not (Test-Path -LiteralPath $sourceRootFull)) {
    throw "Source root does not exist: $sourceRootFull"
}

if (-not (Test-Path -LiteralPath $targetRootFull)) {
    throw "Target root does not exist: $targetRootFull"
}

$moduleMap = @{
    inventory = @{
        SourceProject = "osm-pack"
        TargetPackage = "com.xdev.ooms.inventory"
    }
    conditioning = @{
        SourceProject = "osm-cond"
        TargetPackage = "com.xdev.ooms.conditioning"
    }
    finance = @{
        SourceProject = "osm-fin"
        TargetPackage = "com.xdev.ooms.finance"
    }
    production = @{
        SourceProject = "osm-prod"
        TargetPackage = "com.xdev.ooms.production"
    }
    hr = @{
        SourceProject = "osm-hr"
        TargetPackage = "com.xdev.ooms.hr"
    }
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$reportDir = Join-Path $targetRootFull "migration-reports"
$reportPath = Join-Path $reportDir "legacy-copy-$timestamp.md"

$summary = [ordered]@{}
$pendingFeign = New-Object System.Collections.Generic.List[string]
$pendingLegacyImports = New-Object System.Collections.Generic.List[string]

foreach ($module in $Modules) {
    if (-not $moduleMap.ContainsKey($module)) {
        throw "Unknown module '$module'. Valid modules: $($moduleMap.Keys -join ', ')"
    }

    $mapping = $moduleMap[$module]
    $sourceProject = Join-Path $sourceRootFull $mapping.SourceProject
    $targetModule = Join-Path $targetRootFull "modules\$module"

    if (-not (Test-Path -LiteralPath $sourceProject)) {
        throw "Source project does not exist: $sourceProject"
    }

    if (-not (Test-Path -LiteralPath $targetModule)) {
        throw "Target module does not exist: $targetModule"
    }

    $javaSourceRoot = Join-Path $sourceProject "src\main\java"
    $resourceSourceRoot = Join-Path $sourceProject "src\main\resources"
    $targetJavaRoot = Join-Path $targetModule "src\main\java"
    $legacyRoot = Join-Path $targetModule "legacy\$($mapping.SourceProject)"
    $legacyResourceRoot = Join-Path $targetModule "src\main\resources\legacy\$($mapping.SourceProject)"

    $moduleResult = [ordered]@{
        SourceProject = $mapping.SourceProject
        JavaCopied = 0
        JavaSkipped = 0
        BootClassesSkipped = 0
        ResourcesCopied = 0
        ResourcesSkipped = 0
        LegacyFilesCopied = 0
    }

    if (Test-Path -LiteralPath $javaSourceRoot) {
        Get-ChildItem -Path $javaSourceRoot -Recurse -File -Filter "*.java" | ForEach-Object {
            $sourceFile = $_.FullName
            $raw = Get-Content -Raw -LiteralPath $sourceFile

            if ($_.Name -match "Application\.java$" -or $raw -match "@SpringBootApplication") {
                $moduleResult.BootClassesSkipped++
                return
            }

            $rewritten = Replace-LegacyPackages $raw
            $newPackage = Get-JavaPackageName $rewritten

            if (-not $newPackage) {
                $relative = Get-RelativePathCompat -BasePath $javaSourceRoot -Path $sourceFile
                $targetFile = Join-Path $targetJavaRoot $relative
            } else {
                $packagePath = Convert-PackageToPath $newPackage
                $targetFile = Join-Path (Join-Path $targetJavaRoot $packagePath) $_.Name
            }

            $status = Copy-TextFile -SourcePath $sourceFile -TargetPath $targetFile -Content $rewritten
            if ($status -eq "Copied") {
                $moduleResult.JavaCopied++
            } else {
                $moduleResult.JavaSkipped++
            }

            if ($rewritten -match "org\.springframework\.cloud\.openfeign|@FeignClient|@EnableFeignClients") {
                $pendingFeign.Add($targetFile)
            }

            if ($rewritten -match "com\.xdev\.xdevsecurity|com\.xdev\.communicator|com\.xdev\.comunicator|com\.osm\.|com\.osmrh\.") {
                $pendingLegacyImports.Add($targetFile)
            }
        }
    }

    if (Test-Path -LiteralPath $resourceSourceRoot) {
        Get-ChildItem -Path $resourceSourceRoot -Recurse -File | ForEach-Object {
            $sourceFile = $_.FullName
            $relative = Get-RelativePathCompat -BasePath $resourceSourceRoot -Path $sourceFile
            $targetFile = Join-Path $legacyResourceRoot $relative
            $status = Copy-BinaryOrRawFile -SourcePath $sourceFile -TargetPath $targetFile
            if ($status -eq "Copied") {
                $moduleResult.ResourcesCopied++
            } else {
                $moduleResult.ResourcesSkipped++
            }
        }
    }

    $pomSource = Join-Path $sourceProject "pom.xml"
    if (Test-Path -LiteralPath $pomSource) {
        $pomTarget = Join-Path $legacyRoot "pom.xml"
        $status = Copy-BinaryOrRawFile -SourcePath $pomSource -TargetPath $pomTarget
        if ($status -eq "Copied") {
            $moduleResult.LegacyFilesCopied++
        }
    }

    $summary[$module] = $moduleResult
}

if (-not $DryRun) {
    New-Item -ItemType Directory -Force -Path $reportDir | Out-Null
}

$report = New-Object System.Collections.Generic.List[string]
$report.Add("# Legacy Module Copy Report")
$report.Add("")
$report.Add("- Source root: $sourceRootFull")
$report.Add("- Target root: $targetRootFull")
$report.Add("- Overwrite: $Overwrite")
$report.Add("- Dry run: $DryRun")
$report.Add("")
$report.Add("## Module Summary")
$report.Add("")
$report.Add("| Module | Source | Java copied | Java skipped | Boot skipped | Resources copied | Resources skipped |")
$report.Add("|---|---:|---:|---:|---:|---:|---:|")

foreach ($module in $summary.Keys) {
    $item = $summary[$module]
    $report.Add("| $module | $($item.SourceProject) | $($item.JavaCopied) | $($item.JavaSkipped) | $($item.BootClassesSkipped) | $($item.ResourcesCopied) | $($item.ResourcesSkipped) |")
}

$report.Add("")
$report.Add("## Pending Feign/Internal Port Conversion")
$report.Add("")
if ($pendingFeign.Count -eq 0) {
    $report.Add("None detected.")
} else {
    $pendingFeign | Sort-Object -Unique | ForEach-Object {
        $report.Add("- $_")
    }
}

$report.Add("")
$report.Add("## Pending Legacy Import Review")
$report.Add("")
if ($pendingLegacyImports.Count -eq 0) {
    $report.Add("None detected.")
} else {
    $pendingLegacyImports | Sort-Object -Unique | ForEach-Object {
        $report.Add("- $_")
    }
}

$report.Add("")
$report.Add("## Notes")
$report.Add("")
$report.Add("- Standalone Spring Boot application classes were skipped.")
$report.Add("- Old resources were copied under `src/main/resources/legacy/<old-project>` so they do not override monolith runtime configuration.")
$report.Add("- Old `pom.xml` files were copied under `legacy/<old-project>/pom.xml` for dependency review only.")
$report.Add("- Gateway and Eureka are intentionally not copied.")
$report.Add("- Security is intentionally not copied by this script because it has already been migrated with parity-first handling.")

if ($DryRun) {
    $report | ForEach-Object { Write-Output $_ }
} else {
    Set-Content -LiteralPath $reportPath -Value $report
    Write-Output "Legacy module copy completed."
    Write-Output "Report: $reportPath"
}
