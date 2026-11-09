import { Component,OnInit,OnDestroy,inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute,RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent } from '../breadcrumb/breadcrumb.component';
import { SessaoState } from '../../core/services/sessao-state.service';
import { LotesProducaoService,OrdemSetor,LinhaSetor,ESTADOS_PRODUCAO } from '../../core/services/lotes-producao.service';
@Component({selector:'app-ordens-setor',standalone:true,imports:[FormsModule,RouterLink,DecimalPipe,NavbarComponent,BreadcrumbComponent],templateUrl:'./ordens-setor.component.html'})
export class OrdensSetorComponent implements OnInit,OnDestroy {
 readonly api=inject(LotesProducaoService);readonly sessao=inject(SessaoState);private route=inject(ActivatedRoute);private subs=new Subscription();readonly estados=ESTADOS_PRODUCAO;
 caminho='';recurso='';nome='';volta='';pagina=0;total=0;busca='';erro='';ocupado=false;carregando=false;ordens:OrdemSetor[]=[];detalhe?:OrdemSetor;confirmacao?:LinhaSetor;motivo='';private chave=crypto.randomUUID();
 get breadcrumb(){return [{label:'Início',route:'/dashboard'},{label:this.nome,route:this.volta},{label:'Ordens de produção'}];}
 ngOnInit(){this.subs.add(this.route.data.subscribe(d=>{this.caminho=d['caminho'];this.recurso=d['recurso'];this.nome=d['nome'];this.volta=d['volta'];this.detalhe=undefined;this.confirmacao=undefined;this.pagina=0;this.carregar();}));}
 carregar(){this.carregando=true;this.subs.add(this.api.fila(this.caminho,this.pagina,this.busca).subscribe({next:p=>{this.ordens=p.itens;this.total=p.total;this.carregando=false;},error:e=>{this.erro=this.mensagem(e);this.carregando=false;}}));}
 executar(acao:string){if(!this.detalhe||this.ocupado)return;this.ocupado=true;this.erro='';this.subs.add(this.api.acao(this.caminho,this.detalhe,acao).subscribe({next:o=>{this.detalhe=o;this.ocupado=false;},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
 preparar(l:LinhaSetor){this.confirmacao=l;this.motivo='';this.chave=crypto.randomUUID();}
 consumir(){if(!this.detalhe||!this.confirmacao||this.ocupado||this.motivo.trim().length<5)return;this.ocupado=true;this.erro='';this.subs.add(this.api.consumir(this.caminho,this.detalhe,this.confirmacao.itemChave,this.chave,this.motivo).subscribe({next:o=>{this.detalhe=o;this.confirmacao=undefined;this.ocupado=false;},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
 mensagem(e:any){return e.error?.mensagem??'Não foi possível concluir a operação.';}
 ngOnDestroy(){this.subs.unsubscribe();}
}
