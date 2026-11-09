import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { PecaEstoque, PecaEstoqueForm, PecasEstoquePagina, PecaEstoqueResponse,MovimentacoesPecasPagina } from '../models/peca-estoque.model';

@Injectable({ providedIn: 'root' })
export class PecaEstoqueService {
  private readonly url = '/aratech/manutencao/estoque';
  constructor(private http: HttpClient) {}

  getAll(pagina = 0, tamanho = 8, busca = ''): Observable<PecasEstoquePagina> {
    let params = new HttpParams().set('pagina',pagina).set('tamanho',tamanho);
    if (busca.trim()) params = params.set('busca',busca.trim());
    return this.http.get<PecasEstoquePagina>(this.url,{params});
  }

  add(form: PecaEstoqueForm): Observable<PecaEstoque> {
    return this.http.post<PecaEstoqueResponse>(this.url,{...this.dados(form),quantidade: form.quantidade})
      .pipe(map(r => r.peca));
  }

  update(id: number,form: PecaEstoqueForm): Observable<PecaEstoque> {
    // Saldo, ID, status e data de criacao nao pertencem a edicao cadastral.
    return this.http.put<PecaEstoqueResponse>(`${this.url}/${id}`,this.dados(form)).pipe(map(r => r.peca));
  }

  delete(id: number): Observable<PecaEstoque> {
    return this.http.delete<PecaEstoqueResponse>(`${this.url}/${id}`).pipe(map(r => r.peca));
  }

  entrada(id: number,quantidade: number,chaveOperacao: string,motivo: string): Observable<PecaEstoque> {
    return this.http.post<PecaEstoqueResponse>(`${this.url}/${id}/entradas`,{quantidade,chaveOperacao,motivo}).pipe(map(r => r.peca));
  }

  saida(id: number,quantidade: number,chaveOperacao: string,motivo: string): Observable<PecaEstoque> {
    return this.http.post<PecaEstoqueResponse>(`${this.url}/${id}/saidas`,{quantidade,chaveOperacao,motivo}).pipe(map(r => r.peca));
  }

  historico(id:number,pagina=0,tamanho=8):Observable<MovimentacoesPecasPagina>{
    return this.http.get<MovimentacoesPecasPagina>(`${this.url}/${id}/movimentacoes`,{params:new HttpParams().set('pagina',pagina).set('tamanho',tamanho)});
  }

  minimo(id: number, quantidadeMinima: number | null): Observable<{ quantidadeMinima: number | null }> {
    return this.http.patch<{ quantidadeMinima: number | null }>(`${this.url}/${id}/minimo`, { quantidadeMinima });
  }

  private dados(form: PecaEstoqueForm) {
    return { nome: form.nome.trim(), codigo: form.codigo.trim().toUpperCase(), unidade: form.unidade,
      localizacao: form.localizacao.trim(), descricao: form.descricao.trim() };
  }
}
