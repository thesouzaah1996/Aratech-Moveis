export interface Produto {
  id: number;
  categoriaID: number | null;
  fornecedorID?: number | null;
  nome: string;
  sku: string;
  quantidade: number;
  localArmazenamento: string;
  descricao?: string;
  vencimentoProduto?: string | null;
  criadoEm?: string;
}

export interface ProdutoForm {
  id?: number;
  categoriaID: number | null;
  fornecedorID: number | null;
  nome: string;
  sku: string;
  quantidade: number;
  localArmazenamento: string;
  descricao: string;
  vencimentoProduto: string | null;
}
