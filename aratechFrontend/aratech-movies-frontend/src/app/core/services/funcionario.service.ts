import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Funcionario, FuncionarioForm } from '../models/funcionario.model';
import { environment } from '../../../environments/environment';

interface ApiResponse {
  status: number;
  mensagem: string;
  funcionario?: Funcionario;
  funcionarios?: Funcionario[];
}

@Injectable({ providedIn: 'root' })
export class FuncionarioService {
  private readonly api = `${environment.apiUrl}/recursoshumanos/funcionario`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Funcionario[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/todos`)
      .pipe(map(res => res.funcionarios ?? []));
  }

  search(nome: string): Observable<Funcionario[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/buscar`, { params: { nome } })
      .pipe(map(res => res.funcionarios ?? []));
  }

  add(form: FuncionarioForm): Observable<Funcionario> {
    return this.http
      .post<ApiResponse>(`${this.api}/adicionar`, form)
      .pipe(map(res => res.funcionario!));
  }

  enable(id: number): Observable<Funcionario> {
    return this.http
      .patch<ApiResponse>(`${this.api}/ativar/${id}`, {})
      .pipe(map(res => res.funcionario!));
  }

  disable(id: number): Observable<Funcionario> {
    return this.http
      .patch<ApiResponse>(`${this.api}/desativar/${id}`, {})
      .pipe(map(res => res.funcionario!));
  }

  atribuirPerfis(id: number, perfisIds: number[]): Observable<Funcionario> {
    return this.http
      .patch<ApiResponse>(`${this.api}/perfis/${id}`, { perfisIds })
      .pipe(map(res => res.funcionario!));
  }
}
