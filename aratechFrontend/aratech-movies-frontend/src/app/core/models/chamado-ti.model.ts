export type TipoChamadoTi = 'Acesso / Permissão' | 'Hardware' | 'Software / Sistema' | 'Rede / Internet' | 'Impressora' | 'E-mail / Comunicação' | 'Outros';
export type PrioridadeTi = 'Baixa' | 'Média' | 'Alta' | 'Crítica';
export type StatusChamadoTi = 'Aberto' | 'Em Atendimento' | 'Concluído' | 'Cancelado';
export interface ChamadoTiForm {
  tipo: TipoChamadoTi | ''; prioridade: PrioridadeTi | ''; moduloAfetado: string; titulo: string;
  solicitante: string; setor: string; contato: string; descricao: string; tentativasSolucao: string;
}
export interface ChamadoTi {
  id: number; numero: string; tipo: TipoChamadoTi; prioridade: PrioridadeTi; moduloAfetado: string; titulo: string;
  solicitante: string; setor: string; contato: string; descricao: string; tentativasSolucao: string;
  status: StatusChamadoTi; dataAbertura: string;
}
export interface ChamadosTiPagina { status: number; chamados: ChamadoTi[]; pagina: number; tamanho: number; total: number; }
export interface ChamadoTiResponse { status: number; mensagem?: string; chamado: ChamadoTi; }
export const TIPOS_CHAMADO_TI: TipoChamadoTi[] = ['Acesso / Permissão','Hardware','Software / Sistema','Rede / Internet','Impressora','E-mail / Comunicação','Outros'];
export const MODULOS_CHAMADO_TI = ['', 'Login / Autenticação','Dashboard','Almoxarifado','Portaria','Carregamento','Manutenção','Assistência Técnica','Recursos Humanos','PCP','Compras','Financeiro','Administração'];
