param([ValidateSet('simular','inserir','validar')][string]$Comando='simular')
$ErrorActionPreference = 'Stop'
$raizProjeto = Split-Path $PSScriptRoot -Parent
. (Join-Path $PSScriptRoot 'banco-comum.ps1')
if ((Perfil-Selecionado $raizProjeto) -ne 'dev') { throw 'Este script exige o perfil dev ativo.' }
$carga = [IO.File]::ReadAllText((Join-Path $PSScriptRoot 'dados-demonstracao-dev.sql'))
$validacao = [IO.File]::ReadAllText((Join-Path $PSScriptRoot 'validar-dados-demonstracao-dev.sql'))
if ($Comando -eq 'validar') { $sql = $validacao }
else {
    $sql = $carga.Replace('\ir validar-dados-demonstracao-dev.sql',$validacao)
    if ($Comando -eq 'simular') { $sql = $sql.Replace('-- FINALIZAR' + "`nCOMMIT;",'ROLLBACK;').Replace('-- FINALIZAR' + "`r`nCOMMIT;",'ROLLBACK;') }
}
if ($Comando -eq 'inserir') { $null = Backup-Banco $raizProjeto 'dev' }
$prefixo = @'
set -eu
[ "$POSTGRES_DB" = "aratechmoveis_dev" ] || exit 1
psql -X -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 <<'ARATECH_DEMO_SQL'
'@
Executar-ShellBanco 'aratechmoveis-database-dev' ($prefixo + "`n" + $sql + "`nARATECH_DEMO_SQL`n")
Write-Host "Dados de demonstração: $Comando concluído somente em dev."
