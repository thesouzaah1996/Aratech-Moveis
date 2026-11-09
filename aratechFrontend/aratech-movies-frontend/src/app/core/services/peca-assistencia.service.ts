import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { PecaAssistencia, PecaAssistenciaForm, PecasAssistenciaPagina, PecaAssistenciaResponse,MovimentacoesAssistenciaPagina } from '../models/peca-assistencia.model';

@Injectable({ providedIn: 'root' })
export class PecaAssistenciaService {
  private readonly url = '/aratech/assistencia/estoque-pecas';
  constructor(private http: HttpClient) {}

  getAll(pagina = 0, tamanho = 8, busca = '', abaixoMinimo = false): Observable<PecasAssistenciaPagina> {
    let params = new HttpParams().set('pagina',pagina).set('tamanho',tamanho).set('abaixoMinimo',abaixoMinimo);
    if (busca.trim()) params = params.set('busca',busca.trim());
    return this.http.get<PecasAssistenciaPagina>(this.url,{params});
  }

  add(form: PecaAssistenciaForm): Observable<PecaAssistencia> {
    return this.http.post<PecaAssistenciaResponse>(this.url,{...this.dados(form),quantidade: form.quantidade})
      .pipe(map(r => r.peca));
  }

  update(id: number,form: PecaAssistenciaForm): Observable<PecaAssistencia> {
    // Saldo, ID, status e data de criacao nao pertencem a edicao cadastral.
    return this.http.put<PecaAssistenciaResponse>(`${this.url}/${id}`,this.dados(form)).pipe(map(r => r.peca));
  }

  delete(id: number): Observable<PecaAssistencia> {
    return this.http.delete<PecaAssistenciaResponse>(`${this.url}/${id}`).pipe(map(r => r.peca));
  }

  entrada(id: number,quantidade: number,chaveOperacao: string,motivo: string): Observable<PecaAssistencia> {
    return this.http.post<PecaAssistenciaResponse>(`${this.url}/${id}/entradas`,{quantidade,chaveOperacao,motivo}).pipe(map(r => r.peca));
  }

  saida(id: number,quantidade: number,chaveOperacao: string,motivo: string): Observable<PecaAssistencia> {
    return this.http.post<PecaAssistenciaResponse>(`${this.url}/${id}/saidas`,{quantidade,chaveOperacao,motivo}).pipe(map(r => r.peca));
  }

  historico(id:number,pagina=0,tamanho=8):Observable<MovimentacoesAssistenciaPagina>{
    return this.http.get<MovimentacoesAssistenciaPagina>(`${this.url}/${id}/movimentacoes`,{params:new HttpParams().set('pagina',pagina).set('tamanho',tamanho)});
  }

  private dados(form: PecaAssistenciaForm) {
    return { modelo: form.modelo.trim(), quantidadeMinima: form.quantidadeMinima, nome: form.nome.trim(), codigo: form.codigo.trim().toUpperCase(), unidade: form.unidade,
      localizacao: form.localizacao.trim(), descricao: form.descricao.trim() };
  }
}
