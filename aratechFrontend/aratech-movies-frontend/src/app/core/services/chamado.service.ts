import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Chamado, ChamadoForm, StatusChamado } from '../models/chamado.model';
import { environment } from '../../../environments/environment';

interface ApiResponse {
  status: number;
  message: string;
  chamado?: Chamado;
  chamados?: Chamado[];
}

@Injectable({ providedIn: 'root' })
export class ChamadoService {
  private readonly api = `${environment.apiUrl}/manutencao/chamado`;

  constructor(private http: HttpClient) {}

  getAll(status?: StatusChamado): Observable<Chamado[]> {
    const params: Record<string, string> = status ? { status } : {};
    return this.http
      .get<ApiResponse>(`${this.api}/all`, { params })
      .pipe(map(res => res.chamados ?? []));
  }

  add(form: ChamadoForm): Observable<Chamado> {
    return this.http
      .post<ApiResponse>(`${this.api}/add`, form)
      .pipe(map(res => res.chamado!));
  }

  atribuirMecanico(id: number, mecanico: string): Observable<Chamado> {
    return this.http
      .put<ApiResponse>(`${this.api}/atribuir/${id}`, { mecanico })
      .pipe(map(res => res.chamado!));
  }

  concluir(id: number): Observable<Chamado> {
    return this.http
      .put<ApiResponse>(`${this.api}/concluir/${id}`, {})
      .pipe(map(res => res.chamado!));
  }
}
