import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Perfil, Pagina, UsuarioPerfil } from '../models/perfil.model';

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly url = '/aratech/rh/perfis';
  constructor(private http: HttpClient) {}

  listar(pagina = 0, tamanho = 8, busca = ''): Observable<Pagina<Perfil>> {
    const params = new HttpParams().set('pagina', pagina).set('tamanho', tamanho).set('busca', busca.trim());
    return this.http.get<Pagina<Perfil>>(this.url, { params });
  }
  add(nome: string): Observable<Perfil> { return this.http.post<Perfil>(this.url, { nome }); }
  update(id: number, nome: string): Observable<Perfil> { return this.http.put<Perfil>(`${this.url}/${id}`, { nome }); }
  buscarUsuarios(busca: string, pagina = 0, tamanho = 8): Observable<Pagina<UsuarioPerfil>> {
    return this.http.get<Pagina<UsuarioPerfil>>(`${this.url}/usuarios`, { params: new HttpParams().set('busca', busca.trim()).set('pagina', pagina).set('tamanho', tamanho) });
  }
  atribuir(usuarioId: number, nome: string): Observable<UsuarioPerfil> {
    return this.http.post<UsuarioPerfil>(`${this.url}/usuarios/${usuarioId}/atribuicoes`, { nome: nome.trim() });
  }
  catalogoPermissoes(): Observable<string[]> { return this.http.get<string[]>('/aratech/auth/usuarios/permissoes'); }
  permissoes(id: number): Observable<string[]> { return this.http.get<string[]>(`/aratech/auth/usuarios/perfis/${id}/permissoes`); }
  salvarPermissoes(id: number, permissoes: string[]): Observable<string[]> { return this.http.put<string[]>(`/aratech/auth/usuarios/perfis/${id}/permissoes`, permissoes); }
  criarAcesso(id: number): Observable<{ senhaTemporaria: string }> { return this.http.post<{ senhaTemporaria: string }>(`/aratech/auth/usuarios/${id}/acesso`, {}); }
}
