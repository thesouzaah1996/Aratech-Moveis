# Solicitar manutencao

A tela Manutencao > Solicitar Manutencao registra equipamento, tipo, prioridade, solicitante, setor, telefone opcional e descricao no PostgreSQL. O layout original e o modal de resultado foram mantidos. O numero, o status inicial ABERTA e a data de abertura sao definidos pelo servidor.

## Estrutura

O pacote manutencao/chamados segue controller, dto, enums, model, repository, service/imp, validation e exception. A inicializacao aplica db/manutencao-chamados.sql de forma aditiva, sem apagar tabelas ou registros existentes.

## API

| Metodo | Caminho | Uso |
| --- | --- | --- |
| POST | /aratech/manutencao/chamados | Criar solicitacao; resposta 201 com chamado |
| GET | /aratech/manutencao/chamados | Consulta paginada; pagina, tamanho e status opcional |
| GET | /aratech/manutencao/chamados/{id} | Consultar o chamado pelo numero |

Listagem retorna status, chamados, pagina, tamanho e total. Pagina comeca em 0; tamanho de 1 a 100, padrao 8. Ordenacao por abertura e ID decrescentes. Status aceitos no filtro: ABERTA, EM_MANUTENCAO e CONCLUIDA. A fila da equipe de manutencao utiliza o endpoint /aratech/manutencao/fila, com paginacao e filtros no servidor. Acompanhamento permanece sem integracao, reservado para uma etapa futura.

Exemplo de POST:

```json
{
  "equipamento": "Prensa Hidraulica 01",
  "tipo": "CORRETIVA",
  "prioridade": "ALTA",
  "solicitante": "Ana Silva",
  "setor": "Producao",
  "telefone": "43999991234",
  "descricao": "Motor parado durante a operacao.",
  "chaveOperacao": "11111111-2222-4333-8444-555555555555"
}
```

Equipamento: 2 a 150 caracteres; solicitante: 3 a 150; setor: 2 a 100; descricao: 3 a 3000, com quebras de linha permitidas. Tipo: CORRETIVA ou PREVENTIVA. Prioridade: BAIXA, MEDIA ou ALTA. Telefone opcional: DDD e 10 ou 11 digitos; pontuacao usual e normalizada. Chave UUID obrigatoria, repetida somente para tentar novamente a mesma solicitacao.

Reenviar a chave com os mesmos dados normalizados devolve o mesmo numero sem criar outro chamado, sem reabrir nem alterar a data. Outra solicitacao recebe outro UUID. Dados invalidos retornam 400, chamado ausente 404, conflito de chave 409; mensagens seguem o campo mensagem. O formulario bloqueia envios simultaneos e preserva a chave e os dados apos erro de rede.

A fila unificada em /aratech/manutencao/fila recebe manutencao e solicitacoes de pecas. O cadastro de mecanicos, a atribuicao e a conclusao estao integrados. Consulte manutencao-fila-pecas.md para os contratos.
