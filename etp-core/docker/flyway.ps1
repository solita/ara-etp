#!/usr/bin/env pwsh
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet("migrate", "clean")]
    [string]$Command
)

$scriptDir = $PSScriptRoot
Push-Location (Join-Path $scriptDir '..\etp-db')
try {
    # Don't run test migrations to template db
    clojure -M -m solita.etp.db.flywaydb $Command

    # Run test migrations to etp_dev
    $env:DB_URL = "jdbc:postgresql://localhost:5432/etp_dev"
    clojure -M:dev -m solita.etp.db.flywaydb $Command
} finally {
    Pop-Location
}
