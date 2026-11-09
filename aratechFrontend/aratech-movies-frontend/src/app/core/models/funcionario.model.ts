import { Perfil } from './perfil.model';

export interface Funcionario {
  id: number;
  nome: string;
  email: string;
  emailCorporativo?: string;
  cargo: string;
  setor: string;
  telefone?: string;
  ativo: boolean;
  perfis: Perfil[];
}

export interface FuncionarioForm {
  nome: string;
  cpf: string;
  rg?: string;
  pis?: string;
  dataNascimento: string;
  sexo: string;
  estadoCivil?: string;
  nomeMae: string;
  nomePai?: string;
  cep: string;
  logradouro: string;
  numero: string;
  complemento?: string;
  bairro: string;
  cidade: string;
  uf: string;
  telefone?: string;
  celular?: string;
  dataAdmissao: string;
  tipoContrato: string;
  jornadaHoras: number | null;
  cargo: string;
  tipoFuncionario: string;
  setor: string;
  salario: number;
  email: string;
  banco: string;
  agencia: string;
  conta: string;
  tipoConta: string;
  pix?: string;
}

export interface ArquivosFuncionario {
  rg: File | null;
  ctps: File | null;
  aso: File | null;
  compResidencia: File | null;
  tituloEleitor: File | null;
  reservista: File | null;
  diplomaEscolar: File | null;
  certidaoCasamento: File | null;
  certidaoNascimento: File | null;
}

export interface CadastroFuncionarioResponse {
  status: number;
  mensagem: string;
  funcionarioId: number;
  nomeCompleto: string;
  ativo: boolean;
}

export interface FuncionarioResumoApi {
  id: number; nomeCompleto: string; email: string; cargo: string; setor: string; telefoneFixo?: string; ativo: boolean;
}
export interface FuncionariosPagina {
  status: number; funcionarios: FuncionarioResumoApi[]; pagina: number; tamanho: number; total: number;
}
