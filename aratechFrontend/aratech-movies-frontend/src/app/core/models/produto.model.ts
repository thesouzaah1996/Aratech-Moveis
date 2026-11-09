export interface Produto {
  id: number;
  categoriaID: number;
  categoriaNome: string;
  fornecedorID: number;
  fornecedorNome: string;
  nome: string;
  sku: string;
  quantidade: number;
  quantidadeMinima?: number | null;
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
  quantidadeMinima?: number | null;
  localArmazenamento: string;
  descricao: string;
  vencimentoProduto: string | null;
}

export interface ProdutosPagina {
  status: number;
  produtos: Produto[];
  pagina: number;
  tamanho: number;
  total: number;
}

export interface ProdutoResponse {
  status: number;
  mensagem: string;
  produto: Produto;
}

export interface MovimentacaoEstoque {
  id:number;tipo:'INICIAL'|'ENTRADA'|'SAIDA';quantidade:number;saldoAnterior:number;saldoPosterior:number;
  criadoEm:string;responsavelNome:string;motivo:string;
}
export interface MovimentacoesPagina {
  status:number;movimentacoes:MovimentacaoEstoque[];pagina:number;tamanho:number;total:number;
}
