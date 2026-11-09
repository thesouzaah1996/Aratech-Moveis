# Operação do Flyway

## Responsabilidades separadas

A mesma imagem contém duas entradas: a aplicação HTTP e a ferramenta `MigracaoApplication`. A aplicação usa a conta `aratech_app_<ambiente>`, com permissões de leitura/escrita dos dados, uso das sequências e **somente leitura** de `flyway_schema_history`. Não recebe senha de migração nem de administração no Docker.

Na inicialização, `FlywayMigrationStrategy` executa **validate**, nunca migrate. O Hibernate usa `ddl-auto: validate`: confere as entidades mapeadas sem criar ou modificar tabelas. Versões pendentes, checksums divergentes ou migrações desconhecidas impedem a inicialização. Essa validação não substitui uma auditoria completa de alterações manuais em tabelas acessadas somente por JDBC.

A tarefa temporária de migração usa `aratech_flyway_<ambiente>`, proprietária dos objetos do schema `public`, sem superusuário, criação de bancos, criação de roles ou associação a outras roles. Ela usa o mesmo JAR e a mesma imagem da aplicação, encerra e é removida. Não é outra instância permanente do backend.

O usuário administrativo original do PostgreSQL continua responsável por preparar contas e propriedade. Ele não é enviado ao backend nem à tarefa Flyway. As credenciais novas são aleatórias, diferentes por ambiente e ficam nos arquivos privados ignorados pelo Git; não são argumentos da linha de comando ou mensagens dos scripts.

## Atualização normal

Selecione `spring.profiles.active` em `src/main/resources/application.yml`. A seleção aceita dev, homolog/hologo e prod. Na pasta `moveis`:

```powershell
.\migrar-banco.ps1 -Comando informar
.\migrar-banco.ps1 -Comando validar
.\migrar-banco.ps1 -Comando migrar
```

`subir-docker-local.ps1` executa a etapa migrar antes de recriar o único backend e frontend. Se a etapa falhar, a atualização da aplicação é interrompida.

Para manutenção explícita de outro banco, `migrar-banco.ps1` também aceita `-Ambiente dev|homolog|prod`, sem mudar o YAML ou o backend que está atendendo. O comportamento padrão continua sendo seguir o YAML.

Antes de migrar, o script faz um `pg_dump` do banco selecionado, restaura integralmente esse backup em um banco temporário, verifica o resultado e remove o banco temporário. A cópia fica em `.rollback-backups/flyway-<data>-<ambiente>-<identificador>/banco.dump`. Isso verifica a restauração daquele backup, mas não substitui retenção externa e o plano de recuperação do ambiente real.

O destino é conferido pelo nome do banco conectado e pelo nome esperado. O schema deve existir e pertencer à conta de migração. A tarefa não cria bancos ou schemas silenciosamente.

## Primeira preparação das contas locais

Com os três PostgreSQL locais já disponíveis e as configurações privadas existentes:

```powershell
.\preparar-permissoes-bancos.ps1
```

Esse comando administrativo também gera e testa backups antes das mudanças. Prepara duas contas por banco, preserva registros e transfere somente os objetos do schema da aplicação; não transfere o banco nem tablespaces. Recusa colisões de roles sem registro de provisionamento. As senhas são preservadas nas reexecuções; nenhum administrador HTTP é criado ou alterado por esse procedimento.

Depois de migrar, `PermissoesMigracaoCallback` concede acesso aos dados e sequências, configura os privilégios padrão para novos objetos e remove escrita/DDL do histórico Flyway da conta da aplicação. A conta da aplicação não pode assumir a conta de migração. A sincronização do catálogo de permissões HTTP permanece no inicializador existente e não concede perfis a pessoas.

## Banco legado sem histórico Flyway

Não há adoção automática por variável de ambiente. Um banco não vazio e desconhecido faz a migração normal falhar.

Primeiro revise o banco em homologação e prepare as contas. Depois, informe explicitamente o nome exato do banco que foi revisado:

```powershell
.\migrar-banco.ps1 -Comando adotar -BancoEsperado NOME_EXATO_DO_BANCO
```

O comando testa o backup, confere destino/proprietário e reconhece as tabelas fundamentais do legado antes de executar um baseline de versão **0**, seguido de V1 a V13. Uma base já gerenciada deve usar migrar ou validar, sem novo baseline. Estruturas desconhecidas são recusadas.

