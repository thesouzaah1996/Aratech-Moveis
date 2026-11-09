import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ChamadoTi, ChamadoTiForm, ChamadoTiResponse, ChamadosTiPagina, StatusChamadoTi } from '../models/chamado-ti.model';
@Injectable({ providedIn: 'root' })
export class ChamadoTiService {
  private readonly url = '/aratech/suporte/chamados';
  constructor(private readonly http: HttpClient) {}
  abrir(form: ChamadoTiForm, chaveOperacao: string): Observable<ChamadoTi> {
    return this.http.post<ChamadoTiResponse>(this.url, { ...form, chaveOperacao }).pipe(map(r => r.chamado));
  }
  getPagina(pagina = 0, tamanho = 8, status?: StatusChamadoTi, busca = ''): Observable<ChamadosTiPagina> {
    let params = new HttpParams().set('pagina',pagina).set('tamanho',tamanho);
    if (status) params = params.set('status',status);
    if (busca.trim()) params = params.set('busca',busca.trim());
    return this.http.get<ChamadosTiPagina>(this.url, { params });
  }
  getById(id: number): Observable<ChamadoTi> {
    return this.http.get<ChamadoTiResponse>(`${this.url}/${id}`).pipe(map(r => r.chamado));
  }
}
