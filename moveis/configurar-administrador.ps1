param([Parameter(Mandatory = $true)][ValidateRange(1, [long]::MaxValue)][long]$FuncionarioId)
$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath $PSScriptRoot
try {
    & .\mvnw.cmd -q -DskipTests package
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível preparar a aplicação.' }
} finally { Pop-Location }
$senhaSegura = Read-Host 'Senha inicial (8 a 128 caracteres)' -AsSecureString
$confirmacaoSegura = Read-Host 'Repita a senha' -AsSecureString
$senhaBstr = [IntPtr]::Zero
$confirmacaoBstr = [IntPtr]::Zero
try {
    $senhaBstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($senhaSegura)
    $confirmacaoBstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($confirmacaoSegura)
    $senhaInicial = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($senhaBstr)
    if ($senhaInicial.Length -lt 8 -or $senhaInicial.Length -gt 128 -or $senhaInicial -cne [Runtime.InteropServices.Marshal]::PtrToStringBSTR($confirmacaoBstr)) { throw 'Confira o tamanho e a confirmação da senha.' }
    $inicioBootstrap = New-Object System.Diagnostics.ProcessStartInfo
    $inicioBootstrap.FileName = (Get-Command java -ErrorAction Stop).Source
    $inicioBootstrap.WorkingDirectory = $PSScriptRoot
    $jarBootstrap = Join-Path $PSScriptRoot 'target\moveis-0.0.1-SNAPSHOT.jar'
    $inicioBootstrap.Arguments = '-jar "' + $jarBootstrap + '" --aratech.security.bootstrap=true --spring.main.web-application-type=none'
    $inicioBootstrap.UseShellExecute = $false
    $inicioBootstrap.CreateNoWindow = $true
    $inicioBootstrap.RedirectStandardInput = $true
    $processoBootstrap = [System.Diagnostics.Process]::Start($inicioBootstrap)
    $entradaBootstrap = [System.Text.Encoding]::UTF8.GetBytes(([string]$FuncionarioId) + "`n" + $senhaInicial + "`n")
    $processoBootstrap.StandardInput.BaseStream.Write($entradaBootstrap, 0, $entradaBootstrap.Length)
    $processoBootstrap.StandardInput.BaseStream.Flush()
    [Array]::Clear($entradaBootstrap, 0, $entradaBootstrap.Length)
    $processoBootstrap.StandardInput.Close()
    $processoBootstrap.WaitForExit()
    if ($processoBootstrap.ExitCode -ne 0) { throw 'Não foi possível configurar o administrador. Confira as mensagens acima.' }
} finally {
    if ($senhaBstr -ne [IntPtr]::Zero) { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($senhaBstr) }
    if ($confirmacaoBstr -ne [IntPtr]::Zero) { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($confirmacaoBstr) }
    $senhaInicial = $null
    $senhaSegura.Dispose()
    $confirmacaoSegura.Dispose()
}
