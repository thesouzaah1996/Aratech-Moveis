$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
. (Join-Path $PSScriptRoot 'scripts/banco-comum.ps1')
foreach ($ambiente in @('dev','homolog','prod')) {
    $sql = @'
DO $$
DECLARE app TEXT := 'aratech_app___AMBIENTE__'; migrador TEXT := 'aratech_flyway___AMBIENTE__';
BEGIN
    IF EXISTS(SELECT 1 FROM pg_roles WHERE rolname IN (app,migrador) AND (rolsuper OR rolcreatedb OR rolcreaterole OR rolreplication OR rolbypassrls))
       OR (SELECT count(*) FROM pg_roles WHERE rolname IN (app,migrador))<>2 THEN
        RAISE EXCEPTION 'Contas operacionais inexistentes ou administrativas';
    END IF;
    IF pg_has_role(app,migrador,'MEMBER') OR has_schema_privilege(app,'public','CREATE') THEN
        RAISE EXCEPTION 'Aplicacao possui permissao de alterar estrutura';
    END IF;
    IF NOT EXISTS(SELECT 1 FROM pg_namespace n JOIN pg_roles r ON r.oid=n.nspowner WHERE n.nspname='public' AND r.rolname=migrador) THEN
        RAISE EXCEPTION 'Proprietario incorreto do schema';
    END IF;
    IF EXISTS(SELECT 1 FROM unnest(ARRAY['SELECT','INSERT','UPDATE','DELETE']) p WHERE NOT has_table_privilege(app,'public.funcionario',p))
       OR NOT has_table_privilege(app,'public.flyway_schema_history','SELECT')
       OR has_table_privilege(app,'public.flyway_schema_history','INSERT,UPDATE,DELETE,TRUNCATE,REFERENCES,TRIGGER') THEN
        RAISE EXCEPTION 'Permissoes de dados/historico incorretas';
    END IF;
END $$;
SELECT current_database() banco, version versao FROM public.flyway_schema_history WHERE success ORDER BY installed_rank DESC LIMIT 1;
'@
    $script = 'set -eu' + "`n" + 'psql -q -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<''ARATECH_SQL''' + "`n" + $sql.Replace('__AMBIENTE__',$ambiente) + "`nARATECH_SQL`n"
    Executar-ShellBanco ("aratechmoveis-database-$ambiente") $script
    Write-Host "${ambiente}: contas sem privilegios administrativos, aplicacao sem DDL e historico somente leitura confirmados."
}
