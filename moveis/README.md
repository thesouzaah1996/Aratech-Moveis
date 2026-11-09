# Backend

Para executar um frontend e um backend no Docker, escolhendo o ambiente pelo YAML e mantendo os três bancos separados, veja [Docker local](docs/docker-local.md).

O Compose principal está em `docker-compose.yml`; na raiz do repositório, `compose.yml` inclui essa configuração. Use `docker compose up -d` para iniciar os containers preparados. Todos usam a rede `aratechmoveis` e nomes `aratechmoveis-frontend`, `aratechmoveis-backend`, `aratechmoveis-database-<ambiente>` e `aratechmoveis-minio`.

O banco é atualizado por uma tarefa Flyway separada; o backend apenas valida o histórico e o mapeamento JPA ao iniciar, usando uma conta restrita. Para preparar contas, migrar bancos novos ou adotar um banco existente, siga [operação do Flyway](docs/migracoes-flyway.md). Não há carga demonstrativa automática.

O guia para usuários está no menu **Documentação do Sistema** da aplicação. Veja [organização do guia](docs/guia-usuario.md).

Execute o backend no Windows com Java 21 e os bancos no Docker.
Em `src/main/resources/application.yml`, altere `spring.profiles.active`
para selecionar o ambiente:

| Perfil | Banco | Endereço publicado |
| --- | --- | --- |
| `dev` | `aratechmoveis_dev` | `localhost:5435` |
| `homolog` ou `hologo` | `aratechmoveis_homolog` | `localhost:5433` |
| `prod` | `aratechmoveis_prod` | `localhost:5434` |

Na pasta `moveis`, inicie o banco do ambiente escolhido antes do backend:

```powershell
docker compose -f docker-compose-dev.yml up -d --wait
# Para homolog: docker compose -f docker-compose-homolog.yml up -d --wait
# Para prod: docker compose -f docker-compose-prod.yml up -d --wait
.\mvnw.cmd spring-boot:run
```

Trocar o perfil seleciona a conexão; não inicia containers automaticamente.
O arquivo `.env` contém as credenciais e portas de cada ambiente e é carregado
ao executar pela pasta `moveis` ou pela raiz `C:\AratechMoveis`.
A porta 5435 de dev evita conflito com o PostgreSQL instalado no Windows.

Os containers usam a rede `aratechmoveis`. Caso o backend também seja executado
nessa rede Docker, sobrescreva as variáveis `DB_HOST_<AMBIENTE>` com o serviço
`aramoveis-dev`, `aramoveis-homolog` ou `aramoveis-prod` e
`DB_PORT_<AMBIENTE>` com `5432` no ambiente do processo Java.

O cadastro de funcionários está documentado em [docs/cadastro-funcionario.md](docs/cadastro-funcionario.md).
Os documentos são enviados ao MinIO e as chaves dos objetos ficam vinculadas ao funcionário no banco.

O fluxo de portaria e recebimento do almoxarifado esta documentado em [docs/controle-acesso-recebimento.md](docs/controle-acesso-recebimento.md).
