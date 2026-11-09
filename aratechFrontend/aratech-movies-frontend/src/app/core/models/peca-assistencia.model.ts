export interface PecaAssistencia {
  id: number;
  nome: string;
  codigo: string;
  modelo: string;
  quantidadeMinima: number;
  quantidade: number;
  unidade: string;
  localizacao: string;
  descricao?: string;
  ativo?: boolean;
  criadoEm?: string;
}

export interface PecaAssistenciaForm {
  id?: number;
  nome: string;
  codigo: string;
  modelo: string;
  quantidadeMinima: number;
  quantidade: number;
  unidade: string;
  localizacao: string;
  descricao: string;
}

export interface PecasAssistenciaPagina { status: number; pecas: PecaAssistencia[]; totalAbaixoMinimo: number; pagina: number; tamanho: number; total: number; }
export interface PecaAssistenciaResponse { status: number; mensagem?: string; peca: PecaAssistencia; }
export interface MovimentacaoPecaAssistencia { id: number; tipo: 'INICIAL' | 'ENTRADA' | 'SAIDA'; quantidade: number; saldoAnterior: number; saldoPosterior: number; criadoEm: string; responsavelNome: string; motivo: string; }
export interface MovimentacoesAssistenciaPagina { status: number; movimentacoes: MovimentacaoPecaAssistencia[]; pagina: number; tamanho: number; total: number; }
