import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { SolicitacaoPeca, SolicitacaoPecaForm, SolicitacaoPecaResponse } from '../models/solicitacao-peca.model';
@Injectable({ providedIn: 'root' })
export class SolicitacaoPecaService {
  private readonly url = '/aratech/manutencao/solicitacoes-pecas';
  constructor(private readonly http: HttpClient) {}
  add(form: SolicitacaoPecaForm, chaveOperacao: string): Observable<SolicitacaoPeca> {
    return this.http.post<SolicitacaoPecaResponse>(this.url, { ...form, chaveOperacao }).pipe(map(r => r.solicitacao));
  }
  getById(id: number): Observable<SolicitacaoPeca> {
    return this.http.get<SolicitacaoPecaResponse>(`${this.url}/${id}`).pipe(map(r => r.solicitacao));
  }
}
