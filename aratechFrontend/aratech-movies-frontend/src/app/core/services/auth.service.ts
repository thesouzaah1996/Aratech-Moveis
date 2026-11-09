import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { SessaoState, SessaoUsuario } from './sessao-state.service';
import { CsrfService } from './csrf.service';
interface ApiResponse { mensagem: string; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = '/aratech/auth';
  constructor(private http: HttpClient, private state: SessaoState, private csrf: CsrfService) {
    localStorage.removeItem('token'); localStorage.removeItem('perfis');
  }
  login(emailCorporativo: string, senha: string): Observable<SessaoUsuario> {
    const body = new HttpParams().set('emailCorporativo', emailCorporativo.trim().toLowerCase()).set('senha', senha);
    return this.http.post<SessaoUsuario>(`${this.api}/entrar`, body).pipe(tap(usuario => {
      this.state.usuario.set(usuario); this.csrf.limpar();
    }));
  }
  verificarSessao(): Observable<SessaoUsuario> {
    return this.http.get<SessaoUsuario>(`${this.api}/sessao`).pipe(tap(usuario => this.state.usuario.set(usuario)), catchError(erro => { if (erro.status === 401) { this.state.usuario.set(null); this.csrf.limpar(); } return throwError(() => erro); }));
  }
  logout(): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.api}/sair`, {}).pipe(tap(() => { this.state.usuario.set(null); this.csrf.limpar(); }));
  }
  alterarSenha(senhaAtual: string, novaSenha: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.api}/senha`, { senhaAtual, novaSenha }).pipe(tap(() => { this.state.usuario.set(null); this.csrf.limpar(); }));
  }
  getPerfis(): string[] { return this.state.usuario()?.perfis ?? []; }
  isAuthenticated(): boolean { return !!this.state.usuario(); }
  hasAnyRole(roles: string[]): boolean { return roles.some(role => this.getPerfis().includes(role)); }
  temPermissao(permissao: string): boolean { return this.state.permite(permissao); }
}
