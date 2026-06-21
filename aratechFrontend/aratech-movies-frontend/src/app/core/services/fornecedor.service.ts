import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Fornecedor, FornecedorForm } from '../models/fornecedor.model';

interface ApiResponse {
  status: number;
  message: string;
  fornecedores?: Fornecedor[];
  fornecedorDTO?: Fornecedor;
}

@Injectable({ providedIn: 'root' })
export class FornecedorService {
  private readonly api = 'http://localhost:8091/almoxarifado/fornecedor';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Fornecedor[]> {
    return this.http
      .get<ApiResponse>(`${this.api}/all`)
      .pipe(map(res => res.fornecedores ?? []));
  }

  add(form: FornecedorForm): Observable<Fornecedor> {
    return this.http
      .post<ApiResponse>(`${this.api}/add`, form)
      .pipe(map(res => res.fornecedorDTO!));
  }

  disable(id: number): Observable<void> {
    return this.http
      .post<void>(`${this.api}/disable/${id}`, {});
  }
}
