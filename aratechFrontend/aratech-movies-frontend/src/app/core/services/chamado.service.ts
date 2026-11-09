import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';
import { Chamado, ChamadoForm, ChamadoResponse, ChamadosPagina, StatusChamado } from '../models/chamado.model';

@Injectable({ providedIn: 'root' })
export class ChamadoService {
  private readonly url = '/aratech/manutencao/chamados';
  constructor(private readonly http: HttpClient) {}

  getPagina(pagina = 0, tamanho = 8, status?: StatusChamado): Observable<ChamadosPagina> {
    let params = new HttpParams().set('pagina', pagina).set('tamanho', tamanho);
    if (status) params = params.set('status', status);
    return this.http.get<ChamadosPagina>(this.url, { params });
  }
  getAll(status?: StatusChamado): Observable<Chamado[]> {
    return this.getPagina(0, 100, status).pipe(
      expand(p => p.chamados.length > 0 && (p.pagina + 1) * p.tamanho < p.total
        ? this.getPagina(p.pagina + 1, 100, status) : EMPTY),
      reduce((todos, p) => [...todos, ...p.chamados], [] as Chamado[])
    );
  }
  getById(id: number): Observable<Chamado> {
    return this.http.get<ChamadoResponse>(`${this.url}/${id}`).pipe(map(r => r.chamado));
  }
  add(form: ChamadoForm, chaveOperacao: string): Observable<Chamado> {
    return this.http.post<ChamadoResponse>(this.url, { ...form, chaveOperacao }).pipe(map(r => r.chamado));
  }

}
