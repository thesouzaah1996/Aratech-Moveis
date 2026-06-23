export interface Produto {
  id: number;
  categoriaID: number | null;
  nome: string;
  sku: string;
  quantidade: number;
  descricao?: string;
  vencimentoProduto?: string | null;
  atualizadoEm?: string;
  criadoEm?: string;
}

export interface ProdutoForm {
  id?: number;
  categoriaID: number | null;
  nome: string;
  sku: string;
  quantidade: number;
  descricao: string;
  vencimentoProduto: string | null;
}
