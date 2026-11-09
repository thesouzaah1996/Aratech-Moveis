# Funções compartilhadas pelos comandos de administração/migração. Não imprime segredos.
function Ler-VariaveisBanco([string]$arquivo) {
    $valores = @{}
    if (Test-Path -LiteralPath $arquivo) {
        foreach ($linha in [IO.File]::ReadAllLines($arquivo)) {
            if ($linha -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=(.*)$') { $valores[$matches[1]] = $matches[2].Trim().Trim('"').Trim("'") }
        }
    }
    return $valores
}
function Conferir-Docker([string]$etapa) {
    if ($LASTEXITCODE -ne 0) { throw "Falha em $etapa (codigo $LASTEXITCODE)." }
}
function Perfil-Selecionado([string]$raiz) {
    $yaml = [IO.File]::ReadAllText((Join-Path $raiz 'src/main/resources/application.yml'))
    $perfil = [regex]::Match($yaml,'(?m)^\s{2}profiles:\s*\r?\n\s{4}active:\s*["'']?(dev|homolog|hologo|prod)["'']?\s*(?:#.*)?$')
    if (!$perfil.Success) { throw 'Configure spring.profiles.active com dev, homolog/hologo ou prod.' }
    $nome = $perfil.Groups[1].Value
    if ($nome -eq 'hologo') { return 'homolog' }
    return $nome
}
function Executar-ShellBanco([string]$container, [string]$script) {
    if ($container -notmatch '^aratechmoveis-database-(dev|homolog|prod)$') { throw 'Container de banco inesperado.' }
    $inicio = New-Object Diagnostics.ProcessStartInfo
    $inicio.FileName = 'docker'
    $inicio.Arguments = "exec -i $container sh"
    $inicio.UseShellExecute = $false
    $inicio.CreateNoWindow = $true
    $inicio.RedirectStandardInput = $true
    $processo = [Diagnostics.Process]::Start($inicio)
    $entrada = [Text.Encoding]::UTF8.GetBytes($script.Replace("`r`n","`n") + "`n")
    $processo.StandardInput.BaseStream.Write($entrada,0,$entrada.Length)
    $processo.StandardInput.Close()
    $processo.WaitForExit()
    if ($processo.ExitCode -ne 0) { throw 'Operacao administrativa no banco falhou.' }
}
function Backup-Banco([string]$raiz, [string]$ambiente) {
    if ($ambiente -notin @('dev','homolog','prod')) { throw 'Ambiente invalido.' }
    $container = "aratechmoveis-database-$ambiente"
    $identificador = [Guid]::NewGuid().ToString('N')
    $bancoTeste = 'aratech_restore_check_' + $identificador
    $dumpTemporario = '/tmp/aratech-flyway-backup-' + $identificador + '.dump'
    $pasta = Join-Path $raiz ('.rollback-backups/flyway-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + $ambiente + '-' + $identificador.Substring(0,8))
    New-Item -ItemType Directory -Force $pasta | Out-Null
    $script = @'
set -eu
check_db=__BANCO_TESTE__
trap 'dropdb --if-exists -U "$POSTGRES_USER" "$check_db" >/dev/null' EXIT
pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc -f __DUMP_TEMPORARIO__
createdb -U "$POSTGRES_USER" -T template0 "$check_db"
pg_restore -U "$POSTGRES_USER" -d "$check_db" --no-owner --no-privileges --exit-on-error __DUMP_TEMPORARIO__
'@
    Executar-ShellBanco $container $script.Replace('__BANCO_TESTE__',$bancoTeste).Replace('__DUMP_TEMPORARIO__',$dumpTemporario)
    & docker cp ($container + ':' + $dumpTemporario) (Join-Path $pasta 'banco.dump')
    Conferir-Docker 'copia do backup'
    & docker exec $container rm -f $dumpTemporario
    Conferir-Docker 'limpeza da copia temporaria do backup'
    Write-Host "Backup gerado e restauracao integral testada: $pasta"
    return $pasta
}
