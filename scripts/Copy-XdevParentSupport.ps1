param(
    [string]$SourceRoot = "C:\osmproject",
    [string]$TargetRoot = "C:\osmproject\OSM 2.0",
    [switch]$Overwrite,
    [switch]$DryRun,
    [switch]$SkipXdevBase,
    [switch]$SkipXdevSecurity,
    [switch]$SkipComunicator,
    [switch]$NoRewriteExistingImports
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

function Get-JavaPackageName {
    param([string]$Text)

    if ($Text -match "(?m)^\s*package\s+([a-zA-Z_][a-zA-Z0-9_]*(?:\.[a-zA-Z_][a-zA-Z0-9_]*)*)\s*;") {
        return $Matches[1]
    }
    return $null
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

function Rewrite-XdevSupportPackages {
    param([string]$Text)

    $replacements = [ordered]@{
        "com.xdev.xdevbase"             = "com.xdev.ooms.sharedkernel"
        "com.xdev.mailSender"           = "com.xdev.ooms.sharedkernel.mail"
        "com.xdev.onsignalNotifcations" = "com.xdev.ooms.sharedkernel.notifications"
        "com.xdev.xdevsecurity"         = "com.xdev.ooms.sharedkernel.xdevsecurity"
        "com.xdev.communicator"         = "com.xdev.ooms.sharedkernel.communicator"
        "com.xdev.comunicator"          = "com.xdev.ooms.sharedkernel.communicator"
    }

    $result = $Text
    foreach ($entry in $replacements.GetEnumerator()) {
        $result = $result.Replace($entry.Key, $entry.Value)
    }
    return $result
}

function Copy-RewrittenJavaTree {
    param(
        [string]$Name,
        [string]$SourceJavaRoot,
        [string]$TargetJavaRoot
    )

    $result = [ordered]@{
        Name = $Name
        Copied = 0
        Skipped = 0
        MissingPackage = 0
    }

    if (-not (Test-Path -LiteralPath $SourceJavaRoot)) {
        throw "Source Java root does not exist: $SourceJavaRoot"
    }

    Get-ChildItem -Path $SourceJavaRoot -Recurse -File -Filter "*.java" | ForEach-Object {
        $sourceFile = $_.FullName
        $raw = Get-Content -Raw -LiteralPath $sourceFile
        $rewritten = Rewrite-XdevSupportPackages $raw
        $packageName = Get-JavaPackageName $rewritten

        if ($packageName) {
            $packagePath = Convert-PackageToPath $packageName
            $targetFile = Join-Path (Join-Path $TargetJavaRoot $packagePath) $_.Name
        } else {
            $relative = Get-RelativePathCompat -BasePath $SourceJavaRoot -Path $sourceFile
            $targetFile = Join-Path $TargetJavaRoot $relative
            $result.MissingPackage++
        }

        if ((Test-Path -LiteralPath $targetFile) -and -not $Overwrite) {
            $result.Skipped++
            return
        }

        if (-not $DryRun) {
            $targetDirectory = Split-Path -Parent $targetFile
            New-Item -ItemType Directory -Force -Path $targetDirectory | Out-Null
            Set-Content -LiteralPath $targetFile -Value $rewritten -NoNewline
        }

        $result.Copied++
    }

    return $result
}

function Copy-LegacyReferenceFiles {
    param(
        [string]$SourceProjectRoot,
        [string]$TargetReferenceRoot
    )

    $copied = 0
    $files = @("pom.xml")

    foreach ($file in $files) {
        $sourceFile = Join-Path $SourceProjectRoot $file
        if (-not (Test-Path -LiteralPath $sourceFile)) {
            continue
        }

        $targetFile = Join-Path $TargetReferenceRoot $file
        if ((Test-Path -LiteralPath $targetFile) -and -not $Overwrite) {
            continue
        }

        if (-not $DryRun) {
            New-Item -ItemType Directory -Force -Path (Split-Path -Parent $targetFile) | Out-Null
            Copy-Item -LiteralPath $sourceFile -Destination $targetFile -Force:$Overwrite
        }
        $copied++
    }

    return $copied
}

function Rewrite-ExistingImports {
    param([string]$Root)

    $changed = 0
    Get-ChildItem -Path $Root -Recurse -File -Include "*.java","*.xml","*.yml","*.yaml" |
        Where-Object { $_.FullName -notmatch "\\target\\" -and $_.FullName -notmatch "\\legacy\\" } |
        ForEach-Object {
            $path = $_.FullName
            $raw = Get-Content -Raw -LiteralPath $path
            $rewritten = Rewrite-XdevSupportPackages $raw
            if ($rewritten -ne $raw) {
                if (-not $DryRun) {
                    Set-Content -LiteralPath $path -Value $rewritten -NoNewline
                }
                $changed++
            }
        }

    return $changed
}

$sourceRootFull = Resolve-FullPath $SourceRoot
$targetRootFull = Resolve-FullPath $TargetRoot
$sharedKernelRoot = Join-Path $targetRootFull "modules\shared-kernel"
$targetJavaRoot = Join-Path $sharedKernelRoot "src\main\java"
$referenceRoot = Join-Path $sharedKernelRoot "legacy\osm-parent"

if (-not (Test-Path -LiteralPath $sourceRootFull)) {
    throw "Source root does not exist: $sourceRootFull"
}

if (-not (Test-Path -LiteralPath $sharedKernelRoot)) {
    throw "Shared-kernel module does not exist: $sharedKernelRoot"
}

$copyResults = New-Object System.Collections.Generic.List[object]
$referenceCount = 0

if (-not $SkipXdevBase) {
    $projectRoot = Join-Path $sourceRootFull "osm-parent\xdev-base"
    $javaRoot = Join-Path $projectRoot "src\main\java"
    $copyResults.Add((Copy-RewrittenJavaTree -Name "xdev-base" -SourceJavaRoot $javaRoot -TargetJavaRoot $targetJavaRoot))
    $referenceCount += Copy-LegacyReferenceFiles -SourceProjectRoot $projectRoot -TargetReferenceRoot (Join-Path $referenceRoot "xdev-base")
}

if (-not $SkipXdevSecurity) {
    $projectRoot = Join-Path $sourceRootFull "osm-parent\xdev-security"
    $javaRoot = Join-Path $projectRoot "src\main\java"
    $copyResults.Add((Copy-RewrittenJavaTree -Name "xdev-security" -SourceJavaRoot $javaRoot -TargetJavaRoot $targetJavaRoot))
    $referenceCount += Copy-LegacyReferenceFiles -SourceProjectRoot $projectRoot -TargetReferenceRoot (Join-Path $referenceRoot "xdev-security")
}

if (-not $SkipComunicator) {
    $projectRoot = Join-Path $sourceRootFull "osm-parent\comunicator"
    $javaRoot = Join-Path $projectRoot "src\main\java"
    $copyResults.Add((Copy-RewrittenJavaTree -Name "comunicator" -SourceJavaRoot $javaRoot -TargetJavaRoot $targetJavaRoot))
    $referenceCount += Copy-LegacyReferenceFiles -SourceProjectRoot $projectRoot -TargetReferenceRoot (Join-Path $referenceRoot "comunicator")
}

$rewrittenExisting = 0
if (-not $NoRewriteExistingImports) {
    $rewrittenExisting = Rewrite-ExistingImports -Root $targetRootFull
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$reportDir = Join-Path $targetRootFull "migration-reports"
$reportPath = Join-Path $reportDir "xdev-parent-copy-$timestamp.md"

$report = New-Object System.Collections.Generic.List[string]
$report.Add("# Xdev Parent Support Copy Report")
$report.Add("")
$report.Add("- Source root: $sourceRootFull")
$report.Add("- Target root: $targetRootFull")
$report.Add("- Overwrite: $Overwrite")
$report.Add("- Dry run: $DryRun")
$report.Add("- Rewrite existing imports: $(-not $NoRewriteExistingImports)")
$report.Add("")
$report.Add("## Copied Libraries")
$report.Add("")
$report.Add("| Library | Copied | Skipped | Missing package |")
$report.Add("|---|---:|---:|---:|")
foreach ($item in $copyResults) {
    $report.Add("| $($item.Name) | $($item.Copied) | $($item.Skipped) | $($item.MissingPackage) |")
}
$report.Add("")
$report.Add("## Other Changes")
$report.Add("")
$report.Add("- Legacy reference files copied: $referenceCount")
$report.Add("- Existing files with rewritten imports: $rewrittenExisting")
$report.Add("")
$report.Add("## Package Mapping")
$report.Add("")
$report.Add('- `com.xdev.xdevbase` -> `com.xdev.ooms.sharedkernel`')
$report.Add('- `com.xdev.mailSender` -> `com.xdev.ooms.sharedkernel.mail`')
$report.Add('- `com.xdev.onsignalNotifcations` -> `com.xdev.ooms.sharedkernel.notifications`')
$report.Add('- `com.xdev.xdevsecurity` -> `com.xdev.ooms.sharedkernel.xdevsecurity`')
$report.Add('- `com.xdev.communicator` -> `com.xdev.ooms.sharedkernel.communicator`')
$report.Add('- `com.xdev.comunicator` -> `com.xdev.ooms.sharedkernel.communicator`')
$report.Add("")
$report.Add("## Notes")
$report.Add("")
$report.Add('- This script copies support-library source into `modules/shared-kernel`.')
$report.Add("- It does not delete any existing code.")
$report.Add("- Use `-Overwrite` to refresh existing copied files.")
$report.Add("- `xdev-security` and `comunicator` may require Spring Cloud OpenFeign and Resilience4j dependencies if their Feign classes remain compiled.")

if ($DryRun) {
    $report | ForEach-Object { Write-Output $_ }
} else {
    New-Item -ItemType Directory -Force -Path $reportDir | Out-Null
    Set-Content -LiteralPath $reportPath -Value $report
    Write-Output "Xdev parent support copy completed."
    Write-Output "Report: $reportPath"
}
