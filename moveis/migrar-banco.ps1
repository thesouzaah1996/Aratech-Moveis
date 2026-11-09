param([ValidateSet('validar','migrar','informar','adotar')][string]$Comando='migrar', [string]$BancoEsperado,
      [ValidateSet('dev','homolog','hologo','prod')][string]$Ambiente)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$Comando = $Comando.ToLowerInvariant()
if ($Ambiente) { $Ambiente = $Ambiente.ToLowerInvariant() }
. (Join-Path $PSScriptRoot 'scripts/banco-comum.ps1')
if (!$Ambiente) { $ambiente = Perfil-Selecionado $PSScriptRoot }
elseif ($Ambiente -eq 'hologo') { $ambiente = 'homolog' }
$sufixo = $ambiente.ToUpperInvariant()
$configuracao = Ler-VariaveisBanco (Join-Path $PSScriptRoot '.env.docker-local')
$segredos = Ler-VariaveisBanco (Join-Path $PSScriptRoot '.docker-local/credenciais-banco.env')
$banco = $configuracao['POSTGRES_DB_' + $sufixo]
if (!$banco -or !$segredos['FLYWAY_USER_' + $sufixo]) { throw 'Prepare as contas com preparar-permissoes-bancos.ps1.' }
if ($Comando -eq 'adotar' -and ($BancoEsperado -cne $banco)) { throw 'Para adotar, informe -BancoEsperado com o nome exato do banco revisado.' }
if ($BancoEsperado -and $BancoEsperado -cne $banco) { throw 'Banco selecionado diferente do banco esperado.' }
if ($Comando -in @('migrar','adotar')) { $null = Backup-Banco $PSScriptRoot $ambiente }
$variaveis = @(
    'ARATECH_MIGRACAO_URL=jdbc:postgresql://aramoveis-' + $ambiente + ':5432/' + $banco
    'ARATECH_MIGRACAO_USUARIO=' + $segredos['FLYWAY_USER_' + $sufixo]
    'ARATECH_MIGRACAO_SENHA=' + $segredos['FLYWAY_PASSWORD_' + $sufixo]
    'ARATECH_APLICACAO_USUARIO=' + $segredos['APP_USER_' + $sufixo]
    'ARATECH_MIGRACAO_BANCO_ESPERADO=' + $banco
    'ARATECH_MIGRACAO_ESQUEMA=public'
)
$arquivoJob = Join-Path $PSScriptRoot ('.docker-local/flyway-job-' + $ambiente + '-' + [Guid]::NewGuid().ToString('N') + '.env')
[IO.File]::WriteAllLines($arquivoJob,$variaveis,(New-Object Text.UTF8Encoding($false)))
$rede = 'aratechmoveis'
try {
    & docker run --rm --network $rede --env-file $arquivoJob --entrypoint java aratechmoveis-backend:local `
        '-Dloader.main=com.aratech.moveis.migracao.MigracaoApplication' -cp /app/app.jar org.springframework.boot.loader.launch.PropertiesLauncher $Comando
    Conferir-Docker "Flyway $Comando em $ambiente"
} finally { Remove-Item -LiteralPath $arquivoJob -ErrorAction SilentlyContinue }
Write-Host "Flyway $Comando concluido em $ambiente. A tarefa temporaria foi removida."
