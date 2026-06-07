param(
    [string]$SourceRoot = "C:\osmproject",
    [string]$TargetRoot = "C:\osmproject\OSM 2.0",
    [switch]$DryRun,
    [switch]$SkipImportRewrite
)

$ErrorActionPreference = "Stop"

function Resolve-FullPath {
    param([string]$Path)
    return [System.IO.Path]::GetFullPath($Path)
}

function Assert-PathInside {
    param(
        [string]$Path,
        [string]$Parent
    )

    $fullPath = Resolve-FullPath $Path
    $fullParent = Resolve-FullPath $Parent
    if (-not $fullParent.EndsWith([System.IO.Path]::DirectorySeparatorChar)) {
        $fullParent = $fullParent + [System.IO.Path]::DirectorySeparatorChar
    }

    if (-not $fullPath.StartsWith($fullParent, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to operate outside target root. Path: $fullPath Parent: $fullParent"
    }
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

function Remove-TargetPath {
    param([string]$Path)

    if (Test-Path -LiteralPath $Path) {
        if (-not $DryRun) {
            Remove-Item -LiteralPath $Path -Recurse -Force
        }
        return $true
    }
    return $false
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

        if (-not $packageName) {
            $result.MissingPackage++
            return
        }

        $packagePath = Convert-PackageToPath $packageName
        $targetFile = Join-Path (Join-Path $TargetJavaRoot $packagePath) $_.Name

        if (-not $DryRun) {
            New-Item -ItemType Directory -Force -Path (Split-Path -Parent $targetFile) | Out-Null
            Set-Content -LiteralPath $targetFile -Value $rewritten -NoNewline
        }

        $result.Copied++
    }

    return $result
}

function Copy-ReferencePom {
    param(
        [string]$Name,
        [string]$SourceProjectRoot,
        [string]$TargetReferenceRoot
    )

    $sourcePom = Join-Path $SourceProjectRoot "pom.xml"
    if (-not (Test-Path -LiteralPath $sourcePom)) {
        return 0
    }

    $targetPom = Join-Path (Join-Path $TargetReferenceRoot $Name) "pom.xml"
    if (-not $DryRun) {
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $targetPom) | Out-Null
        Copy-Item -LiteralPath $sourcePom -Destination $targetPom -Force
    }
    return 1
}

