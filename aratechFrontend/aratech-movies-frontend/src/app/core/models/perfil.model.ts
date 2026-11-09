export interface Perfil { id: number; nome: string; }
export interface UsuarioPerfil { id: number; nome: string; email: string; setor: string; perfis: Perfil[]; }
export interface Pagina<T> { content: T[]; number: number; size: number; totalElements: number; totalPages: number; }
