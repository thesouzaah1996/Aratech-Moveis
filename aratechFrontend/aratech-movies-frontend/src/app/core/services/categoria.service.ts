import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Categoria } from '../../componentes/almoxarifado/categorias/categorias.component';
import { LookupItem } from '../models/lookup.model';
import { environment } from '../../../environments/environment';

interface ApiResponse {
  status: number;
  message: string;
  categorias?: Categoria[];
  categoria?: Categoria;
  categoriaLookup?: LookupItem[];
}

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private readonly api = `${environment.apiUrl}/almoxarifado/categoria`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Categoria[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/all`)
      .pipe(map(res => res.categorias ?? []));
  }

  lookup(): Observable<LookupItem[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/lookup-categoria`)
      .pipe(map(res => res.categoriaLookup ?? []));
  }

  add(nome: string): Observable<Categoria> {
    return this.http
      .post<ApiResponse>(`${this.api}/add`, { nome })
      .pipe(map(res => res.categoria!));
  }

  update(id: number, nome: string): Observable<Categoria> {
    return this.http
      .put<ApiResponse>(`${this.api}/update/${id}`, { nome })
      .pipe(map(res => res.categoria!));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/delete/${id}`);
  }
}
