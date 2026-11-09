import { Component,OnInit,OnDestroy,inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ActivatedRoute,RouterLink } from '@angular/router';
import { Subscription,Subject,debounceTime } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent } from '../breadcrumb/breadcrumb.component';
import { GaleriaFabricacaoComponent } from './galeria-fabricacao.component';
import { SerraIconComponent } from './serra-icon.component';
import { FabricacaoService } from '../../core/services/fabricacao.service';
import { SessaoState } from '../../core/services/sessao-state.service';
import { ItemGuardaRoupa,LoteCorte,LoteResumo,ModeloResumo,STATUS_CORTE } from '../../core/models/fabricacao.model';

@Component({selector:'app-lotes-fabricacao',standalone:true,imports:[FormsModule,DatePipe,RouterLink,NavbarComponent,BreadcrumbComponent,GaleriaFabricacaoComponent,SerraIconComponent],templateUrl:'./lotes-fabricacao.component.html'})
export class LotesFabricacaoComponent implements OnInit,OnDestroy {
  readonly api=inject(FabricacaoService);readonly sessao=inject(SessaoState);private route=inject(ActivatedRoute);
  private subs=new Subscription();private consulta?:Subscription;private pesquisa=new Subject<void>();private pesquisaModelos=new Subject<void>();private consultaModelos?:Subscription;
  corte=false;pagina=0;total=0;busca='';status='';lotes:LoteResumo[]=[];detalhe?:LoteCorte;peca?:ItemGuardaRoupa;
  carregando=false;ocupado=false;erro='';sucesso='';novo=false;modelos:ModeloResumo[]=[];buscaModelo='';carregandoModelos=false;
  modeloId=0;quantidade=1;previsao='';observacoes='';private chave=crypto.randomUUID();apenasCorte=true;readonly estados=STATUS_CORTE;
  get modulo():string{return this.corte?'CORTE_LOTES':'PCP_LOTES';}
  get contexto():'pcp'|'corte'{return this.corte?'corte':'pcp';}
  get breadcrumb(){return[{label:'Início',route:'/dashboard'},{label:this.corte?'Corte':'PCP',route:this.corte?'/corte':'/pcp'},{label:'Lotes'}];}
  ngOnInit():void{this.subs.add(this.pesquisa.pipe(debounceTime(300)).subscribe(()=>this.carregar()));this.subs.add(this.pesquisaModelos.pipe(debounceTime(300)).subscribe(()=>this.carregarModelos()));this.subs.add(this.route.data.subscribe(d=>{this.corte=d['corte']===true;this.detalhe=undefined;this.novo=false;this.pagina=0;this.busca='';this.status='';this.carregar();}));}
  carregar():void{this.consulta?.unsubscribe();this.carregando=true;this.erro='';this.consulta=this.api.lotes(this.pagina,this.busca,this.status,this.corte).subscribe({next:p=>{this.lotes=p.itens;this.total=p.total;this.carregando=false;},error:e=>{this.carregando=false;this.erro=this.mensagem(e);}});}
  pesquisar():void{this.pagina=0;this.pesquisa.next();}
  abrir(id:number):void{if(this.ocupado)return;this.erro='';this.carregando=true;this.subs.add(this.api.lote(id,this.corte).subscribe({next:l=>{this.detalhe=l;this.carregando=false;this.peca=undefined;this.apenasCorte=true;},error:e=>{this.erro=this.mensagem(e);this.carregando=false;}}));}
  voltar():void{if(this.ocupado)return;this.detalhe=undefined;this.peca=undefined;this.novo=false;this.carregar();}
  novoLote():void{this.novo=true;this.erro='';this.sucesso='';this.modeloId=0;this.quantidade=1;this.previsao='';this.observacoes='';this.chave=crypto.randomUUID();this.buscaModelo='';this.carregarModelos();}
  buscarModelo():void{this.pesquisaModelos.next();}
  carregarModelos():void{this.consultaModelos?.unsubscribe();this.carregandoModelos=true;this.consultaModelos=this.api.modelos(0,this.buscaModelo,true,true).subscribe({next:p=>{this.modelos=p.itens;this.carregandoModelos=false;},error:e=>{this.erro=this.mensagem(e);this.carregandoModelos=false;}});}
  criar():void{if(this.ocupado||!this.modeloId||!this.previsao||this.quantidade<1||!Number.isInteger(this.quantidade))return;this.ocupado=true;this.erro='';this.subs.add(this.api.criarLote({modeloId:Number(this.modeloId),quantidade:this.quantidade,previsao:this.previsao,observacoes:this.observacoes,chaveOperacao:this.chave}).subscribe({next:l=>{this.detalhe=l;this.novo=false;this.ocupado=false;this.sucesso='Lote planejado. Confira as peças e envie ao Corte quando estiver pronto.';},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
  executar(acao:string):void{if(!this.detalhe||this.ocupado)return;this.ocupado=true;this.erro='';this.subs.add(this.api.acao(this.detalhe,acao,this.corte).subscribe({next:l=>{this.detalhe=l;this.ocupado=false;this.sucesso=acao==='enviar'?'Lote enviado para a fila do Corte.':acao==='iniciar'?'Corte iniciado.':acao==='finalizar'?'Corte de todas as peças concluído.':'Planejamento cancelado.';},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
  get itens():ItemGuardaRoupa[]{return (this.detalhe?.modelo.ficha.itens??[]).filter(i=>!this.apenasCorte||i.cortar).slice().sort((a,b)=>`${a.material} ${a.espessuraMm??''} ${a.codigo}`.localeCompare(`${b.material} ${b.espessuraMm??''} ${b.codigo}`,'pt-BR'));}
  totalItem(item:ItemGuardaRoupa):number{return Math.round(item.quantidade*(this.detalhe?.quantidade??0)*1000)/1000;}
  get pecasCorte():number{return this.detalhe?.modelo.ficha.itens.filter(i=>i.cortar).reduce((s,i)=>s+this.totalItem(i),0)??0;}
  get areaCorte():number{return this.detalhe?.modelo.ficha.itens.filter(i=>i.cortar).reduce((s,i)=>s+(i.comprimentoMm??0)*(i.larguraMm??0)*this.totalItem(i)/1000000,0)??0;}
  get areaFormatada():string{return this.areaCorte.toLocaleString('pt-BR',{minimumFractionDigits:2,maximumFractionDigits:2});}
  fotos(item:string|null){return this.detalhe?.modelo.fotos.filter(f=>f.itemChave===item)??[];}
  atributos(i:ItemGuardaRoupa){return Object.entries(i.atributos??{});}
  get estadosFiltro(){return Object.entries(this.estados).filter(([e])=>!this.corte||['ENVIADO_CORTE','EM_CORTE','CORTADO'].includes(e));}
  classeStatus(status:string):string{return status==='CORTADO'?'bg-success-subtle text-success':status==='EM_CORTE'?'bg-warning-subtle text-warning-emphasis':status==='CANCELADO'?'bg-secondary-subtle text-secondary':'bg-danger-subtle text-danger';}
  csv():void{if(!this.detalhe)return;const l=this.detalhe;const linhas:unknown[][]=[['Lote','Modelo','Revisão','Código da peça','Peça','Grupo','Material','Acabamento','Cortar','Quantidade por móvel','Unidade','Quantidade do lote','Comprimento mm','Largura mm','Espessura mm','Veio','Bordas','Fita','Furações','Observações','Atributos'],...this.itens.map(i=>[`CT-${l.id}`,l.modelo.ficha.codigo,l.modeloVersao,i.codigo,i.nome,i.grupo,i.material,i.acabamento,i.cortar?'Sim':'Não',i.quantidade,i.unidade,this.totalItem(i),i.comprimentoMm??'',i.larguraMm??'',i.espessuraMm??'',i.veio,i.bordas.join(','),i.fitaBorda,i.furacao,i.observacoes,JSON.stringify(i.atributos)])];const conteudo=linhas.map(r=>r.map(v=>{let s=String(v??'');if(/^[=+\-@\t\r]/.test(s.trimStart()))s="'"+s;return '"'+s.replace(/"/g,'""')+'"';}).join(';')).join('\r\n');const url=URL.createObjectURL(new Blob(['\uFEFF'+conteudo],{type:'text/csv;charset=utf-8'}));const a=document.createElement('a');a.href=url;a.download=`corte-CT-${l.id}.csv`;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);}
  imprimir():void{window.print();}
  anterior():void{if(this.pagina>0){this.pagina--;this.carregar();}}proxima():void{if((this.pagina+1)*12<this.total){this.pagina++;this.carregar();}}
  mensagem(e:any):string{return(e.error?.mensagem??'Não foi possível concluir a operação.')+(e.error?.erros?' '+Object.values(e.error.erros).join(' '):'');}
  ngOnDestroy():void{this.consulta?.unsubscribe();this.consultaModelos?.unsubscribe();this.subs.unsubscribe();this.pesquisa.complete();this.pesquisaModelos.complete();}
}
