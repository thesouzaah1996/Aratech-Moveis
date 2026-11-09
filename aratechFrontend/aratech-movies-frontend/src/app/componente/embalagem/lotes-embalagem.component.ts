import { Component,OnInit,OnDestroy,inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ActivatedRoute,Router,RouterLink } from '@angular/router';
import { Subscription,Subject,debounceTime,forkJoin } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent } from '../breadcrumb/breadcrumb.component';
import { GaleriaFabricacaoComponent } from '../fabricacao/galeria-fabricacao.component';
import { EmbalagemService } from '../../core/services/embalagem.service';
import { SessaoState } from '../../core/services/sessao-state.service';
import { LoteResumo } from '../../core/models/fabricacao.model';
import { Esteira,EtapaEntrada,LoteEmbalagem,ResumoEmbalagem,NovoLoteEmbalagem,ESTADOS_EMBALAGEM } from '../../core/models/embalagem.model';
@Component({selector:'app-lotes-embalagem',standalone:true,imports:[FormsModule,DatePipe,RouterLink,NavbarComponent,BreadcrumbComponent,GaleriaFabricacaoComponent],templateUrl:'./lotes-embalagem.component.html'})
export class LotesEmbalagemComponent implements OnInit,OnDestroy {
  readonly api=inject(EmbalagemService);readonly sessao=inject(SessaoState);private route=inject(ActivatedRoute);private router=inject(Router);
  breadcrumb=[{label:'Início',route:'/dashboard'},{label:'Embalagem',route:'/embalagem'},{label:'Lotes'}];readonly estados=ESTADOS_EMBALAGEM;
  private subs=new Subscription();private consulta?:Subscription;private pesquisa=new Subject<void>();private pesquisaOrigem=new Subject<void>();private consultaOrigem?:Subscription;
  lotes:ResumoEmbalagem[]=[];total=0;pagina=0;busca='';status='';carregando=false;ocupado=false;erro='';sucesso='';detalhe?:LoteEmbalagem;novo=false;
  origens:LoteResumo[]=[];esteiras:Esteira[]=[];buscaOrigem='';origemId=0;previsao='';prioridade='NORMAL';observacoes='';pronto=false;etapas:EtapaEntrada[]=[];private chave=crypto.randomUUID();cancelando=false;motivoCancelamento='';
  ngOnInit():void{this.subs.add(this.pesquisa.pipe(debounceTime(300)).subscribe(()=>this.carregar()));this.subs.add(this.pesquisaOrigem.pipe(debounceTime(300)).subscribe(()=>this.carregarOrigens()));this.subs.add(this.route.paramMap.subscribe(p=>{const id=Number(p.get('id'));this.detalhe=undefined;this.novo=false;this.cancelando=false;id?this.abrir(id):this.carregar();}));}
  carregar():void{this.consulta?.unsubscribe();this.carregando=true;this.erro='';this.consulta=this.api.lotes(this.pagina,this.busca,this.status).subscribe({next:p=>{this.lotes=p.itens;this.total=p.total;this.carregando=false;},error:e=>{this.erro=this.mensagem(e);this.carregando=false;}});}
  pesquisar():void{this.pagina=0;this.pesquisa.next();}
  abrir(id:number):void{this.carregando=true;this.erro='';this.subs.add(this.api.lote(id).subscribe({next:l=>{this.detalhe=l;this.carregando=false;},error:e=>{this.erro=this.mensagem(e);this.carregando=false;}}));}
  voltar():void{if(this.ocupado)return;if(this.route.snapshot.paramMap.has('id')){void this.router.navigate(['/embalagem/lotes']);return;}this.detalhe=undefined;this.novo=false;this.cancelando=false;this.carregar();}
  novoLote():void{this.novo=true;this.erro='';this.sucesso='';this.origemId=0;this.buscaOrigem='';this.previsao='';this.prioridade='NORMAL';this.observacoes='';this.pronto=false;this.etapas=[{esteiraId:0,nome:'Conferência e proteção',instrucoes:''}];this.chave=crypto.randomUUID();this.carregando=true;this.subs.add(forkJoin({esteiras:this.api.esteiras(),origens:this.api.origens('')}).subscribe({next:r=>{this.esteiras=r.esteiras;this.origens=r.origens.itens;this.carregando=false;},error:e=>{this.erro=this.mensagem(e);this.carregando=false;}}));}
  buscarOrigem():void{this.pesquisaOrigem.next();}
  carregarOrigens():void{this.consultaOrigem?.unsubscribe();this.consultaOrigem=this.api.origens(this.buscaOrigem).subscribe({next:p=>this.origens=p.itens,error:e=>this.erro=this.mensagem(e)});}
  adicionarEtapa():void{if(this.etapas.length<12)this.etapas.push({esteiraId:0,nome:'',instrucoes:''});}
  sugerirRoteiro():void{this.etapas=[{esteiraId:0,nome:'Conferência e proteção',instrucoes:'Conferir as peças do lote e aplicar a proteção prevista.'},{esteiraId:0,nome:'Montagem e fechamento',instrucoes:'Acondicionar as peças e fechar a embalagem conforme o produto.'},{esteiraId:0,nome:'Identificação e conferência final',instrucoes:'Conferir identificação dos volumes, proteção e quantidade total.'}];}
  mover(indice:number,delta:number):void{const destino=indice+delta;if(destino<0||destino>=this.etapas.length)return;[this.etapas[indice],this.etapas[destino]]=[this.etapas[destino],this.etapas[indice]];}
  receber():void{if(this.ocupado)return;this.erro='';if(!this.origemId||!this.previsao||!this.pronto||!this.etapas.length||this.etapas.some(e=>!e.esteiraId||!e.nome.trim())){this.erro='Selecione a origem, a previsão e as esteiras do roteiro. Confirme que o lote está pronto para embalagem.';return;}const dto:NovoLoteEmbalagem={origemId:Number(this.origemId),previsao:this.previsao,prioridade:this.prioridade,observacoes:this.observacoes,prontoParaEmbalagem:this.pronto,chaveOperacao:this.chave,etapas:this.etapas.map(e=>({...e,esteiraId:Number(e.esteiraId)}))};this.ocupado=true;this.subs.add(this.api.receber(dto).subscribe({next:l=>{this.ocupado=false;this.novo=false;this.detalhe=l;this.sucesso='Lote recebido. A primeira etapa está disponível na esteira selecionada.';},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
  cancelar():void{if(this.ocupado||!this.detalhe)return;if(this.motivoCancelamento.trim().length<5){this.erro='Informe o motivo do cancelamento (5 a 500 caracteres).';return;}this.ocupado=true;this.subs.add(this.api.cancelar(this.detalhe,this.motivoCancelamento).subscribe({next:l=>{this.detalhe=l;this.ocupado=false;this.cancelando=false;this.sucesso='Recebimento cancelado. A origem pode ser recebida novamente com outro roteiro.';},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
  get concluidas():number{return this.detalhe?.etapas.filter(e=>e.status==='CONCLUIDA').length??0;}get situacoes(){return ['AGUARDANDO','EM_EMBALAGEM','PAUSADO','EMBALADO','CANCELADO'];}
  classeStatus(s:string):string{return s==='EMBALADO'||s==='CONCLUIDA'?'bg-success-subtle text-success':s==='PAUSADO'||s==='PAUSADA'?'bg-warning-subtle text-warning-emphasis':s==='CANCELADO'||s==='CANCELADA'?'bg-secondary-subtle text-secondary':'bg-danger-subtle text-danger';}
  fotos(item:string|null){return this.detalhe?.modelo.fotos.filter(f=>f.itemChave===item)??[];}
  tempo(segundos:number):string{return `${Math.floor(segundos/60)} min`;}
  quantidadeComponente(quantidade:number):number{return Math.round(quantidade*(this.detalhe?.quantidade??0)*1000)/1000;}
  nomeAcao(acao:string):string{return({RECEBIDO:'Lote recebido',INICIAR:'Etapa iniciada',PAUSAR:'Operação pausada',RETOMAR:'Operação retomada',CONCLUIR:'Etapa concluída',CANCELADO:'Lote cancelado'} as Record<string,string>)[acao]??acao;}
  anterior():void{if(this.pagina>0){this.pagina--;this.carregar();}}proxima():void{if((this.pagina+1)*12<this.total){this.pagina++;this.carregar();}}
  mensagem(e:any):string{return(e.error?.mensagem??'Não foi possível concluir a operação.')+(e.error?.erros?' '+Object.values(e.error.erros).join(' '):'');}
  ngOnDestroy():void{this.consulta?.unsubscribe();this.consultaOrigem?.unsubscribe();this.subs.unsubscribe();this.pesquisa.complete();this.pesquisaOrigem.complete();}
}
