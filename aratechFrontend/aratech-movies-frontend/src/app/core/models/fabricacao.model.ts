export interface FotoFabricacao { id:number; itemChave:string|null; legenda:string; }
export interface ItemGuardaRoupa {
  chave:string; codigo:string; nome:string; tipo:string; grupo:string; material:string; acabamento:string;
  unidade:string; quantidade:number; cortar:boolean; comprimentoMm:number|null; larguraMm:number|null;
  espessuraMm:number|null; veio:string; bordas:string[]; fitaBorda:string; furacao:string; observacoes:string;
  atributos:Record<string,string>;
}
export interface FichaGuardaRoupa {
  codigo:string; nome:string; descricao:string; larguraMm:number|null; alturaMm:number|null;
  profundidadeMm:number|null; ativo:boolean; versao:number; itens:ItemGuardaRoupa[];
}
export interface ModeloGuardaRoupa { id:number; versao:number; ficha:FichaGuardaRoupa; fotos:FotoFabricacao[]; }
export interface ModeloResumo { id:number; codigo:string; nome:string; ativo:boolean; versao:number; itens:number; capaId:number|null; }
export interface PaginaFabricacao<T> { itens:T[]; total:number; pagina:number; tamanho:number; }
export interface LoteResumo { id:number; codigoModelo:string; nomeModelo:string; quantidade:number; previsao:string; status:string; versao:number; }
export interface LoteCorte { id:number; modeloId:number; modeloVersao:number; quantidade:number; previsao:string; observacoes:string; status:string; versao:number; modelo:ModeloGuardaRoupa; autor:string; criadoEm:string; enviadoEm:string|null; iniciadoEm:string|null; concluidoEm:string|null; }
export const TIPOS_ITEM = [{id:'PAINEL',nome:'Painel / peça de chapa'},{id:'FERRAGEM',nome:'Ferragem'},{id:'ACESSORIO',nome:'Acessório'},{id:'ACABAMENTO',nome:'Acabamento'},{id:'EMBALAGEM',nome:'Embalagem'},{id:'OUTRO',nome:'Outro item'}];
export const STATUS_CORTE:Record<string,string>={PLANEJADO:'Planejado',ENVIADO_CORTE:'Recebido pelo Corte',EM_CORTE:'Em corte',CORTADO:'Corte concluído',CANCELADO:'Cancelado'};
