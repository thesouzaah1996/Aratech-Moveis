import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, EMPTY, expand, reduce } from 'rxjs';
import { RegistroChegada, RegistroChegadaForm, PaginaChegadas, ResumoFila } from '../models/registro-chegada.model';
@Injectable({ providedIn: 'root' })
export class RegistroChegadaService {
  private readonly url = '/aratech/portaria/chegadas';
  constructor(private http: HttpClient) {}
  getFila(pagina = 0, tamanho = 8): Observable<PaginaChegadas> {
    return this.http.get<PaginaChegadas>(this.url, { params: new HttpParams().set('pagina', pagina).set('tamanho', tamanho) });
  }
  getHistorico(pagina = 0, tamanho = 8, notaFiscal = '', data = ''): Observable<PaginaChegadas> {
    let params = new HttpParams().set('pagina', pagina).set('tamanho', tamanho);
    if (notaFiscal.trim()) params = params.set('notaFiscal', notaFiscal.trim());
    if (data) params = params.set('data', data);
    return this.http.get<PaginaChegadas>(this.url + '/historico', { params });
  }
  getHistoricoCompleto(notaFiscal = '', data = ''): Observable<RegistroChegada[]> {
    return this.getHistorico(0, 100, notaFiscal, data).pipe(
      expand(p => p.content.length > 0 && p.number + 1 < p.totalPages ? this.getHistorico(p.number + 1, 100, notaFiscal, data) : EMPTY),
      reduce((itens, p) => [...itens, ...p.content], [] as RegistroChegada[])
    );
  }
  resumo(): Observable<ResumoFila> { return this.http.get<ResumoFila>(this.url + '/resumo'); }
  add(form: RegistroChegadaForm, chaveOperacao: string): Observable<RegistroChegada> {
    return this.http.post<RegistroChegada>(this.url, { ...form, chaveOperacao });
  }
  atualizar(id: number, form: RegistroChegadaForm): Observable<RegistroChegada> {
    return this.http.put<RegistroChegada>(`${this.url}/${id}`, form);
  }
  finalizar(id: number): Observable<RegistroChegada> {
    return this.http.post<RegistroChegada>(`${this.url}/${id}/finalizar`, {});
  }
  confirmarEntrada(id: number): Observable<RegistroChegada> {
    return this.http.post<RegistroChegada>(`${this.url}/${id}/entrada`, {});
  }
}
