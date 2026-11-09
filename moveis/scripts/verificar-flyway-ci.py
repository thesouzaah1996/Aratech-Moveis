"""Pipeline sem acesso a dev/homolog/prod: usa exclusivamente o serviço aratech_ci."""
import json
import os
from pathlib import Path
import re
import secrets
import subprocess

RAIZ = Path(__file__).resolve().parents[1]
REPO = RAIZ.parent


def executar(argumentos, **opcoes):
    return subprocess.run(argumentos, check=True, **opcoes)


def impedir_edicao_de_versoes():
    evento_path = os.environ.get("GITHUB_EVENT_PATH")
    if not evento_path:
        return
    evento = json.loads(Path(evento_path).read_text())
    anterior = evento.get("pull_request", {}).get("base", {}).get("sha") or evento.get("before", "")
    if not re.fullmatch(r"[a-f0-9]{40}", anterior) or set(anterior) == {"0"}:
        return
    alteracoes = executar(
        ["git", "diff", "--name-status", "--diff-filter=MDR", anterior, "HEAD", "--", "moveis/src/main/resources/db/migration"],
        cwd=REPO, capture_output=True, text=True,
    ).stdout
    for linha in alteracoes.splitlines():
        if any(re.fullmatch(r"V[^/]*\.sql", Path(nome).name) for nome in linha.split("\t")[1:]):
            raise RuntimeError("Migração versionada existente foi alterada/removida: " + linha + ". Crie nova versão.")


def main():
    impedir_edicao_de_versoes()
    container = os.environ.get("CI_POSTGRES_CONTAINER", "")
    if not re.fullmatch(r"[a-f0-9]{12,64}", container):
        raise RuntimeError("Execute somente no serviço PostgreSQL descartável do workflow.")
    app, migrador = "aratech_app_ci", "aratech_flyway_ci"
    senha_app, senha_migrador = secrets.token_hex(32), secrets.token_hex(32)
    banco, admin, senha_admin = "aratech_ci", "ci_bootstrap", "senha_exclusiva_banco_descartavel_ci"
    sql = (
        f"\\set roles_gerenciadas 'false'\n\\set app_user '{app}'\n\\set app_password '{senha_app}'\n"
        f"\\set migration_user '{migrador}'\n\\set migration_password '{senha_migrador}'\n"
        + (RAIZ / "db/preparar-permissoes.sql").read_text(encoding="utf-8")
    )
    executar(["docker", "exec", "-i", container, "psql", "-q", "-v", "ON_ERROR_STOP=1", "-U", admin, "-d", banco], input=sql, text=True)
    ambiente = {
        "DB_HOST_DEV": "localhost", "DB_PORT_DEV": "5432", "POSTGRES_DB_DEV": banco,
        "POSTGRES_USER_DEV": admin, "POSTGRES_PASSWORD_DEV": senha_admin,
        "APP_USER_DEV": app, "APP_PASSWORD_DEV": senha_app,
    }
    (RAIZ / ".env").write_text("\n".join(f"{nome}={valor}" for nome, valor in ambiente.items()) + "\n", encoding="utf-8")
    executar(["bash", "mvnw", "-B", "-DskipTests", "package"], cwd=RAIZ)
    migracao_env = dict(os.environ, **{
        "ARATECH_MIGRACAO_URL": f"jdbc:postgresql://localhost:5432/{banco}",
        "ARATECH_MIGRACAO_USUARIO": migrador, "ARATECH_MIGRACAO_SENHA": senha_migrador,
        "ARATECH_APLICACAO_USUARIO": app, "ARATECH_MIGRACAO_BANCO_ESPERADO": banco,
    })
    java = ["java", "-Dloader.main=com.aratech.moveis.migracao.MigracaoApplication", "-cp", "target/moveis-0.0.1-SNAPSHOT.jar", "org.springframework.boot.loader.launch.PropertiesLauncher"]
    executar(java + ["migrar"], cwd=RAIZ, env=migracao_env)
    executar(java + ["validar"], cwd=RAIZ, env=migracao_env)
    # Este job isola PostgreSQL/Flyway. A integração física de documentos requer MinIO
    # e continua na suíte COMPLETA local (verificar-flyway.ps1), sem enfraquecer esse teste.
    executar(["bash", "mvnw", "-B", "-Dtest=*Test,*Tests,*IT,!FuncionarioCadastroIT", "test"], cwd=RAIZ)


if __name__ == "__main__":
    main()
