import { Component,OnInit,OnDestroy,inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription,Subject,debounceTime } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent } from '../breadcrumb/breadcrumb.component';
import { GaleriaFabricacaoComponent } from './galeria-fabricacao.component';
import { FabricacaoService } from '../../core/services/fabricacao.service';
import { SessaoState } from '../../core/services/sessao-state.service';
import { FichaGuardaRoupa,ItemGuardaRoupa,ModeloGuardaRoupa,ModeloResumo,TIPOS_ITEM } from '../../core/models/fabricacao.model';

type ItemEditor=ItemGuardaRoupa&{campos:{chave:string;valor:string}[]};
@Component({selector:'app-guarda-roupas',standalone:true,imports:[RouterLink,FormsModule,NavbarComponent,BreadcrumbComponent,GaleriaFabricacaoComponent],templateUrl:'./guarda-roupas.component.html'})
export class GuardaRoupasComponent implements OnInit,OnDestroy {
  readonly api=inject(FabricacaoService);readonly sessao=inject(SessaoState);
  private subs=new Subscription();private consulta?:Subscription;private buscaAlterada=new Subject<void>();
  breadcrumb=[{label:'Início',route:'/dashboard'},{label:'Administração',route:'/administracao'},{label:'Guarda-roupas'}];
  modelos:ModeloResumo[]=[];total=0;pagina=0;busca='';ativos=false;carregando=false;ocupado=false;erro='';sucesso='';
  selecionado?:ModeloGuardaRoupa;ficha?:FichaGuardaRoupa;editando=false;indice=0;filtroItens='';sugestao='';
  readonly tipos=TIPOS_ITEM;readonly unidades=['UN','PAR','KIT','M','M2','KG'];readonly bordas=['C1','C2','L1','L2'];
  readonly sugestoes=[
    {nome:'Lateral',tipo:'PAINEL',grupo:'Estrutura'},{nome:'Base',tipo:'PAINEL',grupo:'Estrutura'},{nome:'Tampo',tipo:'PAINEL',grupo:'Estrutura'},{nome:'Divisória',tipo:'PAINEL',grupo:'Estrutura'},{nome:'Prateleira',tipo:'PAINEL',grupo:'Interior'},{nome:'Porta',tipo:'PAINEL',grupo:'Portas'},{nome:'Fundo',tipo:'PAINEL',grupo:'Estrutura'},{nome:'Frente de gaveta',tipo:'PAINEL',grupo:'Gavetas'},{nome:'Lateral de gaveta',tipo:'PAINEL',grupo:'Gavetas'},{nome:'Contra-frente de gaveta',tipo:'PAINEL',grupo:'Gavetas'},{nome:'Fundo de gaveta',tipo:'PAINEL',grupo:'Gavetas'},{nome:'Travessa',tipo:'PAINEL',grupo:'Estrutura'},{nome:'Rodapé',tipo:'PAINEL',grupo:'Acabamento'},
    ...['Dobradiça','Calço de dobradiça','Corrediça','Puxador','Parafuso','Cavilha','Minifix','Suporte de prateleira','Cabideiro','Suporte de cabideiro','Trilho','Roldana','Amortecedor','Pé nivelador','Cantoneira','Tapa-furo','Kit de fixação à parede / antitombamento'].map(nome=>({nome,tipo:'FERRAGEM',grupo:'Ferragens'})),
    ...['Espelho','Perfil de alumínio','Iluminação LED','Passa-fio','Organizador interno'].map(nome=>({nome,tipo:'ACESSORIO',grupo:'Acessórios'})),{nome:'Proteção e embalagem',tipo:'EMBALAGEM',grupo:'Embalagem'}
  ];
  ngOnInit():void{this.subs.add(this.buscaAlterada.pipe(debounceTime(300)).subscribe(()=>this.carregar()));this.carregar();}
  pesquisar():void{this.pagina=0;this.buscaAlterada.next();}
  carregar():void{this.consulta?.unsubscribe();this.carregando=true;this.erro='';this.consulta=this.api.modelos(this.pagina,this.busca,this.ativos).subscribe({next:p=>{this.modelos=p.itens;this.total=p.total;this.carregando=false;},error:e=>{this.carregando=false;this.erro=this.mensagem(e);}});}
  abrir(id:number):void{if(this.ocupado)return;this.carregando=true;this.erro='';this.subs.add(this.api.modelo(id).subscribe({next:m=>{this.selecionado=m;this.editando=false;this.carregando=false;this.filtroItens='';},error:e=>{this.carregando=false;this.erro=this.mensagem(e);}}));}
  voltar():void{if(this.ocupado)return;this.selecionado=undefined;this.ficha=undefined;this.editando=false;this.erro='';this.carregar();}
  novo():void{this.selecionado=undefined;this.ficha={codigo:'',nome:'',descricao:'',larguraMm:null,alturaMm:null,profundidadeMm:null,ativo:true,versao:0,itens:[]};this.editando=true;this.indice=0;this.erro='';this.sucesso='';}
  editar():void{if(!this.selecionado)return;this.ficha=structuredClone(this.selecionado.ficha);this.ficha.versao=this.selecionado.versao;this.ficha.itens=this.ficha.itens.map(i=>({...i,campos:Object.entries(i.atributos??{}).map(([chave,valor])=>({chave,valor}))}));this.editando=true;this.indice=0;this.erro='';}
  cancelarEdicao():void{if(this.ocupado)return;this.editando=false;this.ficha=undefined;this.erro='';}
  get itensEditor():ItemEditor[]{return (this.ficha?.itens??[]) as ItemEditor[];}
  get itemAtual():ItemEditor|undefined{return this.itensEditor[this.indice];}
  adicionar(nome='',tipo='PAINEL',grupo='Estrutura'):void{if(!this.ficha||this.ficha.itens.length>=200)return;let n=this.ficha.itens.length+1;while(this.ficha.itens.some(i=>i.codigo===`ITEM-${String(n).padStart(3,'0')}`))n++;const item:ItemEditor={chave:crypto.randomUUID(),codigo:`ITEM-${String(n).padStart(3,'0')}`,nome,tipo,grupo,material:'',acabamento:'',unidade:'UN',quantidade:1,cortar:tipo==='PAINEL',comprimentoMm:null,larguraMm:null,espessuraMm:null,veio:'SEM',bordas:[],fitaBorda:'',furacao:'',observacoes:'',atributos:{},campos:[]};this.ficha.itens.push(item);this.indice=this.ficha.itens.length-1;}
  adicionarSugestao():void{const s=this.sugestoes.find(s=>s.nome===this.sugestao);if(s)this.adicionar(s.nome,s.tipo,s.grupo);this.sugestao='';}
  removerItem(indice:number):void{this.ficha?.itens.splice(indice,1);this.indice=Math.max(0,Math.min(this.indice,this.itensEditor.length-1));}
  tipoAlterado():void{if(this.itemAtual&&this.itemAtual.tipo!=='PAINEL')this.itemAtual.cortar=false;}
  alternarBorda(borda:string,marcada:boolean):void{const item=this.itemAtual;if(!item)return;item.bordas=marcada?[...item.bordas,borda]:item.bordas.filter(b=>b!==borda);}
  salvar():void{
    if(!this.ficha||this.ocupado)return;this.erro='';
    if(!this.ficha.codigo.trim()||!this.ficha.nome.trim()||!this.ficha.larguraMm||!this.ficha.alturaMm||!this.ficha.profundidadeMm||!this.ficha.itens.length){this.erro='Preencha código, nome, medidas externas e pelo menos um componente.';return;}
    for(let n=0;n<this.itensEditor.length;n++){const i=this.itensEditor[n];if(!i.nome.trim()||!i.codigo.trim()||!i.material.trim()||!i.grupo.trim()||!i.quantidade||i.quantidade<=0||(i.cortar&&(!i.comprimentoMm||!i.larguraMm||!i.espessuraMm))){this.indice=n;this.erro=`Complete os dados do componente ${i.codigo}.`;return;}const campos=i.campos??[];if(campos.some(c=>!c.chave.trim()||!c.valor.trim())||new Set(campos.map(c=>c.chave.trim())).size!==campos.length){this.indice=n;this.erro='Preencha os campos adicionais sem repetir o nome do atributo.';return;}}
    const payload:FichaGuardaRoupa={...this.ficha,itens:this.itensEditor.map(({campos,...i})=>({...i,atributos:Object.fromEntries((campos??[]).map(c=>[c.chave.trim(),c.valor.trim()]))}))};
    this.ocupado=true;this.subs.add(this.api.salvar(payload,this.selecionado?.id).subscribe({next:m=>{this.selecionado=m;this.ficha=undefined;this.editando=false;this.ocupado=false;this.sucesso='Ficha técnica salva. Você já pode adicionar fotos à capa e a cada componente.';},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));
  }
  fotos(item:string|null){return this.selecionado?.fotos.filter(f=>f.itemChave===item)??[];}
  enviarFoto(item:string|null,evento:{arquivo:File;legenda:string}):void{if(!this.selecionado||this.ocupado)return;this.ocupado=true;this.erro='';this.subs.add(this.api.foto(this.selecionado,item,evento.arquivo,evento.legenda).subscribe({next:m=>{this.selecionado=m;this.ocupado=false;this.sucesso='Foto adicionada à ficha.';},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
  retirarFoto(id:number):void{if(!this.selecionado||this.ocupado)return;this.ocupado=true;this.erro='';this.subs.add(this.api.retirarFoto(this.selecionado,id).subscribe({next:m=>{this.selecionado=m;this.ocupado=false;this.sucesso='Foto retirada do catálogo atual.';},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
  get componentesVisiveis(){const q=this.filtroItens.toLocaleLowerCase('pt-BR');return this.selecionado?.ficha.itens.filter(i=>`${i.codigo} ${i.nome} ${i.grupo} ${i.material}`.toLocaleLowerCase('pt-BR').includes(q))??[];}
  get paineis():number{return this.selecionado?.ficha.itens.filter(i=>i.cortar).reduce((s,i)=>s+i.quantidade,0)??0;}
  atributos(i:ItemGuardaRoupa){return Object.entries(i.atributos??{});}
  nomeTipo(tipo:string):string{return this.tipos.find(t=>t.id===tipo)?.nome??tipo;}
  paginaAnterior():void{if(this.pagina>0){this.pagina--;this.carregar();}}
  proximaPagina():void{if((this.pagina+1)*12<this.total){this.pagina++;this.carregar();}}
  mensagem(e:any):string{return (e.error?.mensagem??'Falha ao salvar a ficha.')+(e.error?.erros?' '+Object.values(e.error.erros).join(' '):'')||'Não foi possível concluir a operação.';}
  ngOnDestroy():void{this.consulta?.unsubscribe();this.subs.unsubscribe();this.buscaAlterada.complete();}
}
