import { Component,OnInit,OnDestroy,ViewChild,ElementRef,inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subscription,Subject,debounceTime,interval } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent } from '../breadcrumb/breadcrumb.component';
import { EmbalagemService } from '../../core/services/embalagem.service';
import { SessaoState } from '../../core/services/sessao-state.service';
import { Esteira,PainelEsteira,TarefaEsteira,ESTADOS_EMBALAGEM,ComandoEmbalagem } from '../../core/models/embalagem.model';

@Component({selector:'app-esteiras',standalone:true,imports:[FormsModule,DatePipe,RouterLink,NavbarComponent,BreadcrumbComponent],templateUrl:'./esteiras.component.html'})
export class EsteirasComponent implements OnInit,OnDestroy {
  readonly api=inject(EmbalagemService);readonly sessao=inject(SessaoState);readonly estados=ESTADOS_EMBALAGEM;
  breadcrumb=[{label:'Início',route:'/dashboard'},{label:'Embalagem',route:'/embalagem'},{label:'Esteiras'}];
  private subs=new Subscription();private consulta?:Subscription;private pesquisa=new Subject<void>();
  paineis:PainelEsteira[]=[];busca='';ativas=false;carregando=false;ocupado=false;erro='';sucesso='';atualizado?:Date;form?:Esteira;
  @ViewChild('operacaoDialog') dialogo!:ElementRef<HTMLDialogElement>;
  operacao?:{painel:PainelEsteira;tarefa:TarefaEsteira;acao:string};motivo='';volumes:number|null=null;pecas=false;protecao=false;identificacao=false;erroModal='';
  ngOnInit():void{this.subs.add(this.pesquisa.pipe(debounceTime(300)).subscribe(()=>this.carregar()));this.subs.add(interval(15000).subscribe(()=>{if(!this.ocupado&&!this.form&&!this.operacao&&!this.carregando)this.carregar(false);}));this.carregar();}
  carregar(indicar=true):void{this.consulta?.unsubscribe();if(indicar){this.carregando=true;this.erro='';}this.consulta=this.api.painel(this.busca,this.ativas).subscribe({next:p=>{this.paineis=p;this.carregando=false;this.atualizado=new Date();},error:e=>{this.carregando=false;this.erro=this.mensagem(e);}});}
  pesquisar():void{this.pesquisa.next();}
  novo():void{this.form={id:0,codigo:'',nome:'',atividade:'',responsavel:'',capacidade:1,ativo:true,observacoes:'',versao:0};this.erro='';this.sucesso='';}
  editar(e:Esteira):void{this.form={...e};this.erro='';}
  salvar():void{if(this.ocupado||!this.form)return;if(!this.form.codigo.trim()||!this.form.nome.trim()||!this.form.atividade.trim()){this.erro='Preencha código, nome e atividade da esteira.';return;}this.ocupado=true;this.erro='';this.subs.add(this.api.salvarEsteira(this.form).subscribe({next:()=>{this.form=undefined;this.ocupado=false;this.sucesso='Esteira salva.';this.carregar();},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
  preparar(painel:PainelEsteira,tarefa:TarefaEsteira,acao:string):void{if(this.ocupado)return;if(acao==='pausar'||acao==='concluir'&&tarefa.ultima){this.operacao={painel,tarefa,acao};this.motivo='';this.volumes=null;this.pecas=false;this.protecao=false;this.identificacao=false;this.erroModal='';this.dialogo.nativeElement.showModal();}else this.executar(painel,tarefa,acao,{versao:tarefa.versao});}
  confirmar():void{if(!this.operacao||this.ocupado)return;const {painel,tarefa,acao}=this.operacao;if(acao==='pausar'&&this.motivo.trim().length<5){this.erroModal='Informe o motivo da pausa (5 a 500 caracteres).';return;}if(acao==='concluir'&&(!this.volumes||!Number.isInteger(this.volumes)||!this.pecas||!this.protecao||!this.identificacao)){this.erroModal='Informe os volumes e confirme as três conferências.';return;}const dto:ComandoEmbalagem={versao:tarefa.versao,motivo:this.motivo};if(acao==='concluir'){dto.volumes=this.volumes!;dto.pecasConferidas=this.pecas;dto.protecaoAplicada=this.protecao;dto.identificacaoConferida=this.identificacao;}this.executar(painel,tarefa,acao,dto);}
  executar(p:PainelEsteira,t:TarefaEsteira,acao:string,dto:ComandoEmbalagem):void{if(this.ocupado)return;this.ocupado=true;this.erro='';this.erroModal='';this.subs.add(this.api.operar(p.esteira.id,t.etapaId,acao,dto).subscribe({next:l=>{this.ocupado=false;if(this.operacao)this.fecharOperacao(false);this.sucesso=l.status==='EMBALADO'?`Lote EB-${l.id} embalado: ${l.volumes} volumes.`:acao==='iniciar'?'Operação iniciada.':acao==='pausar'?'Operação pausada; a posição permanece reservada.':acao==='retomar'?'Operação retomada.':'Etapa concluída. A próxima operação foi liberada.';this.carregar(false);},error:e=>{this.ocupado=false;if(this.operacao)this.erroModal=this.mensagem(e);else{this.erro=this.mensagem(e);if(e.status===409)this.carregar(false);}}}));}
  fecharOperacao(atualizar=true):void{if(this.ocupado)return;this.dialogo.nativeElement.close();this.operacao=undefined;if(atualizar)this.carregar(false);}
  cancelamentoDialog(event:Event):void{if(this.ocupado){event.preventDefault();return;}this.operacao=undefined;this.carregar(false);}
  tempo(segundos:number):string{const m=Math.floor(segundos/60);return m>=60?`${Math.floor(m/60)}h ${m%60}min`:`${m}min ${Math.floor(segundos%60)}s`;}
  get ocupadas():number{return this.paineis.reduce((s,p)=>s+p.ocupadas,0);}get emExecucao():number{return this.paineis.reduce((s,p)=>s+p.trabalhando.filter(t=>t.status==='EM_EXECUCAO').length,0);}get pausadas():number{return this.paineis.reduce((s,p)=>s+p.trabalhando.filter(t=>t.status==='PAUSADA').length,0);}get prontas():number{return this.paineis.reduce((s,p)=>s+p.fila.length,0);}
  mensagem(e:any):string{return(e.error?.mensagem??'Não foi possível concluir a operação.')+(e.error?.erros?' '+Object.values(e.error.erros).join(' '):'');}
  ngOnDestroy():void{this.consulta?.unsubscribe();this.subs.unsubscribe();this.pesquisa.complete();}
}
