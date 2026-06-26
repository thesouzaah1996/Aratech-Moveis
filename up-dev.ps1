$changed = git status --porcelain | ForEach-Object {
    if ($_ -match "^.{2} (.+)$") { $matches[1].Trim() }
}

$map = @{
    "aratech-frontend" = "aratechFrontend/"
    "almoxarifado"     = "almoxarifado/"
    "login"            = "login/"
    "portaria"         = "portaria/"
}

$toBuild = $map.Keys | Where-Object {
    $prefix = $map[$_]
    $changed | Where-Object { $_ -like "$prefix*" }
}

$base = "docker compose -f docker-compose.yml -f docker-compose-dev.yml"

if ($toBuild) {
    $lista = $toBuild -join " "
    Write-Host "Rebuildando: $lista"
    Invoke-Expression "$base build $lista"
}

Write-Host "Subindo todos os containers..."
Invoke-Expression "$base up -d"
