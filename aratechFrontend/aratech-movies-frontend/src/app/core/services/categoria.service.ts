import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';
import { Categoria, CategoriaResponse, CategoriasPagina } from '../models/categoria.model';
import { LookupItem } from '../models/lookup.model';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private readonly url = '/aratech/almoxarifado/categorias';
  constructor(private http: HttpClient) {}

  getAll(pagina = 0, tamanho = 8, nome = '', ativo?: boolean): Observable<CategoriasPagina> {
    let params = new HttpParams().set('pagina', pagina).set('tamanho', tamanho);
    if (nome.trim()) params = params.set('nome', nome.trim());
    if (ativo !== undefined) params = params.set('ativo', ativo);
    return this.http.get<CategoriasPagina>(this.url, { params });
  }

  lookup(): Observable<LookupItem[]> {
    // Os seletores de produtos precisam de todas as categorias ativas, nao apenas da primeira pagina.
    return this.getAll(0, 100, '', true).pipe(
      expand(pagina => pagina.categorias.length > 0 && (pagina.pagina + 1) * pagina.tamanho < pagina.total
        ? this.getAll(pagina.pagina + 1, 100, '', true) : EMPTY),
      reduce((todos, pagina) => [...todos, ...pagina.categorias.map(c => ({id: c.id, nome: c.nome}))], [] as LookupItem[])
    );
  }

  add(nome: string): Observable<Categoria> {
    return this.http.post<CategoriaResponse>(this.url, { nome }).pipe(map(r => r.categoria));
  }

  update(id: number, nome: string): Observable<Categoria> {
    return this.http.put<CategoriaResponse>(`${this.url}/${id}`, { nome }).pipe(map(r => r.categoria));
  }

  enable(id: number): Observable<Categoria> {
    return this.http.patch<CategoriaResponse>(`${this.url}/${id}/ativar`, {}).pipe(map(r => r.categoria));
  }

  disable(id: number): Observable<Categoria> {
    return this.http.patch<CategoriaResponse>(`${this.url}/${id}/desativar`, {}).pipe(map(r => r.categoria));
  }
}
