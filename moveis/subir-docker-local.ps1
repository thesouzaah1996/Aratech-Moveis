param([switch]$ReconstruirImagens)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
function Conferir([string]$etapa) {
    if ($LASTEXITCODE -ne 0) { throw "Falha em $etapa (codigo $LASTEXITCODE)." }
}
if (!(Test-Path '.env.docker-local')) { throw 'Configure o arquivo privado .env.docker-local conforme docs/docker-local.md.' }
if (!(Test-Path '.docker-local/certs/localhost.crt')) { throw 'Configure o certificado HTTPS local conforme docs/docker-local.md.' }
$compose = @('compose','--env-file','.env.docker-local','-p','moveis','-f','docker-compose.yml')
& docker @compose config --quiet
Conferir 'validacao do Compose'
if ($ReconstruirImagens) {
    & docker @compose build backend frontend
    Conferir 'build das imagens'
}
& docker @compose up -d --wait aramoveis-dev aramoveis-homolog aramoveis-prod minio
Conferir 'disponibilidade dos bancos'
& (Join-Path $PSScriptRoot 'migrar-banco.ps1') -Comando migrar
& docker @compose up -d --no-build --force-recreate backend frontend
Conferir 'inicializacao da aplicacao'
Write-Host 'Aplicacao unica: https://localhost:8443'
Write-Host 'Ambiente selecionado em src/main/resources/application.yml -> spring.profiles.active.'
