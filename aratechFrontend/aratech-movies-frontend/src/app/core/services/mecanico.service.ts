import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';
import { Mecanico, MecanicoForm } from '../models/mecanico.model';
import { LookupItem } from '../models/lookup.model';
interface MecanicosPagina { mecanicos: Mecanico[]; pagina: number; tamanho: number; total: number; }
@Injectable({ providedIn: 'root' })
export class MecanicoService {
  private readonly url = '/aratech/manutencao/mecanicos';
  constructor(private readonly http: HttpClient) {}
  private pagina(pagina: number, ativo?: boolean): Observable<MecanicosPagina> {
    let params = new HttpParams().set('pagina', pagina).set('tamanho', 100);
    if (ativo !== undefined) params = params.set('ativo', ativo);
    return this.http.get<MecanicosPagina>(this.url, { params });
  }
  getAll(ativo?: boolean): Observable<Mecanico[]> {
    return this.pagina(0, ativo).pipe(expand(p => p.mecanicos.length > 0 && (p.pagina + 1) * p.tamanho < p.total ? this.pagina(p.pagina + 1, ativo) : EMPTY), reduce((todos, p) => [...todos, ...p.mecanicos], [] as Mecanico[]));
  }
  lookup(): Observable<LookupItem[]> { return this.getAll(true).pipe(map(lista => lista.map(m => ({ id: m.id, nome: m.nome })))); }
  add(form: MecanicoForm): Observable<Mecanico> { return this.http.post<{ mecanico: Mecanico }>(this.url, form).pipe(map(r => r.mecanico)); }
  update(id: number, form: MecanicoForm): Observable<Mecanico> { return this.http.put<{ mecanico: Mecanico }>(`${this.url}/${id}`, form).pipe(map(r => r.mecanico)); }
  enable(id: number): Observable<void> { return this.http.patch<void>(`${this.url}/${id}/ativar`, {}); }
  disable(id: number): Observable<void> { return this.http.patch<void>(`${this.url}/${id}/desativar`, {}); }
}
