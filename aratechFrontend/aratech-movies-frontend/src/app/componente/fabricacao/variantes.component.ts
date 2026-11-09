import { Component,OnInit,OnDestroy,inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription,forkJoin } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent } from '../breadcrumb/breadcrumb.component';
import { FabricacaoService } from '../../core/services/fabricacao.service';
import { SessaoState } from '../../core/services/sessao-state.service';
import { ModeloResumo } from '../../core/models/fabricacao.model';
import { LotesProducaoService,FichaVariante,OpcaoVariante,SetorProducao } from '../../core/services/lotes-producao.service';
@Component({selector:'app-variantes',standalone:true,imports:[FormsModule,RouterLink,NavbarComponent,BreadcrumbComponent],templateUrl:'./variantes.component.html'})
export class VariantesComponent implements OnInit,OnDestroy {
 readonly api=inject(LotesProducaoService);readonly catalogo=inject(FabricacaoService);readonly sessao=inject(SessaoState);private subs=new Subscription();
 readonly breadcrumb=[{label:'Início',route:'/dashboard'},{label:'Administração',route:'/administracao'},{label:'Guarda-roupas',route:'/administracao/guarda-roupas'},{label:'Variantes e roteiros'}];
 variantes:OpcaoVariante[]=[];modelos:ModeloResumo[]=[];setores:SetorProducao[]=[];materiais:{id:number;nome:string}[]=[];busca='';buscaModelo='';modeloId=0;erro='';sucesso='';ocupado=false;id?:number;dados?:FichaVariante;
 ngOnInit(){this.carregar();this.subs.add(forkJoin({setores:this.api.setores(true),materiais:this.api.materiais()}).subscribe({next:r=>{this.setores=r.setores;this.materiais=r.materiais;},error:e=>this.erro=this.mensagem(e)}));}
 carregar(){this.subs.add(this.api.variantes(this.busca,true).subscribe({next:v=>this.variantes=v,error:e=>this.erro=this.mensagem(e)}));}
 buscarModelos(){this.subs.add(this.catalogo.modelos(0,this.buscaModelo,true).subscribe({next:p=>this.modelos=p.itens,error:e=>this.erro=this.mensagem(e)}));}
 nova(){this.id=undefined;this.dados=undefined;this.modeloId=0;this.erro='';this.sucesso='';this.buscarModelos();}
 copiar(){if(!this.modeloId||this.ocupado)return;this.ocupado=true;this.subs.add(this.catalogo.modelo(Number(this.modeloId)).subscribe({next:m=>{this.dados={modeloId:m.id,codigo:'',nome:'',ativo:true,versao:0,ficha:structuredClone(m.ficha),roteiro:m.ficha.itens.map(i=>({itemChave:i.chave,consumoSetorId:null,produtoEstoqueId:null,fatorEstoque:1,operacoes:[]}))};this.id=undefined;this.ocupado=false;},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
 abrir(id:number){if(this.ocupado)return;this.ocupado=true;this.erro='';this.subs.add(this.api.variante(id).subscribe({next:v=>{this.id=v.id;this.dados={...v.dados,versao:v.versao};for(const i of this.dados.ficha.itens)if(!this.dados.roteiro.some(r=>r.itemChave===i.chave))this.dados.roteiro.push({itemChave:i.chave,consumoSetorId:null,produtoEstoqueId:null,fatorEstoque:1,operacoes:[]});this.ocupado=false;},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
 salvar(){if(!this.dados||this.ocupado)return;this.ocupado=true;this.erro='';this.subs.add(this.api.salvar(this.dados,this.id).subscribe({next:v=>{this.id=v.id;this.dados={...v.dados,versao:v.versao};this.ocupado=false;this.sucesso='Variante salva. Lotes já planejados continuam usando sua revisão original.';this.carregar();},error:e=>{this.ocupado=false;this.erro=this.mensagem(e);}}));}
 roteiro(chave:string){return this.dados!.roteiro.find(r=>r.itemChave===chave)!;}
 exigeMedidas(chave:string){const r=this.roteiro(chave);return this.setores.some(s=>s.exigeMedidas&&(s.id===r.consumoSetorId||r.operacoes.some(op=>op.setorId===s.id)));}
 adicionar(chave:string){this.roteiro(chave).operacoes.push({setorId:0,nome:'',instrucoes:''});}
 get operacionais(){return this.setores.filter(s=>s.natureza==='OPERACAO');}
 mensagem(e:any){return e.error?.mensagem??'Não foi possível salvar a variante. Confira os campos obrigatórios.';}
 ngOnDestroy(){this.subs.unsubscribe();}
}
