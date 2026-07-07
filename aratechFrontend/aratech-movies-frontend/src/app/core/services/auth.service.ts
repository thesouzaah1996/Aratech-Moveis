import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

interface ApiResponse {
  status: number;
  mensagem: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = `${environment.apiUrl}/login/auth`;

  constructor(private http: HttpClient) {}

  solicitarRedefinicaoSenha(email: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.api}/redefinir-senha`, { email });
  }

  confirmarRedefinicaoSenha(codigo: string, novaSenha: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.api}/reset-password`, { codigo, novaSenha });
  }

  solicitarPrimeiroAcesso(emailPessoal: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.api}/primeiro-acesso`, { emailPessoal });
  }
}
