param(
  [switch]$Etp2026
)

if ($Etp2026) {
  $env:ETP_2026 = 'true'
} else {
  $env:ETP_2026 = 'false'
}

Invoke-Command -ScriptBlock {
    Set-PSDebug -Trace 1

    docker compose up --build -d frontend
    docker compose up --build etp-db-for-etp_dev
    docker compose exec db dropdb -U postgres cypress_test --if-exists --force
    docker compose exec db createdb -U postgres -T etp_dev cypress_test
    Write-Output "Waiting for frontend to be healthy. Can take ~20s."
    docker compose up --build --wait frontend --wait backend

    Set-PSDebug -Off
}
