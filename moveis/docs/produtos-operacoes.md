# Estoque de produtos

Base local: `http://localhost:8082/aratech/almoxarifado/produtos`.

| Operacao | Requisicao |
|---|---|
| Cadastrar | POST na base |
| Listar e buscar | GET `?pagina=0&tamanho=8&busca=MDF` |
| Consultar | GET `/{id}` |
| Editar dados cadastrais | PUT `/{id}` |
| Arquivar | DELETE `/{id}` |
| Entrada | POST `/{id}/entradas` |
| Saida | POST `/{id}/saidas` |
| Historico | GET `/{id}/movimentacoes?pagina=0&tamanho=20` |

Cadastro (Body > raw > JSON):

```json
{
  "nome": "Parafuso chipboard 4 x 40 mm",
  "sku": "PRD-0001",
  "categoriaID": 1,
  "fornecedorID": 1,
  "quantidade": 100,
  "localArmazenamento": "Rua 1 - Prateleira 01",
  "descricao": "Exemplo de item de almoxarifado",
  "vencimentoProduto": null
}
```

Use IDs reais de categoria e fornecedor ativos. Nome deve ter 2 a 150 caracteres; SKU de 1 a 64 letras/numeros/hifens/sublinhados; local de 2 a 100; descricao opcional de ate 2000. O SKU e normalizado para maiusculas e e unico, incluindo arquivados. IDs, status, data de criacao e referencias de historico nao sao controlados pelo formulario.

A edicao recebe os mesmos campos, EXCETO `quantidade`, que e rejeitada. Tambem nao aceita ID/status/data de criacao no corpo. O SKU deve continuar igual ao cadastrado. O saldo e preservado mesmo se uma movimentacao acontecer enquanto o usuario estiver editando os demais dados.

Entrada/saida:

```json
{
  "quantidade": 5,
  "chaveOperacao": "00000000-0000-0000-0000-000000000001",
  "motivo": "Retirada para producao"
}
```

Gere um UUID diferente para cada nova operacao. Ao repetir a mesma operacao por falha de rede, mantenha a chave e a quantidade. A repeticao nao altera o saldo novamente; reutilizar a chave com outro produto/tipo/quantidade retorna 409. A tela preserva chave e quantidade nas tentativas apos falha de rede.

Quantidades sao inteiras: zero e permitido no cadastro, mas entrada/saida devem ser positivas. Limite de 1 bilhao para o saldo e para cada movimentacao. Numeros fracionados nao sao arredondados/truncados: retornam 400. Entradas e saidas calculam o saldo no servidor sob bloqueio do registro; nunca aceitam um saldo final calculado pelo cliente. Saida maior que o disponivel retorna 409. Saldo e historico sao gravados na mesma transacao.

O cadastro grava um movimento INICIAL. Toda entrada/saida registra quantidade, saldo anterior/posterior, instante e chave. O historico e paginado, inclusive para produtos arquivados. Novas movimentacoes registram o responsavel como nao identificado e exigem motivo de 3 a 500 caracteres. O historico agora pode ser aberto na tela pelo botao de relogio. Registros antigos mantem a indicacao de autoria legada.

Categoria e fornecedor precisam estar ativos para cadastro, edicao e novas entradas. Saidas do estoque existente sao permitidas mesmo se o fornecedor/categoria tiver sido desativado. Uma nova validade deve estar no futuro; entrada de produto vencido e bloqueada. Retirada/baixa de material vencido continua permitida para possibilitar descarte: o sistema de estoque nao autoriza seu uso na producao nem substitui esse controle operacional.

Arquivar exige saldo zero. Nenhum produto ou historico e apagado fisicamente; o SKU permanece reservado. Produtos arquivados nao aparecem na listagem nem aceitam edicao/novas movimentacoes. Repetir o arquivamento retorna sucesso. Nao existe reativacao de produtos nesta etapa.

A tela usa paginas de 8 itens, convertendo pagina 1 para pagina 0 da API. A busca por nome, SKU ou descricao e aplicada no banco antes de contar/paginar. As relacoes sao exibidas com os nomes retornados pela API, mesmo se categoria/fornecedor estiverem inativos. Os seletores percorrem todas as paginas ativas.

`db/produtos.sql` contem as tabelas e restricoes. `db/produtos-fabrica-moveis.sql` insere 50 produtos/saldos ficticios, cada um com seu movimento inicial, vinculados as categorias e fornecedores de demonstracao. Cada unidade representa um item ou embalagem fechada; nao e inventario real e nao modela kg/litros fracionados. O script e transacional/idempotente e nao sobrescreve estoque existente.

IDs invalidos/dados invalidos retornam 400, registros ausentes 404 e conflitos de estoque/SKU/estado 409. O controle de autenticacao e permissoes foi retirado desta etapa; novos movimentos nao identificam um usuario.
