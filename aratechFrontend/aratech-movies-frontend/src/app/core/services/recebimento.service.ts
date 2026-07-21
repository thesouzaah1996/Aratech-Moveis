import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Recebimento } from '../models/recebimento.model';
import { environment } from '../../../environments/environment';

interface ApiResponse {
  status: number;
  mensagem: string;
  carga?: Recebimento;
  recebimentos?: Recebimento[];
  historicoRecebimentos?: Recebimento[];
}

@Injectable({ providedIn: 'root' })
export class RecebimentoService {
  private readonly api = `${environment.apiUrl}/almoxarifado/recebimento`;

  constructor(private http: HttpClient) {}

  getTodos(): Observable<Recebimento[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/todos`)
      .pipe(map(res => res.recebimentos ?? []));
  }

  getHistorico(): Observable<Recebimento[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/historico`)
      .pipe(map(res => res.historicoRecebimentos ?? []));
  }

  autorizar(notaFiscal: string): Observable<Recebimento> {
    return this.http
      .post<ApiResponse>(`${this.api}/autorizar/${notaFiscal}`, {})
      .pipe(map(res => res.carga!));
  }

  finalizar(notaFiscal: string): Observable<Recebimento> {
    return this.http
      .put<ApiResponse>(`${this.api}/finalizar/${notaFiscal}`, {})
      .pipe(map(res => res.carga!));
  }
}
