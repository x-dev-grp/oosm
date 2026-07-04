# Fix local login after OSM -> OOSM rename
# Run from repo:  powershell -File oosm/scripts/setup-local-login.ps1

$ErrorActionPreference = "Stop"
$scriptsDir = $PSScriptRoot

Write-Host "=== OOSM local login setup ===" -ForegroundColor Cyan

Push-Location $scriptsDir
try {
  if (-not (Test-Path "node_modules/pg")) {
    npm install pg bcryptjs --no-save | Out-Null
  }

  Write-Host "Migrating users osmuser -> oosmuser (if legacy table exists)..."
  node run-local-user-migration.cjs

  Write-Host "Resetting platform admin password to osmAdmin123..."
  node reset-local-user-password.cjs osmAdmin123 oosmAdmin
}
finally {
  Pop-Location
}

Write-Host ""
Write-Host "Done. Now RESTART the backend (stop + start in IDE or terminal)." -ForegroundColor Yellow
Write-Host "Ensure oosm/.env uses local DB:" -ForegroundColor Yellow
Write-Host "  DB_URL=jdbc:postgresql://localhost:5432/osm"
Write-Host "  DB_USER=postgres"
Write-Host "  DB_PASS=root"
Write-Host ""
Write-Host "Optional (resets admin password on every boot):" -ForegroundColor DarkGray
Write-Host "  SECURITY_BOOTSTRAP_ENABLED=true"
Write-Host "  SECURITY_BOOTSTRAP_PASSWORD=osmAdmin123"
Write-Host ""
Write-Host "Login at http://localhost:4200 with:" -ForegroundColor Green
Write-Host "  Username: oosmAdmin"
Write-Host "  Password: osmAdmin123"
