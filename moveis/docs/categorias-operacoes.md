# Categorias do almoxarifado

Base local: `http://localhost:8082/aratech/almoxarifado/categorias`.

O Flyway prepara a tabela automaticamente na migração V1. Para bancos já utilizados, siga a [adoção de banco existente](migracoes-flyway.md). Os scripts em `moveis/db` são materiais legados e demonstrativos; não são as migrações da aplicação.

| Operacao | Metodo e caminho | Corpo |
|---|---|---|
| Cadastrar | POST na base | `{"nome":"Ferragens"}` |
| Listar | GET na base, `?pagina=0&tamanho=20` | Nenhum |
| Consultar | GET `/{id}` | Nenhum |
| Atualizar | PUT `/{id}` | `{"nome":"Madeiras"}` |
| Ativar | PATCH `/{id}/ativar` | Nenhum |
| Desativar | PATCH `/{id}/desativar` | Nenhum |

No Postman, para POST e PUT escolha Body > raw > JSON. Cadastro retorna 201; as demais operacoes bem-sucedidas retornam 200.

O nome deve ter de 2 a 100 caracteres, nao pode ficar vazio ou conter caracteres de controle. Espacos nas extremidades sao removidos e espacos internos repetidos sao reduzidos a um. Duplicidades sao rejeitadas sem distinguir maiusculas/minusculas, inclusive categorias inativas; reative a existente. Uma chave normalizada com restricao unica no banco protege tambem contra cadastros simultaneos.

IDs devem ser positivos. IDs invalidos retornam 400, registros inexistentes retornam 404 e duplicidades retornam 409. A pagina comeca em zero; tamanho permitido de 1 a 100, com padrao 20.

Atualizar altera apenas o nome, mantendo ID e status. Ativar ou desativar novamente e permitido e nao gera nova gravacao. Status inconsistente no banco retorna 409. Alteracoes bloqueiam o registro durante a transacao. A API retorna DTOs, sem expor a entidade ou a chave normalizada.

O controle de autenticacao e permissoes foi retirado desta etapa.

## Integracao com o frontend e carga inicial

A tela usa paginas de 8 registros: a pagina 1 da tela solicita `pagina=0&tamanho=8`. O total informado pela API define a quantidade de paginas. O frontend nao aplica nova divisao ao resultado recebido.

A busca por nome e feita no servidor via `nome`, antes da paginacao. O parametro opcional `ativo=true` ou `ativo=false` filtra o status antes de contar e paginar. O seletor de categorias de produtos percorre todas as paginas ativas, em lotes de 100.

A antiga exclusao visual foi substituida por ativacao/desativacao. Os registros sao preservados.

O arquivo `db/categorias-fabrica-moveis.sql` cria a tabela quando necessario e cadastra 50 categorias do almoxarifado de uma fabrica de moveis. O script e transacional e pode ser repetido sem duplicar os nomes; ele nao apaga nem reativa categorias existentes.
