# AratechMoviesFrontend

O frontend agora exige login por sessao do servidor, com permissoes por modulo. RH > Perfis de Acesso permite criar e editar perfis personalizados; RH > Permissoes de Usuario permite criar contas e atribuir perfis. Os modulos integrados usam o backend `moveis`; as demais telas permanecem com suas funcionalidades demonstrativas. O primeiro acesso local esta documentado em `moveis/docs/seguranca-perfis.md`, com credencial inicial no arquivo privado `moveis/.env.bootstrap` (fora desta pasta, ignorado pelo Git/Docker).

Para cadastrar localmente, execute o backend `moveis` no perfil dev (porta 8082), com PostgreSQL e MinIO configurados, e inicie este frontend com `npm start`. O proxy em `proxy.conf.json` encaminha `/aratech` para `http://localhost:8082`, evitando a necessidade de CORS no desenvolvimento. Reinicie `npm start` após alterar o proxy.

No container do frontend, `nginx.conf` encaminha a API para `host.docker.internal:8082`, supondo o backend no host Docker Desktop. Em outro ambiente, ajuste esse destino para o endereço interno do backend. O limite de requisição é 21 MB para comportar os 20 MB de documentos e o envelope multipart.

O cadastro concluído aparece na lista da sessão atual. Consultar funcionários ao recarregar, editar e ativar/desativar ainda depende dos respectivos endpoints do backend; não fazem parte desta integração.


This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 19.2.27.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.

## Categorias do almoxarifado

A tela de categorias consulta a API em `/aratech/almoxarifado/categorias`, com paginacao e busca no servidor, cadastro, edicao e ativacao/desativacao. Usa o mesmo proxy local da integracao de funcionarios. A pagina exibida comeca em 1; o parametro `pagina` da API comeca em 0. O total vem do backend.

A carga inicial das 50 categorias esta em `moveis/db/categorias-fabrica-moveis.sql` (no repositorio, fora desta pasta). O seletor de categorias de produtos carrega todas as paginas ativas.

## Fornecedores do almoxarifado

Cadastro, consulta, edicao e ativacao/desativacao usam `/aratech/almoxarifado/fornecedores`. Busca por nome/ID e filtro de status sao aplicados no backend antes da paginacao. A tela usa paginas de 8 itens e o seletor de produtos percorre todas as paginas ativas. `moveis/db/fornecedores-fabrica-moveis.sql` contem 50 fornecedores ficticios para demonstracao, sem contatos comerciais reais.

## Estoque de produtos

Integrado a `/aratech/almoxarifado/produtos`, com paginacao/busca no servidor e categorias/fornecedores ativos. Quantidade inicial somente no cadastro; a edicao preserva o saldo. Entrada e saida enviam quantidade e UUID para operacoes atomicas com historico, mantendo a chave em repeticoes por falha de rede. Arquivar exige saldo zero e preserva o historico. A carga de 50 produtos ficticios esta em `moveis/db/produtos-fabrica-moveis.sql`.
