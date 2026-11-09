import { Injectable,inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FichaGuardaRoupa,ModeloGuardaRoupa,ModeloResumo,PaginaFabricacao,LoteCorte,LoteResumo } from '../models/fabricacao.model';

@Injectable({providedIn:'root'})
export class FabricacaoService {
  private http=inject(HttpClient);
  private catalogo='/aratech/administracao/guarda-roupas';
  modelos(pagina:number,busca:string,ativos=false,pcp=false){return this.http.get<PaginaFabricacao<ModeloResumo>>(pcp?'/aratech/pcp/lotes/modelos':this.catalogo,{params:{pagina,tamanho:12,busca,ativos}});}
  modelo(id:number){return this.http.get<ModeloGuardaRoupa>(`${this.catalogo}/${id}`);}
  salvar(ficha:FichaGuardaRoupa,id?:number){return id?this.http.put<ModeloGuardaRoupa>(`${this.catalogo}/${id}`,ficha):this.http.post<ModeloGuardaRoupa>(this.catalogo,ficha);}
  foto(modelo:ModeloGuardaRoupa,item:string|null,arquivo:File,legenda:string){const dados=new FormData();dados.append('arquivo',arquivo);dados.append('versao',String(modelo.versao));dados.append('legenda',legenda);if(item)dados.append('itemChave',item);return this.http.post<ModeloGuardaRoupa>(`${this.catalogo}/${modelo.id}/fotos`,dados);}
  retirarFoto(modelo:ModeloGuardaRoupa,foto:number){return this.http.delete<ModeloGuardaRoupa>(`${this.catalogo}/${modelo.id}/fotos/${foto}`,{params:{versao:modelo.versao}});}
  lotes(pagina:number,busca:string,status:string,corte:boolean){return this.http.get<PaginaFabricacao<LoteResumo>>(corte?'/aratech/corte/lotes':'/aratech/pcp/lotes/corte',{params:{pagina,tamanho:12,busca,status}});}
  lote(id:number,corte:boolean){return this.http.get<LoteCorte>(corte?`/aratech/corte/lotes/${id}`:`/aratech/pcp/lotes/corte/${id}`);}
  criarLote(dados:{modeloId:number;quantidade:number;previsao:string;observacoes:string;chaveOperacao:string}){return this.http.post<LoteCorte>('/aratech/pcp/lotes/corte',dados);}
  acao(lote:LoteCorte,acao:string,corte:boolean){return this.http.post<LoteCorte>(`${corte?'/aratech/corte/lotes':'/aratech/pcp/lotes/corte'}/${lote.id}/${acao}`,{versao:lote.versao});}
  urlFoto(modelo:number,foto:number,contexto:'administracao'|'pcp'|'corte'|'embalagem'='administracao'):string{return contexto==='administracao'?`${this.catalogo}/${modelo}/fotos/${foto}`:`/aratech/${contexto}/lotes/modelos/${modelo}/fotos/${foto}`;}
}
