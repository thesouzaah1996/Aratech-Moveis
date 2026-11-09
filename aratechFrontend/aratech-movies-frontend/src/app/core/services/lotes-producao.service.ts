import { Injectable,inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FichaGuardaRoupa, ItemGuardaRoupa, PaginaFabricacao } from '../models/fabricacao.model';
export interface SetorProducao {id:number;codigo:string;nome:string;recurso:string;caminho:string;natureza:string;exigeMedidas:boolean;}
export interface OperacaoFicha {setorId:number;nome:string;instrucoes:string;}
export interface RoteiroFicha {itemChave:string;consumoSetorId:number|null;produtoEstoqueId:number|null;operacoes:OperacaoFicha[];fatorEstoque?:number;}
export interface FichaVariante {modeloId:number;codigo:string;nome:string;ativo:boolean;versao:number;ficha:FichaGuardaRoupa;roteiro:RoteiroFicha[];}
export interface Variante {id:number;versao:number;dados:FichaVariante;}
export interface OpcaoVariante {id:number;modeloId:number;produto:string;codigo:string;nome:string;versao:number;}
export interface OrigemProducao {id:number;pedidoId:number|null;produto:string;quantidade:number;disponivel:number;status:string;}
export interface LinhaSetor {itemChave:string;natureza:string;consomeMaterial:boolean;codigo:string;descricao:string;unidade:string;porUnidade:number;total:number;produtoEstoqueId:number|null;quantidadeEstoque:number|null;especificacao:ItemGuardaRoupa|null;operacao:string;instrucoes:string;}
export interface GrupoSetor {setor:SetorProducao;linhas:LinhaSetor[];}
export interface OrdemSetor {id:number;loteId:number;codigoLote:string;setorId:number;setor:string;produto:string;variante:string;quantidadePlanejada:number;revisao:number;status:string;versao:number;linhas:LinhaSetor[];consumosConfirmados:string[];liberadoEm:string;}
export interface LoteProducao {id:number;codigo:string;produto:string;variante:string;quantidade:number;previsao:string;revisao:number;ordemOrigemId:number|null;observacoes:string;autor:string;status:string;previa:{setores:GrupoSetor[];pendencias:string[]};ordens:OrdemSetor[];}
export interface ResumoProducao {id:number;produto:string;variante:string;quantidade:number;previsao:string;revisao:number;status:string;pendentes:number;}
export const ESTADOS_PRODUCAO:Record<string,string>={PLANEJADO:'Planejado',BLOQUEADO:'Revisão necessária',LIBERACAO_PARCIAL:'Liberação parcial',EM_PRODUCAO:'Em produção',CONCLUIDO:'Concluído',LIBERADA:'Liberada',EM_ANDAMENTO:'Em andamento',CONCLUIDA:'Concluída'};
@Injectable({providedIn:'root'})
export class LotesProducaoService {
 private http=inject(HttpClient);private pcp='/aratech/pcp/lotes/producao';private catalogo='/aratech/administracao/guarda-roupas/variantes';
 setores(admin=false){return this.http.get<SetorProducao[]>(`${admin?this.catalogo:this.pcp}/setores`);}
 variantes(busca='',admin=false){return this.http.get<OpcaoVariante[]>(`${admin?this.catalogo:this.pcp}${admin?'':'/variantes'}`,{params:{busca}});}
 variante(id:number){return this.http.get<Variante>(`${this.catalogo}/${id}`);}
 materiais(){return this.http.get<{id:number;nome:string}[]>(`${this.catalogo}/materiais`);}
 salvar(d:FichaVariante,id?:number){return id?this.http.put<Variante>(`${this.catalogo}/${id}`,d):this.http.post<Variante>(this.catalogo,d);}
 origens(busca=''){return this.http.get<OrigemProducao[]>(`${this.pcp}/origens`,{params:{busca}});}
 lotes(pagina=0,busca=''){return this.http.get<PaginaFabricacao<ResumoProducao>>(this.pcp,{params:{pagina,busca}});}
 lote(id:number){return this.http.get<LoteProducao>(`${this.pcp}/${id}`);}
 revisar(l:LoteProducao){return this.http.post<LoteProducao>(`${this.pcp}/${l.id}/revisar`,{versao:l.revisao});}
 criar(d:{varianteId:number;ordemOrigemId:number|null;quantidade:number;previsao:string;observacoes:string;chaveOperacao:string}){return this.http.post<LoteProducao>(this.pcp,d);}
 liberar(l:LoteProducao,setores:number[]){return this.http.post<LoteProducao>(`${this.pcp}/${l.id}/liberar`,{revisao:l.revisao,setores});}
 fila(caminho:string,pagina=0,busca=''){return this.http.get<PaginaFabricacao<OrdemSetor>>(caminho,{params:{pagina,busca}});}
 acao(caminho:string,o:OrdemSetor,acao:string){return this.http.post<OrdemSetor>(`${caminho}/${o.id}/${acao}`,{versao:o.versao});}
 consumir(caminho:string,o:OrdemSetor,itemChave:string,chaveOperacao:string,motivo:string){return this.http.post<OrdemSetor>(`${caminho}/${o.id}/confirmar`,{itemChave,chaveOperacao,motivo});}
}
