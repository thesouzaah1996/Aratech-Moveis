export interface Representante {
  nomeRepresentante: string;
  telefoneRepresentante: string;
  emailRepresentante: string;
}

export interface Fornecedor {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  representante: Representante;
  ativo: boolean;
}

export interface FornecedorForm {
  nome: string;
  email: string;
  telefone: string;
  representante: Representante;
}

export interface FornecedoresPagina {
  status: number;
  fornecedores: Fornecedor[];
  pagina: number;
  tamanho: number;
  total: number;
}

export interface FornecedorResponse {
  status: number;
  mensagem: string;
  fornecedor: Fornecedor;
}
