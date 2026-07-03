import { PrioridadeChamado } from './chamado.model';

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
