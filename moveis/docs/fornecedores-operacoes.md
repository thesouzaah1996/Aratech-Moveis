# Fornecedores do almoxarifado

Base local: `http://localhost:8082/aratech/almoxarifado/fornecedores`.

| Operacao | Metodo e caminho |
|---|---|
| Cadastrar | POST na base |
| Listar | GET na base, `?pagina=0&tamanho=8&busca=Madeira&ativo=true` |
| Consultar | GET `/{id}` |
| Atualizar | PUT `/{id}` |
| Ativar | PATCH `/{id}/ativar` |
| Desativar | PATCH `/{id}/desativar` |

POST e PUT recebem Body > raw > JSON:

```json
{
  "nome": "Fornecedor Exemplo",
  "email": "contato@example.com",
  "telefone": "4330000000",
  "representante": {
    "nomeRepresentante": "Representante Exemplo",
    "telefoneRepresentante": "43990000000",
    "emailRepresentante": "representante@example.com"
  }
}
```

Todos os seis campos de texto e o representante sao obrigatorios. Nomes de 2 a 150 caracteres, emails validos de ate 150 caracteres, telefones com 10 ou 11 digitos sem mascara. Espacos nas extremidades sao removidos; emails sao normalizados para minusculas. Caracteres de controle sao rejeitados. O frontend remove as mascaras antes do envio.

Nome e email do fornecedor devem ser unicos, inclusive para fornecedores inativos. A atualizacao aceita manter o proprio nome/email. ID e status sao controlados pelo servidor. Representante e um objeto de valor, sem ID ou referencia externa recebida do cliente. Alteracoes usam bloqueio no banco e ativacao/desativacao sao idempotentes.

A pagina da API comeca em 0, com tamanho de 1 a 100 (padrao 20). A tela mostra 8 por pagina e usa o total retornado pela API, sem paginar novamente os itens. Busca por nome ou ID exato e filtro por status sao aplicados antes de contar e paginar. O seletor de fornecedores em produtos percorre todas as paginas ativas.

`db/fornecedores.sql` contem o esquema. `db/fornecedores-fabrica-moveis.sql` cria a tabela se necessario e insere 50 fornecedores FICTICIOS para demonstracao de insumos de uma fabrica de moveis. Os nomes, representantes, emails example.com e telefones nao sao contatos comerciais reais. O script e transacional, pode ser repetido sem duplicar nome/email e preserva registros existentes.

Erros de dados/ID/paginacao retornam 400, registros inexistentes 404 e duplicidades 409. Detalhes internos de erros nao sao enviados na resposta.

O controle de autenticacao e permissoes foi retirado desta etapa.
