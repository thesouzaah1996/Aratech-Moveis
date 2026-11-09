import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';
import { Fornecedor, FornecedorForm, FornecedorResponse, FornecedoresPagina } from '../models/fornecedor.model';
import { LookupItem } from '../models/lookup.model';

@Injectable({ providedIn: 'root' })
export class FornecedorService {
  private readonly url = '/aratech/almoxarifado/fornecedores';
  constructor(private http: HttpClient) {}

  getAll(pagina = 0, tamanho = 8, busca = '', ativo?: boolean): Observable<FornecedoresPagina> {
    let params = new HttpParams().set('pagina', pagina).set('tamanho', tamanho);
    if (busca.trim()) params = params.set('busca', busca.trim());
    if (ativo !== undefined) params = params.set('ativo', ativo);
    return this.http.get<FornecedoresPagina>(this.url, {params});
  }

  getById(id: number): Observable<Fornecedor> {
    return this.http.get<FornecedorResponse>(`${this.url}/${id}`).pipe(map(r => r.fornecedor));
  }

  lookup(): Observable<LookupItem[]> {
    return this.getAll(0, 100, '', true).pipe(
      expand(p => p.fornecedores.length > 0 && (p.pagina + 1) * p.tamanho < p.total
        ? this.getAll(p.pagina + 1, 100, '', true) : EMPTY),
      reduce((todos, p) => [...todos, ...p.fornecedores.map(f => ({id: f.id, nome: f.nome}))], [] as LookupItem[])
    );
  }

  add(form: FornecedorForm): Observable<Fornecedor> {
    return this.http.post<FornecedorResponse>(this.url, this.payload(form)).pipe(map(r => r.fornecedor));
  }

  update(id: number, form: FornecedorForm): Observable<Fornecedor> {
    return this.http.put<FornecedorResponse>(`${this.url}/${id}`, this.payload(form)).pipe(map(r => r.fornecedor));
  }

  enable(id: number): Observable<Fornecedor> {
    return this.http.patch<FornecedorResponse>(`${this.url}/${id}/ativar`, {}).pipe(map(r => r.fornecedor));
  }

  disable(id: number): Observable<Fornecedor> {
    return this.http.patch<FornecedorResponse>(`${this.url}/${id}/desativar`, {}).pipe(map(r => r.fornecedor));
  }

  private payload(form: FornecedorForm): FornecedorForm {
    // Lista explicita: nunca encaminhar ID ou status recebidos da tela.
    return {
      nome: form.nome.trim().replace(/ +/g, ' '), email: form.email.trim().toLowerCase(),
      telefone: form.telefone.replace(/\D/g, ''),
      representante: {
        nomeRepresentante: form.representante.nomeRepresentante.trim().replace(/ +/g, ' '),
        telefoneRepresentante: form.representante.telefoneRepresentante.replace(/\D/g, ''),
        emailRepresentante: form.representante.emailRepresentante.trim().toLowerCase()
      }
    };
  }
}
