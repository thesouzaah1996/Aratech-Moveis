# Uma aplicação Docker e três bancos

O Compose principal é `moveis/docker-compose.yml`, com **um frontend, um backend, três PostgreSQL** (dev, homolog e prod) e MinIO. Todos usam uma única rede bridge chamada **aratechmoveis**, criada automaticamente pelo Compose. As mesmas imagens executam a aplicação em qualquer ambiente.

Na raiz `C:\AratechMoveis`, `compose.yml` inclui essa definição. Para subir os containers já preparados:

```powershell
docker compose up -d
docker compose ps
```

Também funciona na pasta `moveis`, sem `-f`: o Docker usa `docker-compose.yml`. O antigo `docker-compose-local.yml` permanece apenas como arquivo de compatibilidade, incluindo a mesma definição, sem duplicar os serviços.

| Serviço | Nome do container |
| --- | --- |
| Frontend | aratechmoveis-frontend |
| Backend | aratechmoveis-backend |
| PostgreSQL dev | aratechmoveis-database-dev |
| PostgreSQL homolog | aratechmoveis-database-homolog |
| PostgreSQL prod | aratechmoveis-database-prod |
| Documentos MinIO | aratechmoveis-minio |

Os nomes dos volumes PostgreSQL e a pasta de documentos foram mantidos para preservar os dados. Os nomes de serviço `backend`, `minio` e `aramoveis-*` continuam disponíveis no DNS interno da rede.

A consolidação foi validada selecionando dev, hologo/homolog e prod no YAML, sem reconstruir as imagens durante a troca. O ambiente foi deixado em dev.

O endereço é sempre **https://localhost:8443**. O certificado é local e autoassinado. O perfil `prod` neste computador não representa publicação em um servidor externo.

O backend também responde em `http://localhost:8092` para diagnóstico local. Em dev, a interface técnica da API está em `http://localhost:8092/documentacao/index.html`; a disponibilidade do Swagger continua seguindo a configuração do perfil.

## Trocar de ambiente

Edite `src/main/resources/application.yml`, como já era feito no projeto:

```yaml
spring:
  profiles:
    active: dev # dev, homolog (ou hologo), prod
```

Depois, na pasta `moveis`, execute:

```powershell
.\subir-docker-local.ps1
.\verificar-docker-local.ps1
```

O script recria o único backend e o único frontend para aplicar a configuração. Não reconstrói as imagens. O backend carrega os quatro arquivos `application*.yml` por montagem somente de leitura; o Compose não sobrescreve o perfil ativo. O frontend continua encaminhando `/aratech` para o mesmo serviço `backend:8080`, independentemente do perfil.

As conexões usam os serviços Docker do banco selecionado. Cada banco conserva seu volume:

| Ambiente | Serviço PostgreSQL | Porta local | Volume |
| --- | --- | --- | --- |
| dev | aramoveis-dev | 5435 | moveis_dev_data |
| homolog | aramoveis-homolog | 5433 | moveis_homolog_data |
| prod | aramoveis-prod | 5434 | moveis_prod_data |

Os buckets de documentos seguem o perfil: `aratechmoveis` em dev, `aratechmoveis-homolog` em homolog e `aratechmoveis-prod` em prod. A troca reinicia as sessões; faça login com uma conta do banco selecionado.

## Atualizar a aplicação

Após alterar código, gere o pacote e reconstrua as imagens:

```powershell
.\mvnw.cmd -DskipTests package
.\subir-docker-local.ps1 -ReconstruirImagens
```

As credenciais ficam em arquivos privados ignorados pelo Git. Os certificados ficam em `.docker-local/certs`. O Compose usa `POSTGRES_DB_*`, `POSTGRES_USER_*` e `POSTGRES_PASSWORD_*` nos serviços PostgreSQL; o backend recebe apenas `APP_USER_*` e `APP_PASSWORD_*`, além das configurações do MinIO. Na preparação inicial, `.env.docker-local` foi montado a partir de `.env` e da configuração do MinIO existente; a preparação de permissões acrescenta as contas restritas.

O arquivo privado `.env` também contém as variáveis do MinIO necessárias ao comando padrão `docker compose up -d`. Não publique esses arquivos. Para atualizar código e banco, prefira `subir-docker-local.ps1`, que executa a etapa Flyway antes de iniciar a aplicação; o Compose puro inicia os containers, mas não substitui essa etapa de atualização.

Antes de iniciar o backend, o script executa uma tarefa temporária Flyway contra o banco selecionado. A tarefa usa a mesma imagem do backend, encerra e é removida. Permanecem somente um frontend e um backend. O backend apenas valida o histórico e o mapeamento JPA; não recebe as credenciais de migração ou administração.

Os três bancos locais já foram migrados até V13. As contas `aratech_app_<ambiente>` e `aratech_flyway_<ambiente>` são separadas, sem privilégios administrativos. As credenciais privadas ficam em `.docker-local/credenciais-banco.env`. A preparação é feita por `preparar-permissoes-bancos.ps1`, com backup e teste de restauração antes de transferir a propriedade dos objetos do schema. O usuário administrativo do PostgreSQL continua exclusivo da administração, sem ser enviado ao backend.

Para validar sem modificar o banco, use `./migrar-banco.ps1 -Comando validar`; para informar versões, use `-Comando informar`. Uma migração gera e testa um backup antes de executar. A adoção de um banco não vazio sem histórico é uma operação explícita com conferência do destino, conforme [migrações](migracoes-flyway.md). Não há mais opção de adoção automática no script de subida.

Backups anteriores à primeira migração permanecem em `.rollback-backups/docker-local-<data-hora>`. As credenciais iniciais de homolog/prod permanecem em `.docker-local/acessos-iniciais.txt`, com troca obrigatória no primeiro acesso; o administrador preexistente de dev foi preservado.

Os relatórios em `.docker-local/relatorios/dados-<ambiente>.txt` mostram a cobertura de registros por tela. A implantação não inventa dados reais nem copia registros entre bancos. Indicadores e relatórios derivam dos cadastros dos setores; parâmetros têm somente as duas opções implementadas. A fonte de dados operacionais ainda precisa ser definida.
