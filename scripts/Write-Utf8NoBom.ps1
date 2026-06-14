# Shared helper + standalone BOM stripper for oosm sources.
# Usage (dot-source):  . "$PSScriptRoot\Write-Utf8NoBom.ps1"
# Usage (fix files):    powershell -File "C:\OSM 2.0\oosm\scripts\Write-Utf8NoBom.ps1"
#                       powershell -File ...\Write-Utf8NoBom.ps1 -Root "C:\OSM 2.0\oosm" -Extensions java,ps1

param(
    [string]$Root = (Join-Path $PSScriptRoot ".."),
    [string[]]$Extensions = @(".java", ".ps1", ".xml", ".yml", ".yaml", ".properties", ".json", ".md"),
    [switch]$Recurse = $true,
    [switch]$WhatIf
)

function Get-Utf8NoBomEncoding {
    return New-Object System.Text.UTF8Encoding $false
}

function Test-Utf8Bom {
    param([byte[]]$Bytes)
    return $Bytes.Length -ge 3 -and $Bytes[0] -eq 0xEF -and $Bytes[1] -eq 0xBB -and $Bytes[2] -eq 0xBF
}

function Write-Utf8NoBomFile {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [Parameter(Mandatory = $true)]
        [string]$Content
    )
    $utf8 = Get-Utf8NoBomEncoding
    [System.IO.File]::WriteAllText($Path, $Content, $utf8)
}

function Remove-Utf8BomFromFile {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [switch]$WhatIf
    )

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        Write-Warning "Skip missing file: $Path"
        return $false
    }

    $bytes = [System.IO.File]::ReadAllBytes($Path)
    if (-not (Test-Utf8Bom $bytes)) {
        return $false
    }

    $utf8 = Get-Utf8NoBomEncoding
    $contentBytes = if ($bytes.Length -gt 3) { $bytes[3..($bytes.Length - 1)] } else { @() }
    $text = $utf8.GetString($contentBytes)

    if ($WhatIf) {
        Write-Host "[WhatIf] Would remove BOM: $Path"
        return $true
    }

    [System.IO.File]::WriteAllText($Path, $text, $utf8)
    Write-Host "Removed BOM: $Path"
    return $true
}

function Remove-Utf8BomFromTree {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Root,
        [string[]]$Extensions,
        [switch]$Recurse,
        [switch]$WhatIf
    )

    $rootPath = (Resolve-Path -LiteralPath $Root).Path
    $normalizedExtensions = @(
        foreach ($ext in $Extensions) {
            if ([string]::IsNullOrWhiteSpace($ext)) { continue }
            $value = $ext.Trim().ToLowerInvariant()
            if (-not $value.StartsWith('.')) { $value = ".$value" }
            $value
        }
    ) | Select-Object -Unique

    Write-Host "Scanning: $rootPath"
    Write-Host "Extensions: $($normalizedExtensions -join ', ')"

    $searchParams = @{
        Path        = $rootPath
        File        = $true
        ErrorAction = "SilentlyContinue"
    }
    if ($Recurse) {
        $searchParams["Recurse"] = $true
    }

    $files = Get-ChildItem @searchParams | Where-Object {
        $ext = $_.Extension.ToLowerInvariant()
        $normalizedExtensions -contains $ext
    }

    $scanned = 0
    $fixed = 0

    foreach ($file in $files) {
        $scanned++
        if (Remove-Utf8BomFromFile -Path $file.FullName -WhatIf:$WhatIf) {
            $fixed++
        }
    }

    Write-Host ""
    Write-Host "Done. Scanned $scanned file(s), fixed $fixed file(s) with UTF-8 BOM."
    if ($fixed -eq 0) {
        Write-Host "No UTF-8 BOM found (files already clean for selected extensions)."
    }
}

# When dot-sourced, only export helpers. When run with -File, strip BOM under Root.
$invocation = $MyInvocation.InvocationName
$isDotSourced = ($null -ne $invocation -and ($invocation -eq '.' -or $invocation -eq '&'))

if (-not $isDotSourced) {
    $ErrorActionPreference = "Stop"
    Remove-Utf8BomFromTree -Root $Root -Extensions $Extensions -Recurse:$Recurse -WhatIf:$WhatIf
}
