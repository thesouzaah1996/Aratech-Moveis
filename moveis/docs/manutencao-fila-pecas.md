# Solicitacoes de pecas e fila de chamados

Solicitar Peca utiliza o mesmo padrao de Solicitacao de Manutencao: validacao no cliente e servidor, UUID por operacao, prevencao de duplo clique, reenvio seguro apos falha de rede e numero/data/status definidos pelo servidor. A interface e os modais existentes foram preservados.

## Persistencia e compatibilidade

Manutencao e peca sao registros na mesma tabela manutencao_chamado, com a coluna origem (MANUTENCAO ou PECA) e numeracao unica. A fila consulta os mesmos registros; nao ha copia nem sincronizacao entre tabelas. Solicitacoes antigas recebem origem MANUTENCAO na migracao. As consultas /chamados continuam exclusivas de manutencao; /solicitacoes-pecas/{id} aceita apenas pecas. O UUID e unico entre ambas as origens.

O script db/migration/V6__manutencao_chamados.sql é aplicado pelo Flyway uma única vez por banco, com controle de versão. Não apaga registros existentes. As tabelas de mecanicos e especialidades tambem sao persistidas no PostgreSQL.

## API

| Metodo | Caminho a partir de /aratech/manutencao | Uso |
| --- | --- | --- |
| POST | /solicitacoes-pecas | Criar requisicao; resposta 201 com solicitacao |
| GET | /solicitacoes-pecas/{id} | Dados completos da requisicao |
| GET | /fila | Fila unificada: pagina, tamanho, status e busca |
| GET | /fila/{id} | Detalhes atualizados, incluindo peca e mecanico |
| POST | /fila/{id}/mecanico | Atribuir com corpo {"mecanicoId": 7} |
| POST | /fila/{id}/concluir | Concluir atendimento iniciado |
| GET | /mecanicos | Listagem paginada; filtro ativo opcional |
| POST | /mecanicos | Cadastrar nome, especialidades e turno |
| PUT | /mecanicos/{id} | Editar mecanico |
| PATCH | /mecanicos/{id}/ativar | Ativar |
| PATCH | /mecanicos/{id}/desativar | Desativar sem apagar historico |

A fila retorna itens, pagina, tamanho e total; detalhe/acoes retornam item. O objeto item.peca contem todos os dados da requisicao, ou null para manutencao. Mecanicos retornam mecanico ou mecanicos conforme a operacao. Erros seguem status e mensagem, com HTTP 400/404/409/500 conforme os demais modulos.

## Requisicao de peca

Campos: nomePeca (2 a 150), codigo opcional (ate 100), quantidade positiva (ate 3 casas decimais, maximo 999999999999.999), unidade (un, kg, L, m, cx, par), equipamento (2 a 150), finalidade (CORRETIVA, PREVENTIVA, MELHORIA, ESTOQUE), prioridade (BAIXA, MEDIA, ALTA), solicitante (3 a 150), setor (2 a 100), telefone opcional com DDD (10 ou 11 digitos), observacoes (3 a 3000) e chaveOperacao UUID obrigatoria.

Repetir uma chave e os mesmos dados normalizados devolve o mesmo registro. Reutilizar a chave com outra origem ou outros dados retorna conflito. Quantidade 1.5 e 1.500 representam o mesmo valor para esse controle. Solicitar ou concluir uma requisicao nao altera o saldo de estoque.

## Atendimento

Abertura inicia em ABERTA. Atribuir exige mecanico existente e ativo, muda para EM_MANUTENCAO e grava dataInicio. Reatribuir preserva o inicio. Concluir exige atendimento iniciado, muda para CONCLUIDA e grava dataConclusao. Concluido nao pode ser reatribuido. Repetir a mesma atribuicao ou conclusao nao altera os horarios. Os registros e o mecanico sao bloqueados durante as transacoes para evitar conflitos entre operadores.

A fila prioriza ALTA, MEDIA e BAIXA; dentro da mesma prioridade, atende a abertura mais antiga e desempata por ID. Paginacao, status e busca por equipamento, solicitante, setor, nome/codigo da peca ou ID sao executados no servidor. A tela atualiza a fila a cada cinco segundos e preserva os dados com aviso em caso de falha.

## Validacao

FluxoManutencaoIT usa o PostgreSQL de desenvolvimento com transacoes revertidas automaticamente: cadastra as duas origens, verifica numeracao, reenvio, detalhes, atribuicao, conclusao, prioridade, filtros e paginacao. A inicializacao do schema permanece aplicada; os registros de teste nao permanecem no banco.

Na Fila de Chamados, chamados ABERTA exibem o dropdown de mecanicos ativos cadastrados na pagina Mecanicos e um botao de confirmacao na propria linha. A lista e atualizada automaticamente. Acompanhamento nao consulta o backend nem participa deste fluxo nesta etapa.
