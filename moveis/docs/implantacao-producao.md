# Implantação em produção

A aplicação foi implementada e validada localmente. Publicação, domínio HTTPS, segredos, atribuição nominal de perfis e cópias de segurança dependem do ambiente real. Não execute o profile dev em produção; não copie banco/dados demonstrativos de desenvolvimento.

## Artefatos

No backend, com Java 21, execute `./verificar-flyway.ps1` para validar a suíte completa em schema exclusivo de teste; o pipeline de banco também usa PostgreSQL descartável. Para gerar o pacote após a validação, use `./mvnw.cmd -DskipTests package`. O jar executável fica em target/moveis-0.0.1-SNAPSHOT.jar. O Dockerfile executa esse jar como usuário sem privilégios. Gere o jar antes de construir a imagem; não rode a suíte de testes no banco operacional de produção.

No frontend: `npm ci` e `npm run build`. O Dockerfile aceita NGINX_CONF=nginx.prod.conf para o backend no serviço backend:8080; a configuração padrão de desenvolvimento continua disponível. Telas não contêm dados demonstrativos ou cadastros guardados apenas no navegador.

## Ambiente

O profile prod exige POSTGRES_DB_PROD, APP_USER_PROD, APP_PASSWORD_PROD, MINIO_URL_PROD, MINIO_ACCESS_KEY_PROD e MINIO_SECRET_KEY_PROD. A conta APP deve ser restrita; POSTGRES_USER_PROD/POSTGRES_PASSWORD_PROD são credenciais administrativas do serviço PostgreSQL e não devem ser enviadas ao backend. A tarefa Flyway recebe sua própria conta somente durante a migração. MINIO_BUCKET_PROD tem padrão aratechmoveis. Configure acesso persistente ao armazenamento de documentos; o contêiner não inclui os arquivos locais de docs, env ou backups.

Para Docker Compose, combine docker-compose-prod.yml e docker-compose-app.yml. Antes de qualquer atualização do banco real, faça e teste uma cópia de segurança de PostgreSQL e dos objetos MinIO; conserve a imagem/jar anterior. O SQL adicional cria tabelas e acrescenta data_vencimento em Compras, sem excluir dados anteriores. Faça a primeira atualização em homologação com uma cópia protegida dos dados necessários.

Exemplo de geração da configuração: `docker compose -f docker-compose-prod.yml -f docker-compose-app.yml config --quiet`. Não envie a saída completa de config com segredos para logs públicos. O comando de publicação é `docker compose -f docker-compose-prod.yml -f docker-compose-app.yml up -d --build`, executado pelo responsável do ambiente após revisar os artefatos e o backup. Esse comando não foi executado automaticamente nesta tarefa.

O backend fica somente na rede Docker; o frontend fica em 127.0.0.1:8088 para receber tráfego de um proxy HTTPS no host. O proxy externo deve preservar Host e definir X-Forwarded-Proto=https, descartando valores enviados pelo cliente. A sessão usa cookie Secure, HttpOnly e SameSite; acessar diretamente por HTTP não substitui a configuração HTTPS. A configuração de proxy encaminha /aratech para o backend; não utiliza a porta de desenvolvimento 8082.

Mantenha uma instância de backend nesta configuração: as sessões atuais são de memória e são encerradas em reinicializações. Redis/sessões compartilhadas e alta disponibilidade não foram implementados. O banco e o armazenamento precisam de volume persistente e política de backup própria.

ARATECH_DOCS_ENABLED=false em produção. A interface e a renovação de CSRF do Swagger continuam disponíveis no ambiente de desenvolvimento, sem formulário próprio de login nem a seção removida anteriormente.

## Primeiro administrador e perfis

O banco pode começar vazio: a tarefa separada do Flyway aplica as migrações em ordem de dependência, sem inserir cadastros demonstrativos. Prepare contas distintas para administração, migração e aplicação; o processo HTTP não deve receber senhas administrativas ou de migração. Ele apenas valida o histórico e as entidades JPA antes de iniciar. Execute a tarefa de migração antes de publicar a nova versão. Para um banco existente sem histórico, siga a [adoção explícita na versão 0](migracoes-flyway.md), primeiro em homologação. A adoção automática fica permanentemente desativada. Não substitua backup e homologação antes de atualizar dados reais.

Se o banco ainda não tem administrador, os provisionadores existentes recebem login/credencial pela entrada padrão privada, nunca por URL ou log. A conta técnica temporária usa aratech.security.admin-temporario=true, exige troca de senha e não substitui uma conta técnica já existente. Configure-a uma única vez, entregue a credencial ao responsável e remova a opção de provisionamento da execução normal. Não use senhas de exemplo em produção.

Depois de entrar e trocar a senha, configure perfis por tarefa no RH e atribua aos funcionários. Para acesso restrito a telas, não deixe também uma permissão ampla do setor em outro perfil. Verifique pelo menos um usuário de consulta, um de operação e um de aprovação antes da abertura para os funcionários. O registro de uma baixa confirma um fato operacional; o sistema não executa pagamentos bancários ou emite nota fiscal.

O login do administrador e dos funcionários exige o domínio interno exato `@aratechmoveis`, por exemplo `admin@aratechmoveis`. Antes de atualizar um ambiente existente, adeque credenciais antigas fora desse padrão por procedimento administrativo controlado; a aplicação não renomeia contas automaticamente. O provisionador técnico exige o login completo pela entrada privada.
