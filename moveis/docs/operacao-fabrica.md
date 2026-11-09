# Operação integrada da fábrica

Implementação para uma fábrica pequena, com produção para estoque e sob encomenda. As telas existentes foram ligadas ao backend; registros antes mantidos apenas em arrays do navegador agora são persistidos. Não há cargas demonstrativas novas.

## Fluxo comercial, produção e saída

1. Comercial cadastra um pedido em RASCUNHO, com cliente, produto, quantidade, preço, entrega prevista e vencimento negociado.
2. Confirmar cria, na mesma transação, uma ordem PLANEJADA no PCP e uma conta a receber. O PCP deve definir linha, responsável e estimativa real de horas antes de iniciar. A hora provisória da encomenda não é uma ficha técnica.
3. PCP programa ordens: início/previsão, linha e horas totais. As horas são distribuídas igualmente pelos dias corridos; a programação rejeita carga superior à capacidade diária cadastrada. Esse cálculo simples não presume calendário de turnos, feriados ou OEE.
4. Quando necessário, PCP solicita materiais ao Almoxarifado. A solicitação não baixa saldo. O Almoxarifado confirma a entrega física: a saída usa o estoque existente, preserva autoria/histórico e dispara reposição por mínimo quando aplicável. Recusa não altera saldo. Uma devolução integral só pode ser registrada antes de iniciar a ordem, mediante retorno físico do material.
5. Requisições pendentes devem ser resolvidas antes de iniciar/concluir a produção. Ordens/encomendas com materiais já entregues não podem ser canceladas sem devolução. Cancelamento antes da produção cancela requisições pendentes e a provisão financeira da encomenda; título já baixado impede cancelamento automático.
6. Iniciar muda ordem e encomenda para EM_PRODUCAO. Concluir muda ordem para FINALIZADA e encomenda para PRONTO; o lote lógico é OP-{id}. Não credita insumos nem presume consumo de uma ficha técnica inexistente.
7. Carregamento consulta produção pronta. A saída integral exige caminhão CARREGAR em EM_RECEBIMENTO, após autorização do setor e confirmação de entrada pela Portaria. Registra ordem, chegada e instante. Venda passa para EXPEDIDO; isso não comprova entrega ao cliente. A chegada não é encerrada automaticamente, pois pode carregar várias ordens.

Produção para estoque começa diretamente em Ordens, sem criar conta a receber. Lotes administrativos consultam a mesma produção concluída, evitando cadastros divergentes. Não há módulo de Separação, paletes ou bipagem.

## Compras e Financeiro

- Reposição por estoque mínimo e solicitações de manutenção continuam gerando os pedidos existentes.
- Cotações com até três fornecedores se vinculam a um pedido aberto. Escolher uma proposta preenche fornecedor e preço no mesmo pedido; não aprova a compra nem cria outro pedido.
- Aprovação/recebimento exigem fornecedor, preço, entrega e vencimento negociado. O vencimento é independente da entrega. Receber gera exatamente uma conta a pagar por pedido, na mesma transação. Não lança entrada física automaticamente: o recebimento de materiais continua sob responsabilidade do setor de estoque.
- Valores são calculados no servidor com BigDecimal e gravados com duas casas. Conta integrada não pode ser livremente editada/cancelada por fora da origem.
- Documentos/Notas são uma visão dos mesmos títulos a pagar. Para uma compra já integrada, use Vincular documento na conta existente; isso registra número e emissão sem duplicar a obrigação. Não há emissão de NF ou integração fiscal.
- Baixa integral exige data efetiva não futura, motivo, versão e UUID. É registro gerencial de um pagamento/recebimento ocorrido; não executa pagamento bancário.
- Caixa deriva das baixas e inclui o acumulado até o mês selecionado. Não soma provisões abertas nem representa saldo bancário conciliado.

## RH e Administração

Férias/afastamentos e solicitações são vinculados a funcionários ativos. Períodos pendentes/aprovados não podem se sobrepor. Aprovação e recusa registram decisão, motivo e autor; Em curso/Concluído são derivados das datas aprovadas. Esses registros não calculam direitos legais, folha ou ponto automaticamente.

Solicitações têm análise e resposta final, sem simular emissão de holerites/documentos. O acesso é do RH autorizado; não libera dados de outros empregados a qualquer usuário.

Auditoria dos novos fluxos é transacional. Operações legadas bem-sucedidas também registram metadados HTTP sem corpo sensível; falhas nesse registro adicional são registradas em log e não desfazem uma operação legada já concluída. Histórico de estoque, ponto e portaria permanece nos módulos de origem. Consultas de auditoria não exibem senhas, salários, arquivos pessoais ou descrições médicas.