function Rewrite-ExistingProjectImports {
    param([string]$Root)

    $changed = 0
    Get-ChildItem -Path $Root -Recurse -File -Include "*.java" |
        Where-Object {
            $_.FullName -notmatch "\\target\\" -and
            $_.FullName -notmatch "\\legacy\\" -and
            $_.FullName -notmatch "\\scripts\\"
        } |
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

function Count-UnresolvedSupportImports {
    param([string]$Root)

    $patterns = @(
        "com\.xdev\.xdevbase",
        "com\.xdev\.mailSender",
        "com\.xdev\.onsignalNotifcations",
        "com\.xdev\.xdevsecurity",
        "com\.xdev\.communicator",
        "com\.xdev\.comunicator"
    )

    $count = 0
    Get-ChildItem -Path $Root -Recurse -File -Include "*.java" |
        Where-Object {
            $_.FullName -notmatch "\\target\\" -and
            $_.FullName -notmatch "\\legacy\\" -and
            $_.FullName -notmatch "\\scripts\\"
        } |
        ForEach-Object {
            $raw = Get-Content -Raw -LiteralPath $_.FullName
            foreach ($pattern in $patterns) {
                if ($raw -match $pattern) {
                    $count++
                    break
                }
            }
        }

    return $count
}

$sourceRootFull = Resolve-FullPath $SourceRoot
$targetRootFull = Resolve-FullPath $TargetRoot
$sharedKernelRoot = Join-Path $targetRootFull "modules\shared-kernel"
$sharedKernelJavaPackageRoot = Join-Path $sharedKernelRoot "src\main\java\com\xdev\ooms\sharedkernel"
$sharedKernelJavaRoot = Join-Path $sharedKernelRoot "src\main\java"
$legacyReferenceRoot = Join-Path $sharedKernelRoot "legacy\osm-parent"
$targetDir = Join-Path $sharedKernelRoot "target"

if (-not (Test-Path -LiteralPath $sourceRootFull)) {
    throw "Source root does not exist: $sourceRootFull"
}

if (-not (Test-Path -LiteralPath $sharedKernelRoot)) {
    throw "Shared-kernel module does not exist: $sharedKernelRoot"
}

Assert-PathInside -Path $sharedKernelJavaPackageRoot -Parent $targetRootFull
Assert-PathInside -Path $legacyReferenceRoot -Parent $targetRootFull
Assert-PathInside -Path $targetDir -Parent $targetRootFull

$removed = New-Object System.Collections.Generic.List[string]
foreach ($path in @($sharedKernelJavaPackageRoot, $legacyReferenceRoot, $targetDir)) {
    if (Remove-TargetPath -Path $path) {
        $removed.Add($path)
    }
}

$copyResults = New-Object System.Collections.Generic.List[object]
$referencePoms = 0

$xdevBaseRoot = Join-Path $sourceRootFull "osm-parent\xdev-base"
$xdevSecurityRoot = Join-Path $sourceRootFull "osm-parent\xdev-security"
$comunicatorRoot = Join-Path $sourceRootFull "osm-parent\comunicator"

$copyResults.Add((Copy-RewrittenJavaTree -Name "xdev-base" -SourceJavaRoot (Join-Path $xdevBaseRoot "src\main\java") -TargetJavaRoot $sharedKernelJavaRoot))
$copyResults.Add((Copy-RewrittenJavaTree -Name "xdev-security" -SourceJavaRoot (Join-Path $xdevSecurityRoot "src\main\java") -TargetJavaRoot $sharedKernelJavaRoot))
$copyResults.Add((Copy-RewrittenJavaTree -Name "comunicator" -SourceJavaRoot (Join-Path $comunicatorRoot "src\main\java") -TargetJavaRoot $sharedKernelJavaRoot))

$referencePoms += Copy-ReferencePom -Name "xdev-base" -SourceProjectRoot $xdevBaseRoot -TargetReferenceRoot $legacyReferenceRoot
$referencePoms += Copy-ReferencePom -Name "xdev-security" -SourceProjectRoot $xdevSecurityRoot -TargetReferenceRoot $legacyReferenceRoot
$referencePoms += Copy-ReferencePom -Name "comunicator" -SourceProjectRoot $comunicatorRoot -TargetReferenceRoot $legacyReferenceRoot

$rewrittenFiles = 0
if (-not $SkipImportRewrite) {
    $rewrittenFiles = Rewrite-ExistingProjectImports -Root $targetRootFull
}

$unresolvedCount = Count-UnresolvedSupportImports -Root $targetRootFull

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$reportDir = Join-Path $targetRootFull "migration-reports"
$reportPath = Join-Path $reportDir "shared-kernel-reset-$timestamp.md"

$report = New-Object System.Collections.Generic.List[string]
$report.Add("# Shared Kernel Reset Report")
$report.Add("")
$report.Add("- Source root: $sourceRootFull")
$report.Add("- Target root: $targetRootFull")
$report.Add("- Dry run: $DryRun")
$report.Add("- Import rewrite skipped: $SkipImportRewrite")
$report.Add("")
$report.Add("## Removed Paths")
$report.Add("")
if ($removed.Count -eq 0) {
    $report.Add("None.")
} else {
    $removed | ForEach-Object { $report.Add("- $_") }
}
$report.Add("")
$report.Add("## Copied Source")
$report.Add("")
$report.Add("| Source | Java files copied | Missing package |")
$report.Add("|---|---:|---:|")
foreach ($item in $copyResults) {
    $report.Add("| $($item.Name) | $($item.Copied) | $($item.MissingPackage) |")
}
$report.Add("")
$report.Add("## Import Rewrite")
$report.Add("")
$report.Add("- Existing project Java files rewritten: $rewrittenFiles")
$report.Add("- Unresolved old support imports after rewrite: $unresolvedCount")
$report.Add("- Legacy reference poms copied: $referencePoms")
$report.Add("")
$report.Add("## Package Mapping")
$report.Add("")
$report.Add('- `com.xdev.xdevbase` -> `com.xdev.ooms.sharedkernel`')
$report.Add('- `com.xdev.mailSender` -> `com.xdev.ooms.sharedkernel.mail`')
$report.Add('- `com.xdev.onsignalNotifcations` -> `com.xdev.ooms.sharedkernel.notifications`')
$report.Add('- `com.xdev.xdevsecurity` -> `com.xdev.ooms.sharedkernel.xdevsecurity`')
$report.Add('- `com.xdev.communicator` -> `com.xdev.ooms.sharedkernel.communicator`')
$report.Add('- `com.xdev.comunicator` -> `com.xdev.ooms.sharedkernel.communicator`')

if ($DryRun) {
    $report | ForEach-Object { Write-Output $_ }
} else {
    New-Item -ItemType Directory -Force -Path $reportDir | Out-Null
    Set-Content -LiteralPath $reportPath -Value $report
    Write-Output "Shared-kernel reset completed."
    Write-Output "Report: $reportPath"
}
