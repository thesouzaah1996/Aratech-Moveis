export type SetorResponsavel = 'ALMOXARIFADO' | 'CARREGAMENTO' | 'PCP';
export type StatusCaminhao = 'AGUARDANDO' | 'AUTORIZADO' | 'FINALIZADO';

export interface RegistroChegada {
  id: number;
  notaFiscal: string;
  empresa: string;
  nomeMotorista: string;
  placa: string;
  setorResponsavel: SetorResponsavel;
  status: StatusCaminhao;
  dataEntrada: string;
}

export interface RegistroChegadaForm {
  notaFiscal: string;
  empresa: string;
  nomeMotorista: string;
  placa: string;
  setorResponsavel: SetorResponsavel | '';
}

export const SETOR_LABELS: Record<SetorResponsavel, string> = {
  ALMOXARIFADO: 'Almoxarifado',
  CARREGAMENTO: 'Carregamento',
  PCP: 'PCP'
};

export const STATUS_CAMINHAO_LABELS: Record<StatusCaminhao, string> = {
  AGUARDANDO: 'Aguardando',
  AUTORIZADO: 'Autorizado',
  FINALIZADO: 'Finalizado'
};
