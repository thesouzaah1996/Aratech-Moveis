$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
. (Join-Path $PSScriptRoot 'scripts/banco-comum.ps1')
$config = Ler-VariaveisBanco (Join-Path $PSScriptRoot '.env')
$contas = Ler-VariaveisBanco (Join-Path $PSScriptRoot '.docker-local/credenciais-banco.env')
if (!$contas['FLYWAY_USER_DEV']) { throw 'Prepare as contas de banco antes dos testes.' }
& .\mvnw.cmd -DskipTests package
if ($LASTEXITCODE -ne 0) { throw 'Falha ao preparar o pacote para testes.' }
$schemaTeste = 'flyway_validacao_' + [Guid]::NewGuid().ToString('N')
$migrador = $contas['FLYWAY_USER_DEV']
if ($migrador -notmatch '^aratech_flyway_dev$') { throw 'Conta de migracao de teste inesperada.' }
$criarSchema = 'psql -q -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "CREATE SCHEMA ' + $schemaTeste + ' AUTHORIZATION ' + $migrador + ';"'
Executar-ShellBanco 'aratechmoveis-database-dev' $criarSchema
try {
    $inicio = New-Object Diagnostics.ProcessStartInfo
    $inicio.FileName = 'java'
    $inicio.Arguments = '-Dloader.main=com.aratech.moveis.migracao.MigracaoApplication -cp "' + (Join-Path $PSScriptRoot 'target/moveis-0.0.1-SNAPSHOT.jar') + '" org.springframework.boot.loader.launch.PropertiesLauncher migrar'
    $inicio.UseShellExecute = $false; $inicio.CreateNoWindow = $true
    $inicio.EnvironmentVariables['ARATECH_MIGRACAO_URL'] = 'jdbc:postgresql://' + $config['DB_HOST_DEV'] + ':' + $config['DB_PORT_DEV'] + '/' + $config['POSTGRES_DB_DEV']
    $inicio.EnvironmentVariables['ARATECH_MIGRACAO_USUARIO'] = $migrador
    $inicio.EnvironmentVariables['ARATECH_MIGRACAO_SENHA'] = $contas['FLYWAY_PASSWORD_DEV']
    $inicio.EnvironmentVariables['ARATECH_APLICACAO_USUARIO'] = $contas['APP_USER_DEV']
    $inicio.EnvironmentVariables['ARATECH_MIGRACAO_BANCO_ESPERADO'] = $config['POSTGRES_DB_DEV']
    $inicio.EnvironmentVariables['ARATECH_MIGRACAO_ESQUEMA'] = $schemaTeste
    $processo = [Diagnostics.Process]::Start($inicio); $processo.WaitForExit()
    if ($processo.ExitCode -ne 0) { throw 'Falha ao migrar o schema exclusivo dos testes.' }
    & .\mvnw.cmd '-Dtest=*Test,*Tests,*IT' "-Dspring.flyway.default-schema=$schemaTeste" "-Dspring.flyway.schemas=$schemaTeste" "-Dspring.datasource.hikari.schema=$schemaTeste" test
    if ($LASTEXITCODE -ne 0) { throw 'A suite de testes apresentou falhas.' }
} finally {
    if ($schemaTeste -match '^flyway_validacao_[a-f0-9]{32}$') {
        $remover = 'psql -q -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "DROP SCHEMA IF EXISTS ' + $schemaTeste + ' CASCADE;"'
        Executar-ShellBanco 'aratechmoveis-database-dev' $remover
    }
}
Write-Host 'Suite completa aprovada. O schema exclusivo de teste foi removido; os dados principais foram preservados.'
