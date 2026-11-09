import { ModeloGuardaRoupa } from './fabricacao.model';
export interface Esteira {id:number;codigo:string;nome:string;atividade:string;responsavel:string;capacidade:number;ativo:boolean;observacoes:string;versao:number;}
export interface EtapaEntrada {esteiraId:number;nome:string;instrucoes:string;}
export interface ComandoEmbalagem {versao:number;motivo?:string;volumes?:number;pecasConferidas?:boolean;protecaoAplicada?:boolean;identificacaoConferida?:boolean;}
export interface EtapaEmbalagem {id:number;loteId:number;sequencia:number;esteiraId:number;esteira:string;nome:string;instrucoes:string;status:string;operador:string;segundosAtivos:number;iniciadoEm:string|null;concluidoEm:string|null;motivoPausa:string;liberada:boolean;}
export interface EventoEmbalagem {id:number;etapaId:number|null;acao:string;detalhe:string;autor:string;criadoEm:string;}
export interface LoteEmbalagem {id:number;origemId:number;status:string;versao:number;quantidade:number;previsao:string;prioridade:string;observacoes:string;volumes:number|null;criadoEm:string;concluidoEm:string|null;modelo:ModeloGuardaRoupa;etapas:EtapaEmbalagem[];historico:EventoEmbalagem[];}
export interface ResumoEmbalagem {id:number;origemId:number;modelo:string;quantidade:number;previsao:string;prioridade:string;status:string;versao:number;concluidas:number;totalEtapas:number;etapaAtual:string|null;esteiraAtual:string|null;volumes:number|null;}
export interface TarefaEsteira {etapaId:number;loteId:number;versao:number;sequencia:number;modelo:string;quantidade:number;previsao:string;prioridade:string;nome:string;instrucoes:string;status:string;operador:string;motivoPausa:string;segundosAtivos:number;ultima:boolean;}
export interface PainelEsteira {esteira:Esteira;ocupadas:number;trabalhando:TarefaEsteira[];fila:TarefaEsteira[];futuras:number;}
export interface NovoLoteEmbalagem {origemId:number;previsao:string;prioridade:string;observacoes:string;prontoParaEmbalagem:boolean;chaveOperacao:string;etapas:EtapaEntrada[];}
export const ESTADOS_EMBALAGEM:Record<string,string>={AGUARDANDO:'Aguardando',EM_EMBALAGEM:'Em embalagem',PAUSADO:'Pausado',EMBALADO:'Embalado',CANCELADO:'Cancelado',EM_EXECUCAO:'Em execução',PAUSADA:'Pausada',CONCLUIDA:'Concluída',CANCELADA:'Cancelada'};
