import { Component,OnInit,OnDestroy,inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe,DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subscription,forkJoin } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent } from '../breadcrumb/breadcrumb.component';
import { SessaoState } from '../../core/services/sessao-state.service';
import { LotesProducaoService,LoteProducao,ResumoProducao,OpcaoVariante,OrigemProducao,ESTADOS_PRODUCAO } from '../../core/services/lotes-producao.service';
@Component({selector:'app-lotes-producao',standalone:true,imports:[FormsModule,RouterLink,DatePipe,DecimalPipe,NavbarComponent,BreadcrumbComponent],templateUrl:'./lotes-producao.component.html'})
export class LotesProducaoComponent implements OnInit,OnDestroy {
 readonly api=inject(LotesProducaoService);readonly sessao=inject(SessaoState);private subs=new Subscription();readonly estados=ESTADOS_PRODUCAO;
 readonly breadcrumb=[{label:'Início',route:'/dashboard'},{label:'PCP',route:'/pcp'},{label:'Lotes de produção'}];
 pagina=0;total=0;busca='';erro='';sucesso='';ocupado=false;carregando=false;novo=false;lotes:ResumoProducao[]=[];detalhe?:LoteProducao;
 variantes:OpcaoVariante[]=[];origens:OrigemProducao[]=[];buscaVariante='';buscaOrigem='';varianteId=0;ordemOrigemId:number|null=null;quantidade=1;previsao='';observacoes='';selecionados:number[]=[];private chave=crypto.randomUUID();
 ngOnInit(){this.carregar();}
 carregar(){if(this.ocupado)return;this.carregando=true;this.subs.add(this.api.lotes(this.pagina,this.busca).subscribe({next:p=>{this.lotes=p.itens;this.total=p.total;this.carregando=false;},error:e=>{this.erro=this.mensagem(e);this.carregando=false;}}));}
 pesquisar(){this.pagina=0;this.carregar();}
 abrir(id:number){if(this.ocupado)return;this.carregando=true;this.erro='';this.subs.add(this.api.lote(id).subscribe({next:l=>{this.detalhe=l;this.selecionados=[];this.novo=false;this.carregando=false;},error:e=>{this.erro=this.mensagem(e);this.carregando=false;}}));}
 novoLote(){this.novo=true;this.detalhe=undefined;this.erro='';this.sucesso='';this.chave=crypto.randomUUID();this.varianteId=0;this.ordemOrigemId=null;this.quantidade=1;this.previsao='';this.observacoes='';this.opcoes();}
 opcoes(){this.subs.add(forkJoin({variantes:this.api.variantes(this.buscaVariante),origens:this.api.origens(this.buscaOrigem)}).subscribe({next:r=>{this.variantes=r.variantes;this.origens=r.origens;},error:e=>this.erro=this.mensagem(e)}));}
 origemMudou(){const o=this.origens.find(v=>v.id===Number(this.ordemOrigemId));if(o)this.quantidade=Math.min(o.disponivel,10000);}
 criar(){if(this.ocupado||!this.varianteId||!this.previsao||!Number.isInteger(this.quantidade)||this.quantidade<1||this.quantidade>10000)return;this.ocupado=true;this.erro='';this.subs.add(this.api.criar({varianteId:Number(this.varianteId),ordemOrigemId:this.ordemOrigemId?Number(this.ordemOrigemId):null,quantidade:this.quantidade,previsao:this.previsao,observacoes:this.observacoes,chaveOperacao:this.chave}).subscribe({next:l=>{this.detalhe=l;this.selecionados=[];this.novo=false;this.ocupado=false;this.sucesso='Lote planejado. Confira a prévia e selecione os setores; a primeira liberação congela a revisão.';},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
 marcar(id:number,marcado:boolean){this.selecionados=marcado?[...new Set([...this.selecionados,id])]:this.selecionados.filter(v=>v!==id);}
 revisar(){if(this.ocupado||!this.detalhe||this.detalhe.ordens.length)return;this.ocupado=true;this.erro='';this.subs.add(this.api.revisar(this.detalhe).subscribe({next:l=>{this.detalhe=l;this.selecionados=[];this.ocupado=false;this.sucesso='Prévia atualizada. A primeira liberação congelará esta revisão para todos os setores.';},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
 ordem(setor:number){return this.detalhe?.ordens.find(o=>o.setorId===setor);}
 liberar(){if(this.ocupado||!this.detalhe||!this.selecionados.length||this.detalhe.previa.pendencias.length)return;this.ocupado=true;this.erro='';this.subs.add(this.api.liberar(this.detalhe,this.selecionados).subscribe({next:l=>{this.detalhe=l;this.selecionados=[];this.ocupado=false;this.sucesso='Ordens liberadas somente para os setores selecionados.';},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
 voltar(){if(this.ocupado)return;this.novo=false;this.detalhe=undefined;this.sucesso='';this.carregar();}
 mensagem(e:any){return e.error?.mensagem??'Não foi possível concluir a operação.';}
 ngOnDestroy(){this.subs.unsubscribe();}
}
