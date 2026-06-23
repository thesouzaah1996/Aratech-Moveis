import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Produto, ProdutoForm } from '../models/produto.model';
import { environment } from '../../../environments/environment';

interface ApiResponse {
  status: number;
  message: string;
  produto?: Produto;
  produtos?: Produto[];
}

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private readonly api = `${environment.apiUrl}/almoxarifado/produto`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Produto[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/all`)
      .pipe(map(res => res.produtos ?? []));
  }

  add(form: ProdutoForm): Observable<Produto> {
    return this.http
      .post<ApiResponse>(`${this.api}/add`, form)
      .pipe(map(res => res.produto!));
  }

  update(id: number, form: ProdutoForm): Observable<Produto> {
    return this.http
      .put<ApiResponse>(`${this.api}/update/${id}`, form)
      .pipe(map(res => res.produto!));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/delete/${id}`);
  }
}