As migrações existentes contêm criações condicionais e alterações anteriormente executadas pelo sistema. A V4 preserva a remoção da tabela obsoleta `acesso_token_senha`. Para evitar uma remoção automática em uma adoção futura, o preflight recusa um legado que ainda contenha essa tabela: sua destinação precisa de uma revisão específica. As migrações já aplicadas não foram reescritas para retirar o comando.

O reconhecimento do legado não prova compatibilidade de qualquer banco antigo. Divergências precisam de análise e correção revisada; não se usa `repair` para esconder erros e não se pulam versões com um baseline arbitrário em 13.

## Regras das migrações

Arquivos em `src/main/resources/db/migration` são versionados e imutáveis após publicados. A próxima alteração será `V14__descricao.sql`, depois V15 e assim por diante. Não renomeie, remova, edite ou reformate V1–V13. O manifesto SHA-256 normalizado em `db/migration-checksums.sha256` protege os arquivos iniciais; o Flyway também confere seus checksums registrados.

Configuração comum à aplicação e à tarefa:

- UTF-8 e validação estrita do nome dos arquivos;
- schema explícito e `create-schemas=false`;
- `baseline-on-migrate=false`, `clean-disabled=true` e `out-of-order=false`;
- validação antes de migrar, sem ignorar versões futuras ou migrações ausentes;
- inicialização SQL paralela do Spring desabilitada.

O comando operacional não oferece clean nem repair. As migrações PostgreSQL usam transação por migração quando as instruções permitem. Uma falha reverte a própria migração, não as versões anteriores já concluídas. Não há promessa de rollback automático de uma versão publicada: planeje a recuperação por backup ou uma nova migração corretiva.

Para novas mudanças com dados reais, prefira expansão compatível, preenchimento e validação dos dados antes de remover estruturas. Remoções, mudanças de tipo e grandes índices exigem avaliação de dados, bloqueios e duração. Os scripts legados em `moveis/db` não são uma segunda fonte automática de atualização e não devem ser executados em paralelo.

| Versões | Estruturas |
| --- | --- |
| V1–V3 | Almoxarifado, funcionários e perfis |
| V4–V5 | Segurança e ponto |
| V6–V9 | Manutenção, portaria e compras |
| V10 | Comercial, PCP, Financeiro, RH e Administração |
| V11–V13 | Assistência e suporte de TI |

## Verificação automatizada

`./verificar-flyway.ps1` gera o JAR, cria um schema exclusivo de teste no PostgreSQL dev, migra com a conta de migração e executa a suíte completa com a conta restrita da aplicação. O schema é removido ao terminar, inclusive após falha; os registros principais permanecem fora dos testes.

Os testes específicos cobrem banco vazio, adoção explícita, preservação de cadastro, imutabilidade, nome inválido, checksum alterado, versão futura, falha transacional e restrições de privilégios. Confirmam CRUD permitido, validação em conta restrita e negação de DDL, escrita no histórico e troca para a role de migração. Também verificam permissões de novas tabelas e sequências.

O workflow `.github/workflows/flyway.yml` prepara um PostgreSQL 16 descartável, cria contas separadas, gera o JAR, migra e valida o banco e roda os testes de backend, exceto `FuncionarioCadastroIT`, que exige o armazenamento MinIO real. Esse teste permanece na suíte completa local e foi executado nela; o job de banco não o substitui por um mock. Em PRs e pushes, o workflow recusa alteração/remoção de migrações versionadas preexistentes. Não usa credenciais de dev/homolog/prod nem publica a aplicação. A execução remota depende do envio do código ao GitHub; criar o workflow local não equivale a executá-lo lá.

Validação desta entrega: **477 testes aprovados**, incluindo `ApiDocumentacaoIT`, permissões, concorrência e integração real de documentos. A preparação das contas também foi testada em banco descartável, com reexecução idempotente, preservação de cadastro e recusa de colisão. Dev, homolog e prod locais permaneceram na versão 13, com as contas e permissões conferidas. Os arquivos V1–V13 foram preservados.

Referências: [inicialização e estratégia do Spring Boot](https://docs.spring.io/spring-boot/how-to/data-initialization.html), [validação do Flyway](https://documentation.red-gate.com/flyway/reference/commands/validate) e [adoção automática e seus cuidados](https://documentation.red-gate.com/flyway/reference/configuration/flyway-namespace/flyway-baseline-on-migrate-setting).
