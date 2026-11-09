export interface PecaEstoque {
  id: number;
  nome: string;
  codigo: string;
  quantidade: number;
  quantidadeMinima?: number | null;
  unidade: string;
  localizacao: string;
  descricao?: string;
  ativo?: boolean;
  criadoEm?: string;
}

export interface PecaEstoqueForm {
  id?: number;
  nome: string;
  codigo: string;
  quantidade: number;
  quantidadeMinima?: number | null;
  unidade: string;
  localizacao: string;
  descricao: string;
}

export interface PecasEstoquePagina { status: number; pecas: PecaEstoque[]; pagina: number; tamanho: number; total: number; }
export interface PecaEstoqueResponse { status: number; mensagem?: string; peca: PecaEstoque; }
export interface MovimentacaoPecaEstoque { id: number; tipo: 'INICIAL' | 'ENTRADA' | 'SAIDA'; quantidade: number; saldoAnterior: number; saldoPosterior: number; criadoEm: string; responsavelNome: string; motivo: string; }
export interface MovimentacoesPecasPagina { status: number; movimentacoes: MovimentacaoPecaEstoque[]; pagina: number; tamanho: number; total: number; }
