$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$compose = @('compose','--env-file','.env.docker-local','-p','moveis','-f','docker-compose.yml')
$pronto = $false
for ($tentativa = 0; $tentativa -lt 90; $tentativa++) {
    $codigo = & curl.exe --silent --insecure --noproxy '*' --connect-timeout 3 --max-time 5 `
        --output NUL --write-out '%{http_code}' 'https://localhost:8443/aratech/auth/csrf'
    if ($LASTEXITCODE -eq 0 -and $codigo -eq '200') { $pronto = $true; break }
    Start-Sleep -Seconds 2
}
if (!$pronto) { throw 'Backend indisponivel pelo frontend.' }
foreach ($pagina in @('/login','/documentacao')) {
    $codigo = & curl.exe --silent --insecure --noproxy '*' --max-time 10 --output NUL --write-out '%{http_code}' ('https://localhost:8443' + $pagina)
    if ($LASTEXITCODE -ne 0 -or $codigo -ne '200') { throw "Pagina indisponivel: $pagina" }
}
$semSessao = & curl.exe --silent --insecure --noproxy '*' --max-time 10 --output NUL --write-out '%{http_code}' 'https://localhost:8443/aratech/auth/sessao'
if ($LASTEXITCODE -ne 0 -or $semSessao -ne '401') { throw 'Verifique a protecao da sessao.' }
Write-Host 'Frontend, guia, backend e protecao da sessao conferidos em https://localhost:8443.'
& docker @compose ps
if ($LASTEXITCODE -ne 0) { throw 'Falha ao consultar os containers.' }
