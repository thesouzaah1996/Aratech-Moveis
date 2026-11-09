# Implementação operacional da fábrica

Escopo solicitado: concluir telas sem backend, conectar setores e aplicar permissões na API e na interface. A implantação em produção permanece separada da implementação e da validação local.

## Inventário e entregas

- [x] Permissões por tarefa/tela, mantendo compatibilidade com permissões amplas existentes; menus e ações invisíveis quando não autorizados.
- [x] PCP: linhas, ordens, programação e indicadores calculados; lote de produção gerado na conclusão; requisições de materiais com retirada/devolução transacional.
- [x] Comercial: pedidos de clientes, confirmação gerando produção e conta a receber, sem emissão fiscal.
- [x] Carregamento: expedição de produção pronta vinculada a chegada autorizada pela portaria.
- [x] Financeiro: pagar, receber, documentos e caixa com registros persistentes e baixas auditadas.
- [x] Compras: cotações vinculadas a pedidos e fornecedor vencedor; recebimento gera conta a pagar uma única vez com valor e vencimento negociado obrigatórios.
- [x] RH: férias/afastamentos vinculados a funcionários e solicitações com decisões registradas.
- [x] Administração: auditoria, relatórios, parâmetros com efeito real e lotes derivados da produção.
- [x] Swagger/API, testes de negócio/segurança e validação do frontend.

## Validação local

Relatórios do backend: 442 testes, sem falhas ou erros, incluindo concorrência, segurança e ApiDocumentacaoIT. A execução completa foi complementada por repetições das classes alteradas. Frontend: 263 testes aprovados e build concluído; permanecem os avisos de orçamento do bundle e seletores do Bootstrap. Instalação em PostgreSQL vazio validada com 11 testes operacionais/documentação; banco descartável removido ao final. Configuração Docker Compose validada sintaticamente, sem publicar serviços em produção.

## Decisões de operação

Telas separadas por tarefa, tabelas paginadas e formulários em diálogo. Nenhum dado demonstrativo novo. Identificadores e estados são definidos no servidor; escritas têm transação, versão e idempotência. Não há pagamento bancário automático, emissão de NF, cálculo de folha ou concessão automática de privilégios. Uma confirmação operacional registra o fato informado pelo usuário autorizado.

As permissões amplas já atribuídas continuam válidas. Para restringir uma pessoa a telas específicas, use permissões detalhadas e remova a permissão ampla correspondente. Nenhuma pessoa recebe novas permissões sem atribuição por gestor autorizado.
