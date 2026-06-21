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
  representante?: Representante;
  ativo?: boolean;
}

export interface FornecedorForm {
  nome: string;
  email: string;
  telefone: string;
  representante: Representante;
}
