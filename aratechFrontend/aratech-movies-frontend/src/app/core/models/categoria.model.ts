export interface Categoria {
  id: number;
  nome: string;
  ativo: boolean;
}

export interface CategoriasPagina {
  status: number;
  categorias: Categoria[];
  pagina: number;
  tamanho: number;
  total: number;
}

export interface CategoriaResponse {
  status: number;
  mensagem: string;
  categoria: Categoria;
}
