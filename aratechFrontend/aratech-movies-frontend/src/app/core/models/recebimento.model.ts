export type StatusRecebimento = 'AGUARDANDO' | 'AUTORIZADO' | 'EM_RECEBIMENTO' | 'FINALIZADO';

export interface Recebimento {
  id: number;
  notaFiscal: string;
  empresa: string;
  nomeMotorista: string;
  placa: string;
  descricaoCarga: string;
  setorResponsavel: string;
  statusRecebimento: StatusRecebimento;
  dataRecebimento: string;
}

export const STATUS_RECEBIMENTO_LABELS: Record<StatusRecebimento, string> = {
  AGUARDANDO: 'Aguardando',
  AUTORIZADO: 'Autorizado',
  EM_RECEBIMENTO: 'Em recebimento',
  FINALIZADO: 'Finalizado'
};
