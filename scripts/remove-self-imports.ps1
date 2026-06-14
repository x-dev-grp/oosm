# Remove erroneous self-imports added by import injection script
$ErrorActionPreference = "Stop"
. "$PSScriptRoot\Write-Utf8NoBom.ps1"
$root = "C:\OSM 2.0\oosm\modules"

$javaFiles = Get-ChildItem -Path $root -Recurse -Filter "*.java" | Where-Object { $_.FullName -notmatch '\\legacy\\' }

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    if ($content -notmatch 'package ([^;]+);') { continue }
    $package = $Matches[1]
    $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $selfImport = "import $package.$className;"
    if ($content -notmatch [regex]::Escape($selfImport)) { continue }
    $newContent = $content.Replace("$selfImport`n", "").Replace($selfImport, "")
    Write-Utf8NoBomFile -Path $file.FullName -Content $newContent
    Write-Host "Removed self-import in $($file.Name)"
}

Write-Host "Self-import cleanup complete."
