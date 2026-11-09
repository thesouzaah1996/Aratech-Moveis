export type SetorResponsavel = 'ALMOXARIFADO' | 'CARREGAMENTO' | 'PCP';
export type TipoOperacao = 'CARREGAR' | 'DESCARREGAR';
export type StatusCaminhao = 'AGUARDANDO' | 'AUTORIZADO' | 'EM_RECEBIMENTO' | 'FINALIZADO';
export interface RegistroChegada {
  id: number; notaFiscal: string; empresa: string; nomeMotorista: string; placa: string;
  tipoOperacao?: TipoOperacao | '' | null;
  descricaoCarga: string; setorResponsavel: SetorResponsavel; status: StatusCaminhao;
  dataChegada: string; dataAutorizacao: string | null; dataEntrada: string | null; dataFinalizacao: string | null;
}
export interface RegistroChegadaForm {
  notaFiscal: string; empresa: string; nomeMotorista: string; placa: string;
  tipoOperacao?: TipoOperacao | '' | null;
  descricaoCarga: string; setorResponsavel: SetorResponsavel | '';
}
export interface PaginaChegadas<T = RegistroChegada> {
  content: T[]; number: number; size: number; totalElements: number; totalPages: number;
}
export interface ResumoFila {
  aguardando: number; autorizados: number; emRecebimento: number; ultimaChegadaId: number | null; ultimaEmpresa: string | null;
}
export const SETOR_LABELS: Record<SetorResponsavel, string> = { ALMOXARIFADO: 'Almoxarifado', CARREGAMENTO: 'Carregamento', PCP: 'PCP' };
export const STATUS_CAMINHAO_LABELS: Record<StatusCaminhao, string> = {
  AGUARDANDO: 'Aguardando', AUTORIZADO: 'Autorizado', EM_RECEBIMENTO: 'Em recebimento', FINALIZADO: 'Finalizado'
};
