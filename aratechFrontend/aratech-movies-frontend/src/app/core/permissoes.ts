export const TELAS: Record<string,string> = {
 'corte/ordens':'CORTE_LOTES','almoxarifado/separacoes':'ALMOXARIFADO_REQUISICOES','producao/furacao':'PRODUCAO_FURACAO','producao/montagem':'PRODUCAO_MONTAGEM','embalagem/ordens':'EMBALAGEM_LOTES',
  'embalagem/lotes':'EMBALAGEM_LOTES','embalagem/esteiras':'EMBALAGEM_ESTEIRAS',
  'pcp/lotes':'PCP_LOTES','corte/lotes':'CORTE_LOTES','administracao/guarda-roupas':'ADMINISTRACAO_GUARDA_ROUPAS',
  'pcp/requisicoes':'PCP_REQUISICOES','almoxarifado/requisicoes':'ALMOXARIFADO_REQUISICOES',
  'administracao/receber-assistencia':'ADMINISTRACAO_LOTES',
  'almoxarifado/estoque-produtos':'ALMOXARIFADO_ESTOQUE','almoxarifado/categorias':'ALMOXARIFADO_CATEGORIAS','almoxarifado/fornecedores':'ALMOXARIFADO_FORNECEDORES','almoxarifado/recebimento':'ALMOXARIFADO_RECEBIMENTO','almoxarifado/pedidos':'ALMOXARIFADO_PEDIDOS',
  'portaria/controle-acesso':'PORTARIA_CONTROLE','carregamento/cargas-descargas':'CARREGAMENTO_CHEGADAS','carregamento/assistencia':'CARREGAMENTO_ASSISTENCIA','carregamento/expedicao':'CARREGAMENTO_EXPEDICAO',
  'manutencao/solicitar-manutencao':'MANUTENCAO_CHAMADOS','manutencao/acompanhamento':'MANUTENCAO_CHAMADOS','manutencao/fila-chamados':'MANUTENCAO_FILA','manutencao/solicitar-peca':'MANUTENCAO_PECAS','manutencao/estoque':'MANUTENCAO_ESTOQUE','manutencao/mecanicos':'MANUTENCAO_MECANICOS','manutencao/pedidos':'MANUTENCAO_PEDIDOS',
  'assistencia/lotes':'ASSISTENCIA_LOTES','assistencia/estoque-pecas':'ASSISTENCIA_ESTOQUE',
  'recursos-humanos/funcionarios':'RH_FUNCIONARIOS','recursos-humanos/ponto':'RH_PONTO','recursos-humanos/ferias':'RH_AFASTAMENTOS','recursos-humanos/solicitacoes':'RH_SOLICITACOES',
  'pcp/ordens':'PCP_ORDENS','pcp/programacao':'PCP_PROGRAMACAO','pcp/capacidade':'PCP_CAPACIDADE','pcp/linhas':'PCP_CAPACIDADE','pcp/kpis':'PCP_INDICADORES',
  'compras/pedidos':'COMPRAS_PEDIDOS','compras/cotacoes':'COMPRAS_COTACOES','financeiro/contas-pagar':'FINANCEIRO_PAGAR','financeiro/contas-receber':'FINANCEIRO_RECEBER','financeiro/baixa-notas':'FINANCEIRO_NOTAS','financeiro/fluxo-caixa':'FINANCEIRO_CAIXA',
  'administracao/auditoria':'ADMINISTRACAO_AUDITORIA','administracao/relatorios':'ADMINISTRACAO_RELATORIOS','administracao/parametros':'ADMINISTRACAO_PARAMETROS','administracao/lotes':'ADMINISTRACAO_LOTES','suporte/chamado-ti':'SUPORTE_CHAMADOS','suporte/acompanhamento':'SUPORTE_CHAMADOS','comercial/pedidos':'COMERCIAL_PEDIDOS'
};
export function permiteCodigo(permissoes: string[], codigo: string): boolean {
  if(permissoes.includes(codigo))return true;
  const [modulo,acao]=codigo.split('.');
  if(modulo.includes('_'))return permissoes.includes(modulo.split('_')[0]+'.'+acao);
  return acao==='CONSULTAR' && permissoes.some(p=>p.startsWith(modulo+'_')&&p.endsWith('.CONSULTAR'));
}
