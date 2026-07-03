import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { PecaEstoque, PecaEstoqueForm } from '../models/peca-estoque.model';
import { environment } from '../../../environments/environment';

interface ApiResponse {
  status: number;
  message: string;
  pecaEstoque?: PecaEstoque;
  pecasEstoque?: PecaEstoque[];
}

@Injectable({ providedIn: 'root' })
export class PecaEstoqueService {
  private readonly api = `${environment.apiUrl}/manutencao/peca-estoque`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<PecaEstoque[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/all`)
      .pipe(map(res => res.pecasEstoque ?? []));
  }

  add(form: PecaEstoqueForm): Observable<PecaEstoque> {
    return this.http
      .post<ApiResponse>(`${this.api}/add`, form)
      .pipe(map(res => res.pecaEstoque!));
  }

  update(id: number, form: PecaEstoqueForm): Observable<PecaEstoque> {
    return this.http
      .put<ApiResponse>(`${this.api}/update/${id}`, form)
      .pipe(map(res => res.pecaEstoque!));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/delete/${id}`);
  }
}
