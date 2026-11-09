import { ChamadoMecanico, PrioridadeChamado, StatusChamado, TipoManutencao } from './chamado.model';
import { SolicitacaoPeca } from './solicitacao-peca.model';
export interface ItemFilaManutencao {
  id: number; origem: 'MANUTENCAO' | 'PECA'; equipamento: string; tipo: TipoManutencao | null;
  prioridade: PrioridadeChamado; solicitante: string; setor: string; telefone: string; descricao: string;
  status: StatusChamado; dataAbertura: string; mecanico: ChamadoMecanico | null;
  dataInicio: string | null; dataConclusao: string | null; peca: SolicitacaoPeca | null;
}
export interface FilaManutencaoPagina { status: number; itens: ItemFilaManutencao[]; pagina: number; tamanho: number; total: number; }
export interface FilaManutencaoResponse { status: number; mensagem?: string; item: ItemFilaManutencao; }