Relatórios mostram pendências reais, inclusive compras legadas recebidas sem provisão. Os únicos parâmetros oferecidos são janelas de alerta realmente consumidas pelos relatórios: pcp.antecedencia_dias e financeiro.antecedencia_dias, de 0 a 90 dias. O fuso das conexões de banco é America/Sao_Paulo.

## Acesso por tela e ação

Permissões detalhadas seguem SETOR_TELA.CONSULTAR/CADASTRAR/ALTERAR/FINALIZAR. Exemplos: PCP_ORDENS, PCP_REQUISICOES, ALMOXARIFADO_REQUISICOES, RH_PONTO, RH_FUNCIONARIOS, FINANCEIRO_PAGAR, FINANCEIRO_RECEBER, COMPRAS_COTACOES.

As permissões amplas antigas, como RH.CONSULTAR, continuam liberando todas as telas do setor para aquela ação. Para restringir uma pessoa, remova a permissão ampla e atribua as telas específicas. A combinação dos perfis é uma união; um segundo perfil amplo também pode conceder acesso. O perfil não vira administrador por se chamar ADMIN.

A gestão é feita em RH → Perfis de Acesso: crie/edite o perfil, selecione um setor no diálogo de permissões e marque as tarefas. Atribua o perfil ao funcionário em Atribuir Perfis. ACESSOS.GERENCIAR continua separado; gestores não podem conceder acesso superior ao próprio nem administrar credenciais de pessoas mais privilegiadas. Nenhuma permissão nova foi atribuída automaticamente a usuários.

Menus, cards e ações são removidos do DOM quando não autorizados. Rotas de solicitação exigem CADASTRAR. A API repete a verificação em cada requisição; esconder botões não é a proteção de segurança. Seletores de apoio expõem apenas dados mínimos: ponto não precisa liberar cadastro de funcionários; fila não precisa liberar cadastro de mecânicos; produto não precisa liberar contatos completos de fornecedores.

## Contratos e concorrência

Novos endpoints estão em /aratech/comercial, /pcp, /financeiro, /compras/cotacoes, /rh/afastamentos, /rh/solicitacoes, /carregamento/expedicao e /administracao, além das requisições e consultas de apoio. Especificações de campos, formatos e permissões ficam no Swagger.

Cada escrita nova recebe chaveOperacao UUID, gerado uma vez pelo cliente e reutilizado nas tentativas da mesma operação. O servidor armazena assinatura do autor/conteúdo e resultado na mesma transação. Reutilização divergente retorna 409. Atualizações/decisões exigem versao; não aceitam sobreposição silenciosa de alterações. Tabelas têm restrições de integridade e vínculos reais entre setores. Listas paginadas começam em zero, tamanho de 1 a 100; a interface apresenta oito registros por página. As pequenas listas de opções e indicadores são consultas completas.

Erros: 400 validação/formato, 401 sessão, 403 permissão/CSRF, 404 recurso ausente e 409 conflito de estado, duplicidade, saldo, capacidade ou concorrência. As respostas de escrita novas são 200. As respostas e campos de ações legadas mantêm seus contratos, salvo as novas permissões detalhadas e o vencimento negociado em Compras.

O schema operacional está na migração V10 do Flyway, aplicada depois de Compras. Dados históricos não são inventados ou retroativamente convertidos em pagamentos, produção ou provisões. Novas regras de aprovação exigem completar os pedidos antigos pendentes. Veja [migrações e adoção de bancos existentes](migracoes-flyway.md).

## Validação e implantação

Administração → Receber assistência registra a entrada pelo identificador administrativo, código, produto, quantidade, cliente, responsável e defeito. O servidor define o estado inicial; repetir a mesma referência com conteúdo diferente retorna conflito. O acompanhamento e a saída continuam nas telas de Assistência e Carregamento.

Uma instalação vazia cria as tabelas básicas do Almoxarifado na migração V1, antes das tabelas dependentes. A Portaria é criada em V8, antes da expedição em V10. As migrações não inserem produtos, fornecedores ou funcionários demonstrativos.

Testes de integração cobrem confirmação/cancelamento de encomenda, ordem/caminhão, carga de linha, cotações, baixas, documentos sem duplicidade, afastamentos, permissões e retirada/devolução de materiais. Um teste com duas threads confirma que envios simultâneos da mesma chave criam uma única obrigação. Frontend testa visibilidade, revogação, escopo por tela e reenvio sem duplicação. ApiDocumentacaoIT confere a cobertura e as permissões documentadas.

Veja [implantacao-producao.md](implantacao-producao.md) para gerar artefatos e configurar ambiente real. Implementação/validação local não significa que a produção foi publicada ou que seus usuários já receberam os perfis corretos.
