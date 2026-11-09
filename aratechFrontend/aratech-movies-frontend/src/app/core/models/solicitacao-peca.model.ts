import { PrioridadeChamado, StatusChamado } from './chamado.model';

export type FinalidadePeca = 'CORRETIVA' | 'PREVENTIVA' | 'MELHORIA' | 'ESTOQUE';

export interface SolicitacaoPeca {
  id: number;
  nomePeca: string;
  codigo?: string;
  quantidade: number;
  unidade: string;
  equipamento: string;
  finalidade: FinalidadePeca;
  prioridade: PrioridadeChamado;
  solicitante: string;
  setor: string;
  telefone?: string;
  observacoes: string;
  criadoEm: string;
  status: StatusChamado;
}

export interface SolicitacaoPecaForm {
  nomePeca: string;
  codigo: string;
  quantidade: number | null;
  unidade: string;
  equipamento: string;
  finalidade: FinalidadePeca | '';
  prioridade: PrioridadeChamado | '';
  solicitante: string;
  setor: string;
  telefone: string;
  observacoes: string;
}

export interface SolicitacaoPecaResponse { status: number; mensagem?: string; solicitacao: SolicitacaoPeca; }

export const FINALIDADE_PECA_LABELS: Record<FinalidadePeca, string> = {
  CORRETIVA: 'Corretiva', PREVENTIVA: 'Preventiva', MELHORIA: 'Melhoria', ESTOQUE: 'Reposição de estoque'
};
