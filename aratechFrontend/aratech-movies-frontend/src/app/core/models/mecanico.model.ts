export type Especialidade =
  | 'SECCIONADORA' | 'BORDEADEIRA' | 'CNC' | 'PRENSA' | 'FURADEIRA'
  | 'LIXADEIRA' | 'TUPIA' | 'SERRA_CIRCULAR' | 'COLADEIRA'
  | 'PANTOGRAFO' | 'ENVERNIZADEIRA' | 'COMPRESSOR';

export type Turno = 'MANHA' | 'TARDE' | 'NOITE';

export interface Mecanico {
  id: number;
  nome: string;
  especialidades: Especialidade[];
  turno: Turno;
  ativo?: boolean;
}

export interface MecanicoForm {
  nome: string;
  especialidades: Especialidade[];
  turno: Turno | '';
}

export const ESPECIALIDADES: Especialidade[] = [
  'SECCIONADORA', 'BORDEADEIRA', 'CNC', 'PRENSA', 'FURADEIRA', 'LIXADEIRA',
  'TUPIA', 'SERRA_CIRCULAR', 'COLADEIRA', 'PANTOGRAFO', 'ENVERNIZADEIRA', 'COMPRESSOR'
];

export const TURNOS: Turno[] = ['MANHA', 'TARDE', 'NOITE'];

export const ESPECIALIDADE_LABELS: Record<Especialidade, string> = {
  SECCIONADORA: 'Seccionadora',
  BORDEADEIRA: 'Bordeadeira',
  CNC: 'CNC',
  PRENSA: 'Prensa',
  FURADEIRA: 'Furadeira',
  LIXADEIRA: 'Lixadeira',
  TUPIA: 'Tupia',
  SERRA_CIRCULAR: 'Serra Circular',
  COLADEIRA: 'Coladeira',
  PANTOGRAFO: 'Pantógrafo',
  ENVERNIZADEIRA: 'Envernizadeira',
  COMPRESSOR: 'Compressor'
};

export const TURNO_LABELS: Record<Turno, string> = {
  MANHA: 'Manhã',
  TARDE: 'Tarde',
  NOITE: 'Noite'
};
