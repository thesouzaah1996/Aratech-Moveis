-- Administração inicial de um banco exclusivo da aplicação. Executar com psql.
-- Segredos chegam por variáveis psql via stdin, nunca por argumentos do processo.
\set ON_ERROR_STOP on
BEGIN;
SELECT set_config('aratech.application_role', :'app_user', true);
SELECT set_config('aratech.migration_role', :'migration_user', true);
SELECT set_config('aratech.roles_gerenciadas', :'roles_gerenciadas', true);
DO $$
BEGIN
    IF current_setting('aratech.roles_gerenciadas') <> 'true' AND EXISTS(
        SELECT 1 FROM pg_roles WHERE rolname IN (current_setting('aratech.application_role'),current_setting('aratech.migration_role'))
    ) THEN RAISE EXCEPTION 'Existe role operacional sem registro de provisionamento. Revise sua origem antes de continuar';
    END IF;
END $$;
SELECT format('CREATE ROLE %I LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS NOINHERIT', :'app_user')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname=:'app_user') \gexec
SELECT format('CREATE ROLE %I LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS NOINHERIT', :'migration_user')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname=:'migration_user') \gexec
SELECT format('ALTER ROLE %I LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS NOINHERIT PASSWORD %L', :'app_user', :'app_password') \gexec
SELECT format('ALTER ROLE %I LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS NOINHERIT PASSWORD %L', :'migration_user', :'migration_password') \gexec
DO $$
DECLARE r RECORD; migrador TEXT := current_setting('aratech.migration_role'); aplicacao TEXT := current_setting('aratech.application_role');
BEGIN
    IF migrador=aplicacao OR migrador=current_user OR aplicacao=current_user THEN
        RAISE EXCEPTION 'As contas administrativa, de migracao e de aplicacao devem ser distintas';
    END IF;
    IF EXISTS(SELECT 1 FROM pg_auth_members m JOIN pg_roles p ON p.oid=m.member WHERE p.rolname IN (migrador,aplicacao)) THEN
        RAISE EXCEPTION 'As contas operacionais nao devem possuir associacoes a outras roles';
    END IF;
    EXECUTE format('REVOKE CREATE ON DATABASE %I FROM PUBLIC, %I, %I', current_database(),aplicacao,migrador);
    EXECUTE format('ALTER SCHEMA public OWNER TO %I', migrador);
    REVOKE CREATE ON SCHEMA public FROM PUBLIC;
    -- Somente objetos do schema dedicado da aplicação. Não transfere o banco ou tablespaces.
    FOR r IN SELECT c.relname,c.relkind FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
             WHERE n.nspname='public' AND c.relkind IN ('r','p','v','m') LOOP
        EXECUTE format('ALTER %s public.%I OWNER TO %I',
            CASE r.relkind WHEN 'v' THEN 'VIEW' WHEN 'm' THEN 'MATERIALIZED VIEW' ELSE 'TABLE' END,r.relname,migrador);
    END LOOP;
    FOR r IN SELECT c.relname FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
             WHERE n.nspname='public' AND c.relkind='S' LOOP
        EXECUTE format('ALTER SEQUENCE public.%I OWNER TO %I',r.relname,migrador);
    END LOOP;
    FOR r IN SELECT p.proname,p.prokind,pg_get_function_identity_arguments(p.oid) argumentos FROM pg_proc p
             JOIN pg_namespace n ON n.oid=p.pronamespace WHERE n.nspname='public' AND p.prokind IN ('f','p') LOOP
        EXECUTE format('ALTER %s public.%I(%s) OWNER TO %I',
            CASE r.prokind WHEN 'p' THEN 'PROCEDURE' ELSE 'FUNCTION' END,r.proname,r.argumentos,migrador);
    END LOOP;
END $$;
COMMIT;
