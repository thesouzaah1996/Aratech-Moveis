import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Perfil } from '../models/perfil.model';
import { environment } from '../../../environments/environment';

interface ApiResponse {
  status: number;
  mensagem: string;
  perfil?: Perfil;
  perfis?: Perfil[];
}

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly api = `${environment.apiUrl}/recursoshumanos/perfil`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Perfil[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/todos`)
      .pipe(map(res => res.perfis ?? []));
  }

  add(nome: string): Observable<Perfil> {
    return this.http
      .post<ApiResponse>(`${this.api}/adicionar`, { nome })
      .pipe(map(res => res.perfil!));
  }

  update(id: number, nome: string): Observable<Perfil> {
    return this.http
      .put<ApiResponse>(`${this.api}/atualizar/${id}`, { nome })
      .pipe(map(res => res.perfil!));
  }

  ativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.api}/ativar/${id}`, {});
  }

  desativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.api}/desativar/${id}`, {});
  }
}
