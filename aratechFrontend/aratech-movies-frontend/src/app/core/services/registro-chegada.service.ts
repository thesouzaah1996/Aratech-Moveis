import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { RegistroChegada, RegistroChegadaForm } from '../models/registro-chegada.model';
import { environment } from '../../../environments/environment';

interface ApiResponse {
  status: number;
  mensagem: string;
  registroChegada?: RegistroChegada;
  filaRegistroChegada?: RegistroChegada[];
  historicoRegistroChegada?: RegistroChegada[];
  autorizados?: RegistroChegada[];
}

@Injectable({ providedIn: 'root' })
export class RegistroChegadaService {
  private readonly api = `${environment.apiUrl}/portaria/registro-chegada`;

  constructor(private http: HttpClient) {}

  getFila(): Observable<RegistroChegada[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/fila`)
      .pipe(map(res => res.filaRegistroChegada ?? []));
  }

  getHistorico(): Observable<RegistroChegada[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/historico`)
      .pipe(map(res => res.historicoRegistroChegada ?? []));
  }

  getAutorizados(): Observable<RegistroChegada[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/autorizados`)
      .pipe(map(res => res.autorizados ?? []));
  }

  add(form: RegistroChegadaForm): Observable<RegistroChegada> {
    return this.http
      .post<ApiResponse>(`${this.api}/registrar`, form)
      .pipe(map(res => res.registroChegada!));
  }

  atualizar(form: RegistroChegadaForm): Observable<RegistroChegada> {
    return this.http
      .put<ApiResponse>(`${this.api}/atualizar`, form)
      .pipe(map(res => res.registroChegada!));
  }

  finalizar(notaFiscal: string): Observable<RegistroChegada> {
    return this.http
      .put<ApiResponse>(`${this.api}/finalizar`, null, { params: { notaFiscal } })
      .pipe(map(res => res.registroChegada!));
  }
}
