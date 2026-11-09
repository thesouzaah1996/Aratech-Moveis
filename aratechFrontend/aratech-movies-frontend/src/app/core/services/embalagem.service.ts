import { Injectable,inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PaginaFabricacao,LoteResumo } from '../models/fabricacao.model';
import { Esteira,PainelEsteira,LoteEmbalagem,ResumoEmbalagem,NovoLoteEmbalagem,ComandoEmbalagem } from '../models/embalagem.model';
@Injectable({providedIn:'root'})
export class EmbalagemService {
  private http=inject(HttpClient);private base='/aratech/embalagem';
  painel(busca='',ativas=false){return this.http.get<PainelEsteira[]>(`${this.base}/esteiras`,{params:{busca,ativas}});}
  salvarEsteira(esteira:Esteira){const {id,...dados}=esteira;return id?this.http.put<Esteira>(`${this.base}/esteiras/${id}`,dados):this.http.post<Esteira>(`${this.base}/esteiras`,dados);}
  esteiras(){return this.http.get<Esteira[]>(`${this.base}/lotes/esteiras`);}
  origens(busca:string){return this.http.get<PaginaFabricacao<LoteResumo>>(`${this.base}/lotes/origens`,{params:{busca,tamanho:100}});}
  lotes(pagina:number,busca:string,status:string){return this.http.get<PaginaFabricacao<ResumoEmbalagem>>(`${this.base}/lotes`,{params:{pagina,tamanho:12,busca,status}});}
  lote(id:number){return this.http.get<LoteEmbalagem>(`${this.base}/lotes/${id}`);}
  receber(dto:NovoLoteEmbalagem){return this.http.post<LoteEmbalagem>(`${this.base}/lotes`,dto);}
  cancelar(lote:LoteEmbalagem,motivo:string){return this.http.post<LoteEmbalagem>(`${this.base}/lotes/${lote.id}/cancelar`,{versao:lote.versao,motivo});}
  operar(esteira:number,etapa:number,acao:string,dto:ComandoEmbalagem){return this.http.post<LoteEmbalagem>(`${this.base}/esteiras/${esteira}/etapas/${etapa}/${acao}`,dto);}
}
