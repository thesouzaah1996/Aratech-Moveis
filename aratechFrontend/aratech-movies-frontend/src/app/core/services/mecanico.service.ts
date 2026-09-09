import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Mecanico, MecanicoForm } from '../models/mecanico.model';
import { LookupItem } from '../models/lookup.model';
import { environment } from '../../../environments/environment';

interface ApiResponse {
  status: number;
  message: string;
  mecanico?: Mecanico;
  mecanicos?: Mecanico[];
  mecanicosLookup?: LookupItem[];
}

@Injectable({ providedIn: 'root' })
export class MecanicoService {
  private readonly api = `${environment.apiUrl}/manutencao/mecanico`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Mecanico[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/todos`)
      .pipe(map(res => res.mecanicos ?? []));
  }

  lookup(): Observable<LookupItem[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/opcoes-mecanico`)
      .pipe(map(res => res.mecanicosLookup ?? []));
  }

  add(form: MecanicoForm): Observable<Mecanico> {
    return this.http
      .post<ApiResponse>(`${this.api}/adicionar`, form)
      .pipe(map(res => res.mecanico!));
  }

  update(id: number, form: MecanicoForm): Observable<Mecanico> {
    return this.http
      .put<ApiResponse>(`${this.api}/atualizar/${id}`, form)
      .pipe(map(res => res.mecanico!));
  }

  enable(id: number): Observable<void> {
    return this.http.patch<void>(`${this.api}/ativar/${id}`, {});
  }

  disable(id: number): Observable<void> {
    return this.http.patch<void>(`${this.api}/desativar/${id}`, {});
  }
}
