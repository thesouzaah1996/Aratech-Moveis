# Embalagem: lotes e esteiras

Embalagem possui dois acessos: **Lotes**, para receber e acompanhar o roteiro, e **Esteiras**, para cadastrar os postos e operar o trabalho. O painel mostra o que cada esteira está fazendo, em qual lote, com qual operador e quais tarefas estão liberadas em seguida. Não há integração com sensores ou acionamento de equipamentos.

## Pesquisa e organização adotada

A organização segue o conceito de posto de trabalho com operações e fila. O ERPNext define capacidade de um posto como o número de trabalhos simultâneos e usa cartões de operação para registrar execução. O Odoo usa centros de trabalho para organizar operações e capacidade. Na aplicação, esses conceitos foram adaptados para **esteiras de embalagem**, sem reproduzir regras específicas desses produtos. [Postos de trabalho — ERPNext](https://docs.frappe.io/erpnext/workstation), [Cartões de operação — ERPNext](https://docs.frappe.io/erpnext/job-card), [Centros de trabalho — Odoo](https://www.odoo.com/documentation/master/applications/inventory_and_mrp/manufacturing/advanced_configuration/using_work_centers.html).

A HOMAG descreve linhas modulares para embalagem de peças de móveis, incluindo formação e fechamento de caixas e módulos adicionais. Por isso, atividades e roteiro são configuráveis: não existe uma divisão física obrigatória das esteiras. [Linhas de embalagem — HOMAG](https://www.homag.com/fileadmin/product/automation/brochures/line-concepts-packaging/line-concepts-packaging-EN.pdf).

Exemplo de roteiro sugerido na interface, editável antes de receber o lote:

1. Conferência e proteção: separar, conferir e proteger as peças.
2. Montagem e fechamento: acondicionar as peças e fechar a embalagem.
3. Identificação e conferência final: conferir os volumes e suas identificações.

Cada etapa pode ser executada em uma esteira diferente. A mesma esteira pode aparecer novamente no roteiro, desde que as operações respeitem a sequência.

## Cadastro de esteiras

Informe código único, nome, atividade, responsável/equipe, capacidade simultânea de 1 a 20 operações e observações. Não são criadas esteiras de exemplo no banco. As atividades sugeridas são apenas opções de preenchimento.

A capacidade não representa unidades por hora, velocidade ou planejamento diário. Uma etapa **em execução ou pausada** ocupa uma posição. Não é possível reduzir capacidade abaixo das posições ocupadas nem inativar uma esteira com etapas pendentes.

## Recebimento de lotes

Em Lotes, escolha uma origem com estado `CORTADO`, ainda sem recebimento válido. Confirme que as peças passaram pelas demais etapas necessárias e estão prontas para embalagem: o término do corte não confirma montagem ou acabamento.

Informe previsão a partir de hoje, prioridade e de 1 a 12 etapas, escolhendo operação, esteira ativa e instruções em cada etapa. Quantidade e ficha técnica são copiadas da origem. O roteiro e a ficha ficam preservados no recebimento; editar uma atividade no cadastro da esteira não reescreve o roteiro existente.

A criação usa UUID para evitar repetição por falha de rede. Uma origem pode ter somente um recebimento não cancelado, incluindo quando já estiver embalada. Antes de iniciar a primeira etapa, o recebimento pode ser cancelado com motivo; nesse caso a origem pode ser recebida novamente com outro roteiro.

## Operação no painel

- **Iniciar:** apenas uma etapa liberada, com as anteriores concluídas, em esteira ativa com posição livre.
- **Pausar:** exige motivo de 5 a 500 caracteres e mantém a posição reservada.
- **Retomar:** retoma a mesma operação e reinicia a contagem de tempo ativo.
- **Concluir etapa:** libera a posição e a próxima etapa. Uma operação pausada deve ser retomada antes da conclusão.
- **Concluir embalagem:** na última etapa, exige total de volumes de 1 a 100000 e conferências de peças, proteção/fechamento e identificação.

O painel consulta o servidor a cada 15 segundos quando não há formulário ou confirmação em andamento. Mostra até 100 esteiras, até 100 tarefas prontas por esteira, operações ocupadas e contagem das etapas futuras. A fila é ordenada por prioridade (urgente, alta, normal), previsão e ID. A equipe escolhe qual tarefa iniciar; não há sequenciamento automático obrigatório.

Estados do lote: `AGUARDANDO → EM_EMBALAGEM ↔ PAUSADO → EMBALADO`. Cancelamento: `AGUARDANDO → CANCELADO`. Estados de etapa: `AGUARDANDO → EM_EXECUCAO ↔ PAUSADA → CONCLUIDA`.

Todas as operações validam a versão atual do lote. Versão antiga, capacidade ocupada ou sequência inválida retornam 409. O bloqueio transacional da esteira serializa disputas por capacidade. O histórico preserva operador, instante, motivo e conferência final. Tempo ativo exclui pausas. Concluir embalagem não gera expedição, baixa de estoque ou nota fiscal.

## Permissões

- `EMBALAGEM_LOTES.CONSULTAR`: consulta lotes, ficha, histórico, origens e opções de esteiras; não opera nem altera o cadastro das esteiras.
- `EMBALAGEM_LOTES.CADASTRAR`: recebe lote e define roteiro.
- `EMBALAGEM_LOTES.ALTERAR`: cancela recebimento ainda não iniciado.
- `EMBALAGEM_ESTEIRAS.CONSULTAR`: consulta painel.
- `EMBALAGEM_ESTEIRAS.CADASTRAR`: cadastra esteira.
- `EMBALAGEM_ESTEIRAS.ALTERAR`: edita esteira, inicia, pausa e retoma etapas.
- `EMBALAGEM_ESTEIRAS.FINALIZAR`: conclui etapas e embalagem.

Permissões amplas do setor continuam funcionando. Não são herdadas automaticamente as permissões do Corte, PCP ou Carregamento. O ícone de Corte passou a ser uma serra vetorial, mantendo cor e tamanho dos ícones existentes.
