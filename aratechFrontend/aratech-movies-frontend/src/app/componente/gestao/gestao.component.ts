import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subscription, Subject, debounceTime, forkJoin, of } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../breadcrumb/breadcrumb.component';
import { SessaoState } from '../../core/services/sessao-state.service';
import { ActivatedRoute } from '@angular/router';
import { Acao, Campo, CONFIGS, ConfigGestao } from './gestao-config';

type Registro=Record<string,any>;
@Component({selector:'app-gestao',standalone:true,imports:[CommonModule,FormsModule,NavbarComponent,BreadcrumbComponent],templateUrl:'./gestao.component.html',styleUrl:'./gestao.component.scss'})
export class GestaoComponent implements OnInit,OnDestroy {
  @Input({required:true}) recurso='';
  readonly acesso=inject(SessaoState);
  private readonly http=inject(HttpClient);
  private readonly route=inject(ActivatedRoute);
  @ViewChild('dialogo') dialogo?:ElementRef<HTMLElement>;
  config!:ConfigGestao;
  breadcrumb:BreadcrumbItem[]=[];
  itens:Registro[]=[]; total=0; page=1; readonly pageSize=8; busca='';
  mes=new Date().toLocaleDateString('sv-SE',{timeZone:'America/Sao_Paulo'}).slice(0,7);
  loading=false; saving=false; erro=''; sucesso=''; erroDialogo=''; opcoesLoading=false;
  aberto=false; modo:'novo'|'editar'|'acao'='novo'; selecionado:Registro|null=null; acao:Acao|null=null;
  form:Registro={}; opcoes:Record<string,{id:string|number;nome:string}[]>={};
  private envio:{url:string;metodo:'POST'|'PUT';corpo:Registro}|null=null;
  private chave=''; private origemFoco:HTMLElement|null=null;
  private request?:Subscription;private mutation?:Subscription;private optionsRequest?:Subscription;
  private readonly pesquisa=new Subject<void>();private pesquisaSub?:Subscription;
  readonly labels:Record<string,string>={ATENDIDA:'Atendida',RECUSADA:'Recusada',DEVOLVIDA:'Devolvida',COMPRA:'Compra',VENDA:'Venda',MANUAL:'Manual',PLANEJADA:'Planejada',EM_PRODUCAO:'Em produção',FINALIZADA:'Finalizada',CANCELADA:'Cancelada',CANCELADO:'Cancelado',RASCUNHO:'Rascunho',CONFIRMADO:'Confirmado',PRONTO:'Pronto',EXPEDIDO:'Expedido',PENDENTE:'Pendente',VENCIDO:'Vencido',BAIXADO:'Baixado',ABERTA:'Aberta',ESCOLHIDA:'Escolhida',APROVADO:'Aprovado',RECUSADO:'Recusado',EM_CURSO:'Em curso',CONCLUIDO:'Concluído',EM_ANALISE:'Em análise',ENTRADA:'Entrada',SAIDA:'Saída',FERIAS:'Férias',LICENCA:'Licença',AFASTAMENTO:'Afastamento'};
  ngOnInit():void {
    this.recurso=this.recurso||this.route.snapshot.data['recurso'];
    this.config=CONFIGS[this.recurso];if(!this.config){this.erro='Tela não configurada.';return;}
    this.breadcrumb=[{label:'Início',route:'/dashboard'},{label:this.config.setor,route:this.config.rotaSetor},{label:this.config.titulo}];
    this.pesquisaSub=this.pesquisa.pipe(debounceTime(300)).subscribe(()=>this.carregar());this.carregar();
  }
  ngOnDestroy():void {this.request?.unsubscribe();this.mutation?.unsubscribe();this.optionsRequest?.unsubscribe();this.pesquisaSub?.unsubscribe();this.pesquisa.complete();}
  get totalPages():number{return Math.ceil(this.total/this.pageSize);}
  get campos():Campo[]{return this.modo==='acao'?(this.acao?.campos||[]):this.config.campos||[];}
  get emConfirmacao():boolean{return !!this.envio;}
  permitido(acao:string):boolean{return this.acesso.permite(this.config.permissao+'.'+acao);}
  onSearch():void{this.page=1;this.pesquisa.next();}
  setPage(p:number):void{if(!this.loading&&p>=1&&p<=this.totalPages){this.page=p;this.carregar();}}
  carregar():void{
    if(!this.config||this.config.somenteCadastro||!this.permitido('CONSULTAR'))return;
    this.request?.unsubscribe();this.loading=true;
    const params:Record<string,string|number>={pagina:this.page-1,tamanho:this.pageSize,busca:this.busca};if(this.config.mes)params['mes']=this.mes;
    this.request=this.http.get<any>('/aratech'+this.config.api,{params}).subscribe({
      next:p=>{
        this.loading=false;
        if(this.config.simples){const termo=this.busca.trim().toLocaleLowerCase();const filtrados=(p as Registro[]).filter(r=>Object.values(r).join(' ').toLocaleLowerCase().includes(termo));this.total=filtrados.length;this.itens=filtrados.slice((this.page-1)*this.pageSize,this.page*this.pageSize);}
        else{this.itens=p.itens;this.total=p.total;}
        const ultima=Math.max(1,this.totalPages);if(this.page>ultima){this.page=ultima;this.carregar();}
      },error:e=>{this.loading=false;this.erro=this.mensagem(e);}
    });
  }
  podeEditar(row:Registro):boolean{
    if(!this.config.editar||!this.permitido('ALTERAR'))return false;
    if(this.recurso==='pcpOrdens')return row['status']==='PLANEJADA';
    if(this.recurso==='vendas')return row['status']==='RASCUNHO';
    if(['pagar','receber','notas'].includes(this.recurso))return row['origem']==='MANUAL'&&['PENDENTE','VENCIDO'].includes(row['status']);
    return true;
  }
  acoes(row:Registro):Acao[]{return (this.config.acoes||[]).filter(a=>this.permitido(a.permissao)&&(!a.estados||a.estados.includes(row['status']))&&(!a.condicao||a.condicao==='manual'&&row['origem']==='MANUAL'||a.condicao==='semPedido'&&!row['pedidoId']||a.condicao==='naoExpedido'&&!row['expedidoEm']));}
  abrir(modo:'novo'|'editar'|'acao',row:Registro|null=null,acao:Acao|null=null):void{
    if(this.saving||this.envio)return;
    if(modo==='novo'&&!this.permitido('CADASTRAR')||modo==='editar'&&(!row||!this.podeEditar(row))||modo==='acao'&&(!row||!acao||!this.acoes(row).includes(acao)))return;
    this.origemFoco=document.activeElement as HTMLElement;this.modo=modo;this.selecionado=row?{...row}:null;this.acao=acao;this.erroDialogo='';this.form={};this.chave=crypto.randomUUID();
    for(const campo of this.campos)this.form[campo.nome]=modo==='editar'?(row?.[campo.nome]??null):campo.tipo==='number'?null:campo.tipo==='select'?null:'';
    if(this.campos.some(c=>c.nome==='data'))this.form['data']=new Date().toLocaleDateString('sv-SE',{timeZone:'America/Sao_Paulo'});
    this.aberto=true;this.carregarOpcoes();setTimeout(()=>this.dialogo?.nativeElement.querySelector<HTMLElement>('input,select,textarea,button')?.focus());
  }
  fechar():void{if(this.saving||this.envio)return;this.aberto=false;this.optionsRequest?.unsubscribe();this.origemFoco?.focus();}
  private carregarOpcoes():void{
    const fontes=this.campos.filter(c=>c.fonte);this.optionsRequest?.unsubscribe();this.opcoesLoading=true;
    this.optionsRequest=(fontes.length?forkJoin(fontes.map(c=>this.http.get<{id:number;nome:string}[]>('/aratech'+c.fonte))):of([])).subscribe({
      next:listas=>{fontes.forEach((c,i)=>this.opcoes[c.nome]=listas[i]);this.opcoesLoading=false;},
      error:e=>{this.opcoesLoading=false;this.erroDialogo=this.mensagem(e);}
    });
  }
  salvar():void{
    if(this.saving||this.opcoesLoading)return;
    if(!this.envio){
      if(!this.campos.every(c=>{
        const v=this.form[c.nome];if(c.obrigatorio&&(v===null||v===undefined||typeof v==='string'&&!v.trim()))return false;
        if(v===null||v===undefined||v==='')return true;
        if(c.tipo==='number')return Number.isFinite(Number(v))&&(c.min===undefined||Number(v)>=c.min)&&(c.max===undefined||Number(v)<=c.max)&&(c.step!==1||Number.isInteger(Number(v)));
        return !c.limite||String(v).length<=c.limite;
      })){this.erroDialogo='Confira os campos obrigatórios, datas e quantidades.';return;}
      let url='/aratech'+this.config.api;let metodo:'POST'|'PUT'='POST';
      if(this.modo==='editar'){url+='/'+encodeURIComponent(this.selecionado!['id']??this.selecionado!['chave']);metodo='PUT';}
      if(this.modo==='acao')url+='/'+this.selecionado!['id']+'/'+this.acao!.nome;
      const corpo:Registro={chaveOperacao:this.chave,versao:this.selecionado?.['versao']??0};
      this.campos.forEach(c=>{const v=this.form[c.nome];corpo[c.nome]=v===''||v===null||v===undefined?null:c.tipo==='number'?Number(v):v;});
      this.envio={url,metodo,corpo};
    }
    const envio=this.envio;this.saving=true;this.erroDialogo='';this.erro='';this.sucesso='';
    this.mutation=this.http.request(envio.metodo,envio.url,{body:envio.corpo}).subscribe({
      next:()=>{this.saving=false;this.envio=null;this.aberto=false;this.sucesso='Operação registrada com sucesso.';this.carregar();this.origemFoco?.focus();},
      error:e=>{
        this.saving=false;this.erroDialogo=this.mensagem(e);
        if(e.status>=400&&e.status<500){this.envio=null;this.chave=crypto.randomUUID();if(e.status===409){this.aberto=false;this.erro=this.erroDialogo;this.carregar();}}
      }
    });
  }
  rotulo(valor:any):string{return valor===null||valor===undefined||valor===''?'—':this.labels[String(valor)]||String(valor);}
  exportar():void{
    const escapar=(valor:any)=>'"'+String(valor??'').replace(/^\s*[=+\-@]/,"'$&").replace(/"/g,'""')+'"';
    const linhas=[this.config.colunas.map(c=>escapar(c.titulo)).join(';'),...this.itens.map(row=>this.config.colunas.map(c=>escapar(this.rotulo(row[c.nome]))).join(';'))];
    const url=URL.createObjectURL(new Blob(['\uFEFF'+linhas.join('\r\n')],{type:'text/csv;charset=utf-8'}));const link=document.createElement('a');link.href=url;link.download=this.recurso+'-pagina-'+this.page+'.csv';link.click();setTimeout(()=>URL.revokeObjectURL(url),1000);
  }
  tecla(event:KeyboardEvent):void{
    if(event.key==='Escape'){event.preventDefault();this.fechar();}
    if(event.key!=='Tab'||!this.dialogo)return;
    const itens=Array.from(this.dialogo.nativeElement.querySelectorAll<HTMLElement>('button:not(:disabled),input:not(:disabled),select:not(:disabled),textarea:not(:disabled)'));
    if(!itens.length)return;const primeiro=itens[0],ultimo=itens[itens.length-1];
    if(event.shiftKey&&document.activeElement===primeiro){event.preventDefault();ultimo.focus();}else if(!event.shiftKey&&document.activeElement===ultimo){event.preventDefault();primeiro.focus();}
  }
  private mensagem(e:any):string{return Object.values(e.error?.erros||{}).join(' ')||e.error?.mensagem||'Não foi possível confirmar a operação. Tente novamente.';}
}
