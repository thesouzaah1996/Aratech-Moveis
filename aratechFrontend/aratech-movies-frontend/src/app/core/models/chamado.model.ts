export type TipoManutencao = 'CORRETIVA' | 'PREVENTIVA';
export type PrioridadeChamado = 'BAIXA' | 'MEDIA' | 'ALTA';
export type StatusChamado = 'ABERTA' | 'EM_MANUTENCAO' | 'CONCLUIDA';

export interface Chamado {
  id: number;
  equipamento: string;
  tipo: TipoManutencao;
  prioridade: PrioridadeChamado;
  solicitante: string;
  setor: string;
  telefone?: string;
  descricao: string;
  mecanico?: string;
  status: StatusChamado;
  dataAbertura: string;
}

export interface ChamadoForm {
  equipamento: string;
  tipo: TipoManutencao | '';
  prioridade: PrioridadeChamado | '';
  solicitante: string;
  setor: string;
  telefone: string;
  descricao: string;
}

export const TIPO_MANUTENCAO_LABELS: Record<TipoManutencao, string> = {
  CORRETIVA: 'Corretiva',
  PREVENTIVA: 'Preventiva'
};

export const PRIORIDADE_LABELS: Record<PrioridadeChamado, string> = {
  BAIXA: 'Baixa',
  MEDIA: 'Média',
  ALTA: 'Alta'
};

export const STATUS_CHAMADO_LABELS: Record<StatusChamado, string> = {
  ABERTA: 'Aberta',
  EM_MANUTENCAO: 'Em Manutenção',
  CONCLUIDA: 'Concluída'
};
