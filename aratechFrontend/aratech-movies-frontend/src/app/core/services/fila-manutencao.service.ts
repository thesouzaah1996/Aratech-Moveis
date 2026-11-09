import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { FilaManutencaoPagina, FilaManutencaoResponse, ItemFilaManutencao } from '../models/fila-manutencao.model';
import { StatusChamado } from '../models/chamado.model';
@Injectable({ providedIn: 'root' })
export class FilaManutencaoService {
  private readonly url = '/aratech/manutencao/fila';
  constructor(private readonly http: HttpClient) {}
  mecanicos() { return this.http.get<{id:number;nome:string}[]>(`${this.url}/mecanicos`); }
  getPagina(pagina = 0, tamanho = 8, status?: StatusChamado, busca = ''): Observable<FilaManutencaoPagina> {
    let params = new HttpParams().set('pagina', pagina).set('tamanho', tamanho);
    if (status) params = params.set('status', status);
    if (busca.trim()) params = params.set('busca', busca.trim());
    return this.http.get<FilaManutencaoPagina>(this.url, { params });
  }
  getById(id: number): Observable<ItemFilaManutencao> { return this.http.get<FilaManutencaoResponse>(`${this.url}/${id}`).pipe(map(r => r.item)); }
  atribuirMecanico(id: number, mecanicoId: number): Observable<ItemFilaManutencao> {
    return this.http.post<FilaManutencaoResponse>(`${this.url}/${id}/mecanico`, { mecanicoId }).pipe(map(r => r.item));
  }
  concluir(id: number): Observable<ItemFilaManutencao> { return this.http.post<FilaManutencaoResponse>(`${this.url}/${id}/concluir`, {}).pipe(map(r => r.item)); }
}
