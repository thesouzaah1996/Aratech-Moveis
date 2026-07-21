import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

interface ApiResponse {
  status: number;
  mensagem: string;
}

interface LoginResponse extends ApiResponse {
  dados: {
    token: string;
    perfis: string[];
  };
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = `${environment.apiUrl}/login/auth`;

  constructor(private http: HttpClient) {}

  login(emailCorporativo: string, senha: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.api}/entrar`, { emailCorporativo, senha });
  }

  solicitarRedefinicaoSenha(email: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.api}/redefinir-senha`, { email });
  }

  confirmarRedefinicaoSenha(codigo: string, novaSenha: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.api}/reset-password`, { codigo, novaSenha });
  }

  solicitarPrimeiroAcesso(emailPessoal: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.api}/primeiro-acesso/solicitar`, { emailPessoal });
  }

  confirmarPrimeiroAcesso(emailCorporativo: string, codigo: string, novaSenha: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.api}/primeiro-acesso/confirmar`, { emailCorporativo, codigo, novaSenha });
  }

  salvarSessao(token: string, perfis: string[]): void {
    localStorage.setItem('token', token);
    localStorage.setItem('perfis', JSON.stringify(perfis));
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('perfis');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getPerfis(): string[] {
    try {
      return JSON.parse(localStorage.getItem('perfis') ?? '[]');
    } catch {
      return [];
    }
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  hasAnyRole(roles: string[]): boolean {
    const perfis = this.getPerfis();
    return roles.some(role => perfis.includes(role));
  }
}
