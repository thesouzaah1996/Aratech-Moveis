# Organização do backend

Os módulos seguem a estrutura do Almoxarifado. Cada funcionalidade mantém seus contratos e responsabilidades no próprio setor:

```text
setor/funcionalidade/
  controller/        HTTP, validação de entrada e documentação OpenAPI
  dto/               entradas e respostas da API
  model/             entidades e projeções internas, quando necessárias
  repository/        contratos de persistência ou interfaces Spring Data
    imp/             implementações JDBC e acesso ao armazenamento
  service/           interfaces dos casos de uso
    imp/             implementações ServiceImp e transações
  validation/        validações especializadas, quando necessárias
  exception/         exceções do domínio, quando necessárias
  enums/             tipos enumerados do domínio, quando necessários
  config/            inicialização e configuração específicas
```

Pastas são criadas quando existem classes para elas; não há arquivos vazios para preencher a estrutura.

## Responsabilidades e dependências

Controllers chamam interfaces de serviço. Serviços validam regras, coordenam transações e integrações e dependem dos contratos dos repositories. Repositories executam consultas, atualizações, bloqueios e mapeamento de linhas; não dependem de serviços. As implementações concretas são resolvidas pela injeção de construtor do Spring. Os repositories JPA existentes continuam sendo interfaces Spring Data.

SQL de execução fica exclusivamente em `repository`, inclusive operações de segurança, ponto, compras e relatórios. Serviços recebem dados tipados e não recebem consultas SQL como parâmetros. A estrutura é atualizada pela tarefa Flyway com migrações versionadas em `src/main/resources/db/migration`; o processo HTTP apenas valida o histórico e as entidades JPA. O preflight e o callback de permissões ficam em `config/repository/imp`, com SQL fora dos serviços. `SchemaRepository` mantém somente a sincronização do catálogo de permissões. Veja [migrações](migracoes-flyway.md).

Comercial, PCP e Expedição têm serviços próprios. Comercial utiliza o contrato de Produção para criar/cancelar a ordem da encomenda e o contrato do Financeiro para a provisão. A transação do caso de uso continua abrangendo as operações integradas. Os bloqueios `FOR UPDATE`, locks de identificador, controle de versão, restrições de integridade e idempotência permanecem na persistência; a decisão de negócio permanece nos serviços.

`shared` reúne somente suporte compartilhado: paginação/comandos, execução idempotente, auditoria e seletores mínimos. Não concentra os casos de uso dos setores. A anotação `AuditadoNoServico` identifica controllers cuja escrita já é auditada transacionalmente, evitando repetir a auditoria depois da mudança de pacote.

Segurança segue as mesmas camadas, com pastas adicionais para filtros, provedor de autenticação, autorização e handlers HTTP. Política de senha e normalização de login ficam em `validation`; leitura de credenciais e tentativas de acesso fica em repositories. Montar as permissões da sessão é responsabilidade do serviço, não de um mapeador SQL.

## Contratos e validação

As rotas, métodos HTTP e campos JSON foram preservados. Os DTOs anteriormente aninhados foram distribuídos por módulo; seus identificadores de schema no OpenAPI acompanham os novos pacotes. Consumidores que geram código a partir dos nomes dos schemas devem regenerar seus modelos.

`CamadasTest` verifica ausência de SQL/JDBC fora dos repositories, controllers sem acesso direto à persistência, interfaces de serviço com implementação `ServiceImp` e repositories sem dependência de serviços. As suítes de negócio, concorrência, segurança e `ApiDocumentacaoIT` validam o comportamento integrado.

Para verificar tudo: `./mvnw.cmd '-Dtest=*Test,*Tests,*IT' package`. Depois de mover classes entre pacotes, faça uma compilação sem as classes antigas para não manter beans removidos no classpath.

Fabricação, lotes setoriais e embalagem também seguem a separação entre contrato e implementação: `FabricacaoServiceImp`, `LotesProducaoServiceImp` e `EmbalagemServiceImp`. O repositório setorial fica em `pcp/lotes/repository`; o callback que registra o catálogo antes das migrações fica em `config/repository/imp`. As regras de ficha e imagem reutilizadas ficam em `fabricacao/validation/FabricacaoValidator`.

A matriz atual de testes, os cenários e os comandos de execução estão em [Testes do ERP](testes.md). A validação deve incluir as quatro verificações de arquitetura, os unitários dos serviços e `ApiDocumentacaoIT`, além dos testes de sessão e concorrência.
