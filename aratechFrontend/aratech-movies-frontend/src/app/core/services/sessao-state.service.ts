import { Injectable, signal } from '@angular/core';
import { permiteCodigo } from '../permissoes';

export interface SessaoUsuario {
  id: number; nome: string; email: string; administrador: boolean; perfis: string[]; permissoes: string[]; trocarSenha: boolean;
}
@Injectable({ providedIn: 'root' })
export class SessaoState {
  readonly usuario = signal<SessaoUsuario | null>(null);
  permite(permissao: string): boolean {
    const usuario = this.usuario();
    return !!usuario && !usuario.trocarSenha && (usuario.administrador || permiteCodigo(usuario.permissoes,permissao));
  }
}
