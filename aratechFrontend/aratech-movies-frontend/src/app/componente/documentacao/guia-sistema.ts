export interface GuiaSetor {
  id: string;
  nome: string;
  icone: string;
  resumo: string;
  passos: string[];
  cuidados: string[];
  exemplo: string;
  telas: string;
}

// Conteúdo baseado nos serviços de cada setor. Revise ao mudar os fluxos.
export const GUIA_SETORES: GuiaSetor[] = [
  {
    id:'embalagem',nome:'Embalagem',icone:'bi-box-seam',
    resumo:'Distribui o trabalho por esteira e acompanha o lote até a conferência dos volumes.',
    telas:'Embalagem → Ordens do PCP, Lotes e Esteiras',
    passos:['Em Ordens do PCP, abra a OS liberada para Embalagem. Confira lote LP, produto, variante, quantidade e as peças e instruções destinadas ao setor.','Inicie a ordem ao começar o trabalho e conclua depois de executar todas as suas linhas. Essa ordem tem acompanhamento próprio no PCP.','No fluxo de Lotes e Esteiras, cadastre as esteiras e receba uma origem de corte anterior com peças prontas. Defina a sequência de etapas e opere cada uma no painel de esteiras.','Na última etapa do roteiro das esteiras, informe os volumes e confirme peças, proteção e identificação.'],
    cuidados:['Ordens do PCP e Lotes/Esteiras são fluxos distintos. A OS nova não é recebida automaticamente como lote de esteira.','No roteiro de esteiras, a próxima etapa depende da anterior e operações pausadas continuam ocupando capacidade.','O painel de esteiras atualiza a cada 15 segundos fora de formulários e confirmações. Concluir embalagem não registra expedição nem baixa de estoque.'],
    exemplo:'Enquanto a Esteira 1 protege as peças do lote EB-20, a Esteira 2 pode fechar as caixas do EB-19. O EB-20 só segue para fechamento quando sua proteção terminar.'
  },
  {
    id: 'corte', nome: 'Corte', icone: 'serra',
    resumo: 'Recebe os lotes planejados pelo PCP e acompanha o corte das peças de chapa.',
    telas: 'Corte → Lotes',
    passos: [
      'Abra a ordem liberada pelo PCP. Confira o identificador OS, o lote LP de origem, produto, variante, revisão e quantidade planejada.',
      'Confira as peças, os totais, as dimensões em milímetros, material, espessura, acabamento, bordas e instruções de corte.',
      'Use Iniciar ordem ao começar e Concluir ordem ao terminar o trabalho destinado ao Corte. A Furação e os demais setores acompanham suas próprias ordens.',
      'Em Lotes de corte anteriores, o fluxo de corte mantém sua própria ficha, fotos, exportação de peças e impressão.'
    ],
    cuidados: [
      'Ordens novas aparecem somente quando o PCP seleciona e libera Corte. A OS traz lote LP, produto, variante, revisão, quantidade e instruções exclusivas. Os lotes anteriores continuam no histórico.',
      'A ordem é uma lista de peças e medidas. A área exibida nos lotes anteriores soma as peças e não calcula aproveitamento nem quantidade de chapas.',
      'Concluir corte não baixa estoque nem conclui outras etapas de fabricação.'
    ],
    exemplo: 'Se a ficha tem duas laterais e o lote contém dez guarda-roupas, a lista indica vinte laterais para cortar.'
  },
  {
    id: 'furacao', nome: 'Furação', icone: 'furadeira',
    resumo: 'Executa as operações de furação atribuídas pela ficha técnica e liberadas pelo PCP.',
    telas: 'Início → Furação',
    passos: [
      'Abra a OS e confira o lote LP, produto, variante, quantidade planejada e revisão.',
      'Consulte as peças e as instruções de cada operação de furação. Os totais já consideram a quantidade do lote.',
      'Inicie a ordem ao começar e conclua depois de executar suas operações. O PCP acompanha esse status separadamente do Corte e da Montagem.'
    ],
    cuidados: [
      'A ordem só aparece depois que o PCP libera Furação. Trabalho de outros setores não aparece nesta fila.',
      'A mesma peça pode passar por Corte e Furação com instruções diferentes, sem duplicar seu consumo de material.',
      'As ordens mantêm a revisão congelada do lote; uma alteração posterior no cadastro não muda suas instruções.'
    ],
    exemplo: 'Um lote de 150 móveis com duas laterais por unidade gera uma operação de furação sobre 300 laterais, se o roteiro atribuir essa etapa à Furação.'
  },
  {
    id: 'montagem', nome: 'Montagem', icone: 'bi-tools',
    resumo: 'Acompanha o trabalho de montagem atribuído no roteiro da variante.',
    telas: 'Início → Montagem',
    passos: [
      'Abra a ordem liberada pelo PCP e confira o produto, a variante e o lote de origem.',
      'Consulte apenas as peças e operações atribuídas à Montagem, com as quantidades e instruções da revisão do lote.',
      'Inicie o trabalho e conclua a ordem quando todas as suas operações terminarem.'
    ],
    cuidados: [
      'A liberação é escolhida pelo PCP. Uma ordem de Corte concluída não cria automaticamente a ordem de Montagem.',
      'Concluir Montagem não conclui ordens de outros setores nem registra a saída dos materiais.',
      'O lote principal só é concluído quando não há setor necessário pendente e todas as ordens estão concluídas.'
    ],
    exemplo: 'A Montagem pode estar Em andamento enquanto o PCP ainda mantém a Embalagem pendente de liberação no mesmo lote.'
  },
  {
    id: 'variantes', nome: 'Variantes e roteiros', icone: 'bi-palette',
    resumo: 'Define a ficha específica do modelo e da cor, com o responsável pelo consumo e as operações de cada peça.',
    telas: 'Administração → Guarda-roupas → Variantes e roteiros por setor',
    passos: [
      'Use Nova variante, selecione o modelo e prepare uma cópia da ficha. Informe o código da variante, nome/cor e situação ativa.',
      'Confira o consumo por móvel, material, acabamento e medidas de cada componente. Os campos de medidas aparecem conforme as operações e setores escolhidos.',
      'Escolha um setor responsável pelo consumo. Para separação no Almoxarifado, selecione o produto de estoque e confira a conversão de unidade.',
      'Adicione as operações necessárias, com setor e instruções exclusivas para cada etapa. Salve a variante e sua revisão.'
    ],
    cuidados: [
      'O destino é definido pelo roteiro, não pelo nome da peça. Uma lateral pode ter Corte, Furação e Montagem sem consumir material três vezes.',
      'Itens sem setor ou com dados técnicos insuficientes bloqueiam a liberação e aparecem como pendências na prévia do lote.',
      'Antes de emitir qualquer ordem, o PCP pode atualizar a prévia com a variante corrigida. Depois da primeira liberação, todos os setores usam a revisão congelada.',
      'Se a ficha usa KG e o estoque controla gramas, confira a conversão explícita. O sistema não arredonda quantidades para permitir consumo.'
    ],
    exemplo: 'Na variante branca, atribua a lateral ao Corte e suas operações ao Corte e à Furação. Atribua as dobradiças ao Almoxarifado e vincule o produto correto do estoque.'
  },
  {
    id:'guarda-roupas',nome:'Guarda-roupas',icone:'bi-door-closed',
    resumo:'Organiza a ficha técnica de cada modelo e todos os itens necessários para fabricá-lo.',
    telas:'Administração → Guarda-roupas; PCP → Lotes',
    passos:['Cadastre código, nome e medidas externas do modelo. Adicione painéis, ferragens, acessórios, embalagens ou itens personalizados.','Informe as quantidades por móvel e as medidas finais das peças em mm. Inclua veio, fita de borda, furações e atributos adicionais.','Salve a ficha e adicione até quatro fotos para a capa e para cada componente. Abra Variantes e roteiros por setor para definir a ficha da cor e suas operações.','No PCP, planeje o lote pela variante correspondente ao produto que será fabricado.'],
    cuidados:['As sugestões de componentes não substituem o projeto: dimensões e furações dependem das ferragens e materiais escolhidos.','As fotos devem ser JPEG ou PNG de até 5 MB. Salve novos componentes antes de enviar suas imagens.','O lote multissetorial preserva os dados da variante. Os lotes anteriores de corte também preservam suas referências de fotos. Alterar o catálogo não atualiza ordens emitidas.'],
    exemplo:'Um modelo pode conter laterais de MDF, fundo de menor espessura, portas, gavetas, corrediças e cabideiro, cada qual com suas medidas, quantidade, observações e fotos.'
  },
  {
    id: 'pcp', nome: 'Planejamento e produção', icone: 'bi-calendar3',
    resumo: 'O PCP organiza o que será produzido, quando e em qual linha de trabalho.',
    telas: 'PCP → Lotes, Ordens, Programação, Capacidade, Indicadores e Requisições',
    passos: [
      'Cadastre as linhas de produção e suas horas disponíveis por dia. Planeje a ordem com período, responsável, linha e horas necessárias.',
      'Em Lotes, escolha a variante do modelo, quantidade, previsão e opcionalmente a OP/pedido de origem. Confira a prévia por setor e libere somente os checkboxes desejados. Os demais setores continuam pendentes e podem ser liberados depois. Antes da primeira liberação, é possível corrigir a variante e atualizar a prévia. Depois, a revisão fica congelada.',
      'Cada setor recebe sua OS vinculada ao lote LP, com suas próprias linhas. Em Separação de lotes, o Almoxarifado confirma o consumo antes de concluir; liberar não baixa saldo. As requisições anteriores continuam no fluxo existente.',
      'Acompanhe o status de cada OS e os setores pendentes no lote. Só conclua a OP de origem depois de resolver as requisições e concluir todos os lotes vinculados; a OP finalizada fica disponível para expedição.'
    ],
    cuidados: [
      'A programação divide as horas igualmente pelos dias corridos e impede ultrapassar a capacidade diária da linha. Confira feriados e turnos no planejamento da equipe.',
      'Solicitar material ainda não retira saldo. O estoque muda quando o Almoxarifado confirma a entrega.',
      'A devolução integral de materiais das requisições é permitida antes do início da produção, depois do retorno físico.',
      'Repetir a liberação do mesmo lote e setor mantém a mesma OS. Um lote com destinos necessários ainda pendentes não aparece como concluído.',
      'A prévia usa o consumo por unidade da ficha da variante. Corrija as pendências técnicas antes de liberar; a lista de corte não estima chapas.'
    ],
    exemplo: '150 móveis com 2 laterais e 8 dobradiças geram 300 laterais para Corte e 1.200 dobradiças para Almoxarifado. Operações em Corte e Furação não duplicam consumo. O lote não conclui com setores pendentes.'
  },
  {
    id: 'almoxarifado', nome: 'Almoxarifado', icone: 'bi-boxes',
    resumo: 'Cuida dos cadastros de materiais, fornecedores, saldos e entregas para a produção.',
    telas: 'Almoxarifado → Categorias, Fornecedores, Estoque de Produtos, Recebimento, Requisições, Separação de lotes e Pedidos',
    passos: [
      'Organize as categorias e fornecedores e cadastre os produtos com SKU, saldo, local de armazenamento e demais dados necessários.',
      'Registre as entradas e saídas de materiais conforme a movimentação física e consulte o histórico.',
      'Em Separação de lotes, abra a OS do PCP e confira lote, produto, variante, quantidade e materiais. Inicie a ordem; em cada linha, use Conferir e consumir, informe o motivo e confirme a quantidade e sua conversão para o estoque.',
      'Conclua a separação depois de confirmar todos os consumos. A ordem mostra somente os insumos atribuídos ao Almoxarifado, sem instruções de Corte ou Furação.',
      'Nas requisições do PCP, confira a quantidade e confirme a entrega quando o material for entregue. Se recusar, informe a resposta; o saldo permanece igual.',
      'Configure o estoque mínimo quando precisar de acompanhamento de reposição. Consulte em Pedidos as compras geradas para o setor.'
    ],
    cuidados: [
      'Liberar ou iniciar a separação não baixa saldo. O consumo é confirmado por material, exige saldo suficiente e não é repetido ao reenviar a mesma confirmação.',
      'Autorizar um caminhão ou finalizar seu recebimento não lança automaticamente os produtos no estoque.',
      'A reposição por mínimo pode gerar um pedido de compra, que ainda precisa seguir as etapas de Compras.'
    ],
    exemplo: 'Para 150 móveis com oito dobradiças por unidade, a separação apresenta 1.200 dobradiças. O saldo só diminui ao confirmar esse consumo; essa ferragem não aparece na ordem do Corte.'
  },
  {
    id: 'portaria', nome: 'Portaria', icone: 'bi-truck',
    resumo: 'Registra a chegada dos veículos e controla a entrada com autorização do setor responsável.',
    telas: 'Portaria → Controle de Acesso',
    passos: [
      'Registre nota fiscal, empresa, motorista, placa, carga, operação e setor responsável conforme os campos da tela.',
      'A chegada fica aguardando. O setor de destino confere e autoriza o atendimento.',
      'Depois da autorização, confirme a entrada na Portaria. O atendimento passa para a etapa de recebimento.',
      'Finalize depois da entrada e do atendimento. Consulte os registros anteriores no histórico.'
    ],
    cuidados: [
      'A entrada depende da autorização do setor. A autorização sozinha ainda não registra a entrada do veículo.',
      'Uma placa não pode ter duas chegadas em andamento. A mesma nota não pode ser repetida para a mesma empresa.',
      'Os dados da chegada podem ser corrigidos enquanto ela aguarda autorização. Registros finalizados não são reabertos.'
    ],
    exemplo: 'Um caminhão de MDF chega para o Almoxarifado: a Portaria registra, o Almoxarifado autoriza e só então a Portaria confirma a entrada.'
  },
  {
    id: 'carregamento', nome: 'Carregamento e expedição', icone: 'bi-box-arrow-right',
    resumo: 'Acompanha cargas e descargas e registra a saída de ordens prontas e de lotes de assistência.',
    telas: 'Carregamento → Cargas e Descargas, Expedição e Assistência',
    passos: [
      'Na fila de cargas e descargas, confira os veículos destinados ao setor e autorize os que podem entrar.',
      'Aguarde a confirmação da entrada pela Portaria antes de registrar o atendimento correspondente.',
      'Na expedição, selecione uma ordem finalizada e um caminhão da operação Carregar com entrada já confirmada. Registre a saída integral da ordem.',
      'Para assistência, registre o carregamento somente depois que o lote estiver finalizado. Encerre o atendimento do veículo quando todas as operações terminarem.'
    ],
    cuidados: [
      'A expedição registra a saída integral da ordem; confira a quantidade antes de confirmar.',
      'Expedir uma ordem não encerra automaticamente a chegada do caminhão, pois ele pode carregar outras ordens.',
      'O registro de saída não comprova a entrega ao cliente.'
    ],
    exemplo: 'Um caminhão pode levar duas ordens prontas. Registre cada saída e finalize a chegada depois de concluir o atendimento do veículo.'
  },
  {
    id: 'compras', nome: 'Compras', icone: 'bi-cart3',
    resumo: 'Reúne necessidades de compra, compara propostas e registra decisões e recebimentos.',
    telas: 'Compras → Pedidos e Cotações',
    passos: [
      'Consulte pedidos de reposição, solicitações de peças e pedidos manuais.',
      'Em Cotações, vincule até três propostas a um pedido aberto e escolha a proposta desejada.',
      'Complete fornecedor, preço, entrega e vencimento negociado antes da aprovação e do recebimento.',
      'Registre a decisão e, quando a compra for recebida, confirme o recebimento. O sistema cria a conta a pagar vinculada à compra.'
    ],
    cuidados: [
      'Escolher uma cotação preenche fornecedor e preço no pedido; a aprovação é uma etapa separada.',
      'Receber a compra cria a obrigação financeira, mas a entrada física do material deve ser registrada pelo setor de estoque.',
      'Se o pedido já gerou uma conta a pagar, use essa conta. Evite cadastrar uma segunda cobrança manual para a mesma compra.'
    ],
    exemplo: 'Uma compra recebida de R$ 500 aparece no Financeiro como conta a pagar. O responsável ainda precisa registrar o pagamento quando ele realmente acontecer.'
  },
  {
    id: 'financeiro', nome: 'Financeiro', icone: 'bi-cash-coin',
    resumo: 'Acompanha valores a pagar e a receber e registra pagamentos e recebimentos realizados.',
    telas: 'Financeiro → Contas a Pagar, Contas a Receber, Baixa de Notas e Fluxo de Caixa',
    passos: [
      'Confira os títulos criados pelas compras e encomendas. Cadastre manualmente apenas obrigações que ainda não estejam registradas.',
      'Confira pessoa ou empresa, valor e vencimento. Para uma compra integrada, vincule o número do documento à conta existente.',
      'Depois que o dinheiro for efetivamente pago ou recebido, registre a baixa integral com data real e motivo.',
      'Consulte o fluxo de caixa pelo mês desejado. Ele considera as baixas e o acumulado dos períodos anteriores.'
    ],
    cuidados: [
      'Registrar pagamento ou recebimento confirma um fato ocorrido; o sistema não movimenta a conta bancária.',
      'A baixa não aceita data futura. Contas vinculadas a outros setores têm regras de alteração e cancelamento pela origem.',
      'Baixa de Notas usa as mesmas contas a pagar. O registro do documento não emite nota fiscal. O caixa é gerencial e depende das baixas registradas.'
    ],
    exemplo: 'Uma conta vence na sexta, mas foi paga na quinta. Registre a quinta como data do pagamento; a sexta continua sendo o vencimento combinado.'
  },
  {
    id: 'manutencao', nome: 'Manutenção', icone: 'bi-tools',
    resumo: 'Organiza solicitações de manutenção, pedidos de peças e o atendimento pelos mecânicos.',
    telas: 'Manutenção → Solicitar Manutenção, Solicitar Peça, Fila de Chamados, Mecânicos, Estoque e Pedidos',
    passos: [
      'Abra a solicitação descrevendo equipamento, problema ou peça, prioridade e informações de contato solicitadas.',
      'Acompanhe o número gerado. A fila reúne chamados e solicitações de peças, priorizando os mais urgentes e depois os mais antigos.',
      'O responsável atribui um mecânico ativo. Isso inicia o atendimento; a conclusão deve ser registrada depois do trabalho.',
      'Controle as peças no estoque e consulte os pedidos de compra do setor quando houver necessidade de reposição.'
    ],
    cuidados: [
      'Abrir ou concluir uma solicitação de peça não movimenta o saldo do estoque por si só.',
      'Um chamado concluído não pode receber nova atribuição de mecânico.',
      'Escolha a prioridade de acordo com o impacto real e descreva o equipamento para ajudar no atendimento.'
    ],
    exemplo: 'Uma máquina parou: descreva o sintoma e o equipamento na solicitação. A atribuição do mecânico registra o início; concluir confirma que o atendimento terminou.'
  },
  {
    id: 'assistencia', nome: 'Assistência técnica', icone: 'bi-wrench-adjustable',
    resumo: 'Acompanha lotes recebidos para reparo e mantém o controle das peças de assistência.',
    telas: 'Administração → Receber Assistência; Assistência → Lotes e Estoque de Peças; Carregamento → Assistência',
    passos: [
      'Registre o recebimento do lote com referência de origem, código, produto, quantidade e demais informações da tela.',
      'Em Lotes, inicie o trabalho quando o reparo começar. O lote passa de Não iniciado para Em andamento.',
      'Finalize quando o trabalho terminar. O lote finalizado fica disponível para registrar o carregamento.',
      'Controle entradas e saídas das peças na área de estoque de assistência.'
    ],
    cuidados: [
      'O fluxo segue a ordem: receber, iniciar, finalizar e carregar. Cada ação depende da etapa anterior.',
      'A referência de origem identifica o recebimento e evita duplicar o mesmo lote.',
      'Finalizar o lote e registrar seu carregamento são ações distintas.'
    ],
    exemplo: 'Um lote de cadeiras recebido para reparo permanece Não iniciado até o começo do serviço. Depois de finalizado, o Carregamento registra a saída.'
  },
  {
    id: 'rh', nome: 'Pessoas e RH', icone: 'bi-people',
    resumo: 'Reúne funcionários, documentos, ponto, afastamentos e solicitações ao RH.',
    telas: 'Recursos Humanos → Funcionários, Ponto, Férias, Solicitações e Perfis de Acesso; Meu Perfil e Meu Ponto',
    passos: [
      'Mantenha o cadastro do funcionário e seus documentos atualizados nos campos disponíveis.',
      'Em Marcação de ponto, registre sua entrada e saída pelo botão da tela. O sistema usa o horário do servidor e a identidade da sua sessão.',
      'Consulte Meu Ponto pelo mês. Para corrigir horários ou escala de outra pessoa, o responsável precisa do perfil # e da permissão de alteração de ponto; a correção exige horários em sequência e motivo.',
      'Registre férias ou afastamentos e acompanhe a decisão. Períodos pendentes ou aprovados da mesma pessoa não podem se sobrepor.',
      'Nas solicitações ao RH, registre a demanda e acompanhe a análise e a resposta final. Em Meu Perfil, consulte seus próprios dados.'
    ],
    cuidados: [
      'Aprovar férias ou afastamentos registra o período. O sistema não calcula automaticamente direitos, folha ou alterações no ponto.',
      'O status de um período aprovado acompanha suas datas, passando para Em curso e Concluído.',
      'Dados de outros funcionários dependem de permissão. Para corrigir o ponto, use horários crescentes no mesmo dia, sem espaços entre marcações preenchidas.'
    ],
    exemplo: 'Ao corrigir uma saída esquecida, informe o horário correto e explique o motivo. O histórico permite consultar o que mudou e quem realizou a alteração.'
  },
  {
    id: 'administracao', nome: 'Administração e acessos', icone: 'bi-shield-check',
    resumo: 'Ajuda a acompanhar pendências, consultar registros de ações e definir quem pode fazer cada tarefa.',
    telas: 'Administração → Guarda-roupas, Relatórios, Auditoria e Parâmetros; Recursos Humanos → Perfis de Acesso',
    passos: [
      'Consulte os relatórios para identificar pendências reais dos setores e a auditoria para acompanhar as ações registradas.',
      'Ajuste nos parâmetros a antecedência dos alertas de produção e vencimentos, entre 0 e 90 dias.',
      'Em Perfis de Acesso, organize permissões por tarefa: consultar, cadastrar, alterar e finalizar. Atribua os perfis às pessoas responsáveis.',
      'Cadastre os modelos e componentes em Guarda-roupas e as variantes com seus roteiros por setor. Lotes, prévia, liberações e produção concluída ficam no PCP.'
    ],
    cuidados: [
      'Uma pessoa com mais de um perfil reúne as permissões de todos eles. Um perfil com acesso amplo ao setor também pode liberar suas telas.',
      'Gerenciar acessos exige autorização específica. Dar o nome Administrador a um perfil não torna alguém administrador.',
      'A auditoria ajuda na conferência; os históricos detalhados de estoque, ponto e portaria continuam nos próprios setores.'
    ],
    exemplo: 'Para alguém apenas consultar uma tela, escolha a permissão de consulta dessa tela e confira se outro perfil da pessoa já não permite alterar ou finalizar.'
  },
  {
    id: 'suporte', nome: 'Suporte de TI', icone: 'bi-headset',
    resumo: 'Registra problemas com computadores, equipamentos e o próprio sistema para acompanhamento.',
    telas: 'Suporte → Abrir Chamado e Acompanhamento',
    passos: [
      'Escolha tipo, prioridade e módulo afetado. Escreva um título curto e uma descrição do que aconteceu.',
      'Informe solicitante, setor, contato e as tentativas de solução, quando houver.',
      'Envie e guarde o número do chamado. Use o acompanhamento e os filtros para consultar o registro.'
    ],
    cuidados: [
      'Descreva a tela, a ação que tentou fazer e a mensagem apresentada. Nunca informe sua senha na descrição.',
      'A abertura define o chamado como Aberto e gera número e horário automaticamente.',
      'A tela permite abrir e consultar chamados; não oferece atribuição de técnico nem mudança administrativa de status.'
    ],
    exemplo: 'Título: Não consigo abrir o estoque. Descrição: ao acessar Almoxarifado → Estoque às 9h, aparece a mensagem de falha de conexão.'
  }
];
