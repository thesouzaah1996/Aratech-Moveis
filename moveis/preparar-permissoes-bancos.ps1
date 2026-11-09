$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
. (Join-Path $PSScriptRoot 'scripts/banco-comum.ps1')
$configuracao = Ler-VariaveisBanco (Join-Path $PSScriptRoot '.env')
$segredosPath = Join-Path $PSScriptRoot '.docker-local/credenciais-banco.env'
$segredos = Ler-VariaveisBanco $segredosPath
New-Item -ItemType Directory -Force '.docker-local' | Out-Null
foreach ($ambiente in @('dev','homolog','prod')) {
    $sufixo = $ambiente.ToUpperInvariant()
    $appChave = 'APP_USER_' + $sufixo; $appSenha = 'APP_PASSWORD_' + $sufixo
    $migChave = 'FLYWAY_USER_' + $sufixo; $migSenha = 'FLYWAY_PASSWORD_' + $sufixo
    if (!$segredos.ContainsKey($appChave)) {
        $segredos[$appChave] = 'aratech_app_' + $ambiente
        $segredos[$migChave] = 'aratech_flyway_' + $ambiente
        foreach ($chaveSenha in @($appSenha,$migSenha)) {
            $bytes = New-Object byte[] 32
            $rng = [Security.Cryptography.RandomNumberGenerator]::Create(); $rng.GetBytes($bytes); $rng.Dispose()
            $segredos[$chaveSenha] = [BitConverter]::ToString($bytes).Replace('-','').ToLowerInvariant()
        }
    }
    if ($segredos[$appChave] -cne ('aratech_app_' + $ambiente) -or $segredos[$migChave] -cne ('aratech_flyway_' + $ambiente)) { throw 'Role operacional inesperada.' }
    foreach ($senha in @($segredos[$appSenha],$segredos[$migSenha])) { if ($senha -notmatch '^[a-f0-9]{64}$') { throw 'Credencial operacional invalida.' } }
    # Persiste antes de criar as roles para não perder as credenciais em uma interrupção.
    [IO.File]::WriteAllLines($segredosPath, @($segredos.Keys | Sort-Object | ForEach-Object { $_ + '=' + $segredos[$_] }), (New-Object Text.UTF8Encoding($false)))
    $null = Backup-Banco $PSScriptRoot $ambiente
    $container = "aratechmoveis-database-$ambiente"
    $gerenciadas = if ($segredos['GERENCIADO_' + $sufixo] -eq 'true') { 'true' } else { 'false' }
    $sql = "\set roles_gerenciadas '$gerenciadas'`n\set app_user '" + $segredos[$appChave] + "'`n\set app_password '" + $segredos[$appSenha] + "'`n\set migration_user '" + $segredos[$migChave] + "'`n\set migration_password '" + $segredos[$migSenha] + "'`n"
    $sql += [IO.File]::ReadAllText((Join-Path $PSScriptRoot 'db/preparar-permissoes.sql'))
    $script = 'set -eu' + "`n" + 'psql -q -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<''ARATECH_SQL''' + "`n" + $sql + "`nARATECH_SQL`n"
    Executar-ShellBanco $container $script
    $segredos['GERENCIADO_' + $sufixo] = 'true'
    [IO.File]::WriteAllLines($segredosPath, @($segredos.Keys | Sort-Object | ForEach-Object { $_ + '=' + $segredos[$_] }), (New-Object Text.UTF8Encoding($false)))
    Write-Host "Contas separadas e propriedade do schema preparadas em $ambiente."
}
foreach ($arquivo in @('.env','.env.docker-local')) {
    $destino = Join-Path $PSScriptRoot $arquivo
    $linhas = @([IO.File]::ReadAllLines($destino) | Where-Object { $_ -notmatch '^(APP_USER_|APP_PASSWORD_|FLYWAY_USER_|FLYWAY_PASSWORD_)' })
    $linhas += @($segredos.Keys | Where-Object { $_ -notmatch '^GERENCIADO_' } | Sort-Object | ForEach-Object { $_ + '=' + $segredos[$_] })
    [IO.File]::WriteAllLines($destino,$linhas,(New-Object Text.UTF8Encoding($false)))
}
Write-Host 'Credenciais locais atualizadas sem exibir senhas. A aplicacao deve ser recriada com a nova imagem.'
