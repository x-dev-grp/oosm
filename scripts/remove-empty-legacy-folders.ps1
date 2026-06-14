# Remove empty legacy flat folders after co-location
$ErrorActionPreference = "SilentlyContinue"
$oosm = "C:\OSM 2.0\oosm\modules"

$modules = @(
    @{ Name = "production"; Folders = @("model","dto","service","controller","repository") },
    @{ Name = "finance"; Folders = @("model","dto","service","controller","repo") },
    @{ Name = "conditioning"; Folders = @("model","dto","service","controller","repository") },
    @{ Name = "inventory"; Folders = @("entity","dto","service","controller","repository") },
    @{ Name = "hr"; Folders = @("model","Dtos","service","controller","repo") },
    @{ Name = "security"; Folders = @("userManagement") }
)

$removed = 0
foreach ($mod in $modules) {
    $base = Join-Path $oosm "$($mod.Name)\src\main\java\com\xdev\ooms\$($mod.Name)"
    foreach ($folder in $mod.Folders) {
        $path = Join-Path $base $folder
        if (Test-Path $path) {
            Get-ChildItem -Path $path -Recurse -Directory | Sort-Object { $_.FullName.Length } -Descending | ForEach-Object {
                if (-not (Get-ChildItem $_.FullName -Recurse -File)) {
                    Remove-Item $_.FullName -Force
                    $removed++
                }
            }
            if (-not (Get-ChildItem $path -Recurse -File)) {
                Remove-Item $path -Recurse -Force
                $removed++
                Write-Host "Removed: $path"
            }
        }
    }
    # conditioning expedition/shipping model folders
    if ($mod.Name -eq "conditioning") {
        @("expedition\model", "shipping\model", "dto\analytics") | ForEach-Object {
            $path = Join-Path $base $_
            if ((Test-Path $path) -and -not (Get-ChildItem $path -Recurse -File)) {
                Remove-Item $path -Recurse -Force
                $removed++
                Write-Host "Removed: $path"
            }
        }
    }
    # security securityConfig entities/data if empty
    if ($mod.Name -eq "security") {
        @("securityConfig\entities", "securityConfig\data") | ForEach-Object {
            $path = Join-Path $base $_
            if ((Test-Path $path) -and -not (Get-ChildItem $path -Recurse -File)) {
                Remove-Item $path -Recurse -Force
                $removed++
                Write-Host "Removed: $path"
            }
        }
    }
}
Write-Host "Removed $removed empty folders."
