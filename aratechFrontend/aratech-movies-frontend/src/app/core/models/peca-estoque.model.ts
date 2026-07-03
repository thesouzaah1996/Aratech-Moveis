export interface PecaEstoque {
  id: number;
  nome: string;
  codigo: string;
  quantidade: number;
  unidade: string;
  localizacao: string;
  descricao?: string;
}

export interface PecaEstoqueForm {
  id?: number;
  nome: string;
  codigo: string;
  quantidade: number;
  unidade: string;
  localizacao: string;
  descricao: string;
}
