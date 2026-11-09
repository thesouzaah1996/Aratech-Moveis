import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, EMPTY, expand, reduce } from 'rxjs';
import { PaginaChegadas, ResumoFila, RegistroChegadaForm } from '../models/registro-chegada.model';
import { Recebimento } from '../models/recebimento.model';
@Injectable({ providedIn: 'root' })
export class CarregamentoService {
  private readonly url = '/aratech/carregamento/chegadas';
  constructor(private http: HttpClient) {}
  getTodos(pagina = 0, tamanho = 8): Observable<PaginaChegadas<Recebimento>> {
    return this.http.get<PaginaChegadas<Recebimento>>(this.url, { params: new HttpParams().set('pagina', pagina).set('tamanho', tamanho) });
  }
  getHistorico(pagina = 0, tamanho = 8): Observable<PaginaChegadas<Recebimento>> {
    return this.http.get<PaginaChegadas<Recebimento>>(this.url + '/historico', { params: new HttpParams().set('pagina', pagina).set('tamanho', tamanho) });
  }
  getHistoricoCompleto(): Observable<Recebimento[]> {
    return this.getHistorico(0, 100).pipe(
      expand(p => p.content.length > 0 && p.number + 1 < p.totalPages ? this.getHistorico(p.number + 1, 100) : EMPTY),
      reduce((itens, p) => [...itens, ...p.content], [] as Recebimento[])
    );
  }
  resumo(): Observable<ResumoFila> { return this.http.get<ResumoFila>(this.url + '/resumo'); }
  atualizar(id: number, form: RegistroChegadaForm): Observable<Recebimento> {
    return this.http.put<Recebimento>(`${this.url}/${id}`, form);
  }
  autorizar(id: number): Observable<Recebimento> { return this.http.post<Recebimento>(`${this.url}/${id}/autorizar`, {}); }
  finalizar(id: number): Observable<Recebimento> { return this.http.post<Recebimento>(`${this.url}/${id}/finalizar`, {}); }
}
