import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Produto, ProdutoForm, ProdutosPagina, ProdutoResponse,MovimentacoesPagina } from '../models/produto.model';

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private readonly url = '/aratech/almoxarifado/produtos';
  constructor(private http: HttpClient) {}
  opcoes() { return this.http.get<{categorias:{id:number;nome:string}[];fornecedores:{id:number;nome:string}[]}>(`${this.url}/opcoes`); }

  getAll(pagina = 0, tamanho = 8, busca = ''): Observable<ProdutosPagina> {
    let params = new HttpParams().set('pagina',pagina).set('tamanho',tamanho);
    if (busca.trim()) params = params.set('busca',busca.trim());
    return this.http.get<ProdutosPagina>(this.url,{params});
  }

  add(form: ProdutoForm): Observable<Produto> {
    return this.http.post<ProdutoResponse>(this.url,{...this.dados(form),quantidade: form.quantidade})
      .pipe(map(r => r.produto));
  }

  update(id: number,form: ProdutoForm): Observable<Produto> {
    // Saldo, ID, status e data de criacao nao pertencem a edicao cadastral.
    return this.http.put<ProdutoResponse>(`${this.url}/${id}`,this.dados(form)).pipe(map(r => r.produto));
  }

  delete(id: number): Observable<Produto> {
    return this.http.delete<ProdutoResponse>(`${this.url}/${id}`).pipe(map(r => r.produto));
  }

  entrada(id: number,quantidade: number,chaveOperacao: string,motivo: string): Observable<Produto> {
    return this.http.post<ProdutoResponse>(`${this.url}/${id}/entradas`,{quantidade,chaveOperacao,motivo}).pipe(map(r => r.produto));
  }

  saida(id: number,quantidade: number,chaveOperacao: string,motivo: string): Observable<Produto> {
    return this.http.post<ProdutoResponse>(`${this.url}/${id}/saidas`,{quantidade,chaveOperacao,motivo}).pipe(map(r => r.produto));
  }

  historico(id:number,pagina=0,tamanho=8):Observable<MovimentacoesPagina>{
    return this.http.get<MovimentacoesPagina>(`${this.url}/${id}/movimentacoes`,{params:new HttpParams().set('pagina',pagina).set('tamanho',tamanho)});
  }

  minimo(id: number, quantidadeMinima: number | null): Observable<{ quantidadeMinima: number | null }> {
    return this.http.patch<{ quantidadeMinima: number | null }>(`${this.url}/${id}/minimo`, { quantidadeMinima });
  }

  private dados(form: ProdutoForm) {
    return {
      nome: form.nome.trim(),sku: form.sku.trim().toUpperCase(),categoriaID: form.categoriaID,
      fornecedorID: form.fornecedorID,localArmazenamento: form.localArmazenamento.trim(),
      descricao: form.descricao.trim(),vencimentoProduto: form.vencimentoProduto || null
    };
  }
}
