import { PermissaoDirective } from '../../../core/permissao.directive';
import { Component, ElementRef, AfterViewInit, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Subscription, timer } from 'rxjs';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { criarModal, ModalDaTela } from '../../../core/ui/modal';
import { SessaoState } from '../../../core/services/sessao-state.service';
import { PedidoCompraService, PedidoCompra, StatusPedido } from '../../../core/services/pedido-compra.service';
export type { PedidoCompra, StatusPedido } from '../../../core/services/pedido-compra.service';
@Component({selector:'app-pedidos',standalone:true,imports: [PermissaoDirective,FormsModule,CurrencyPipe,DatePipe,NavbarComponent,BreadcrumbComponent],templateUrl:'./pedidos.component.html',styleUrl:'./pedidos.component.scss'})
export class PedidosCompraComponent implements OnInit,AfterViewInit,OnDestroy {
  @ViewChild('pedidoModal') pedidoModalEl!: ElementRef<HTMLElement>;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef<HTMLElement>;
  @ViewChild('reprovarModal') reprovarModalEl!: ElementRef<HTMLElement>;
  @ViewChild('detalheModal') detalheModalEl!: ElementRef<HTMLElement>;
  breadcrumb: BreadcrumbItem[]=[];
  origem?: 'almoxarifado'|'manutencao';
  pedidos:PedidoCompra[]=[]; searchTerm=''; filtroStatus:StatusPedido|'Todos'='Todos';page=1;pageSize=8;
  isEditing=false;submitted=false;successMessage='';errorMessage='';modalError='';reprovarErro='';motivo='';loading=false;pending=false;
  form:Partial<PedidoCompra>=this.emptyForm();pedidoParaExcluir:PedidoCompra|null=null;pedidoReprovar:PedidoCompra|null=null;detalhe:PedidoCompra|null=null;
  private pedidoModal?:ModalDaTela;private deleteModal?:ModalDaTela;private reprovarModal?:ModalDaTela;private detalheModal?:ModalDaTela;
  private request?:Subscription;private mutation?:Subscription;private poll?:Subscription;
  constructor(private service:PedidoCompraService,private route:ActivatedRoute,public sessao:SessaoState){}
  ngOnInit():void{
    this.origem=this.route.snapshot.data['origem'];
    const modulo=this.origem==='almoxarifado'?'Almoxarifado':this.origem==='manutencao'?'Manutenção':'Compras';
    this.breadcrumb=[{label:'Início',route:'/dashboard'},{label:modulo,route:'/'+(this.origem??'compras')},{label:this.origem?'Pedidos':'Pedidos de Compra'}];
    this.poll=timer(0,15000).subscribe(()=>{if(!this.pending&&!this.loading)this.carregar();});
  }
  ngAfterViewInit():void{this.pedidoModal=criarModal(this.pedidoModalEl.nativeElement);this.deleteModal=criarModal(this.deleteModalEl.nativeElement);this.reprovarModal=criarModal(this.reprovarModalEl.nativeElement);this.detalheModal=criarModal(this.detalheModalEl.nativeElement);}
  carregar():void{
    if(this.pending)return;this.request?.unsubscribe();this.loading=true;
    this.request=this.service.listar(this.origem).subscribe({next:ps=>{this.pedidos=ps;this.loading=false;this.errorMessage='';this.page=Math.max(1,Math.min(this.page,this.totalPages));},error:e=>{this.loading=false;this.errorMessage=this.erro(e);}});
  }
  get filtrados(): PedidoCompra[] {
    let lista = [...this.pedidos];
    if (this.filtroStatus !== 'Todos') lista = lista.filter(p => p.status === this.filtroStatus);
    const term = this.searchTerm.trim().toLowerCase();
    if (term) lista = lista.filter(p =>
      p.numero.toLowerCase().includes(term) ||
      (p.fornecedor ?? '').toLowerCase().includes(term) ||
      p.item.toLowerCase().includes(term)
    );
    return lista;
  }

  get paged(): PedidoCompra[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtrados.slice(start, start + this.pageSize);
  }

  get totalPages(): number { return Math.ceil(this.filtrados.length / this.pageSize); }
  get visiblePages(): number[] {
    const start = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    return Array.from({ length: Math.min(this.totalPages, start + 4) - start + 1 }, (_, i) => start + i);
  }
  get paginationStart(): number { return this.filtrados.length === 0 ? 0 : (this.page - 1) * this.pageSize + 1; }
  get paginationEnd(): number { return Math.min(this.page * this.pageSize, this.filtrados.length); }
  get valorTotal(): number { return (this.form.quantidade ?? 0) * (this.form.valorUnitario ?? 0); }

  setPage(p: number): void { if (p >= 1 && p <= this.totalPages) this.page = p; }
  setFiltro(s: string): void { this.filtroStatus = s as StatusPedido | 'Todos'; this.page = 1; }

  openAdd():void{if(this.pending)return;this.isEditing=false;this.submitted=false;this.modalError='';this.form=this.emptyForm();this.pedidoModal?.show();}
  openEdit(p:PedidoCompra):void{if(this.pending)return;this.isEditing=true;this.submitted=false;this.modalError='';this.form={...p};this.pedidoModal?.show();}
  openDelete(p:PedidoCompra):void{if(this.pending)return;this.pedidoParaExcluir=p;this.modalError='';this.deleteModal?.show();}
  ver(p:PedidoCompra):void{this.detalhe=p;this.detalheModal?.show();}
  fecharCadastro():void{if(!this.pending)this.pedidoModal?.hide();}
  fecharDelete():void{if(!this.pending)this.deleteModal?.hide();}
  fecharReprovacao():void{if(!this.pending)this.reprovarModal?.hide();}
  save():void{
    this.submitted=true;if(this.pending||!this.isFormValid())return;this.iniciar();this.modalError='';
    this.mutation=this.service.salvar(this.form,this.isEditing?this.form.id:undefined).subscribe({next:p=>{this.atualizar(p);this.pedidoModal?.hide();this.successMessage='Pedido salvo com sucesso.';},error:e=>{this.pending=false;this.modalError=this.erro(e);}});
  }
  enviarAprovacao(p:PedidoCompra):void{if(this.pending)return;this.iniciar();this.mutation=this.service.enviar(p).subscribe({next:r=>this.atualizar(r),error:e=>this.falha(e)});}
  confirmDelete():void{if(!this.pedidoParaExcluir||this.pending)return;this.iniciar();this.mutation=this.service.cancelar(this.pedidoParaExcluir).subscribe({next:r=>{this.atualizar(r);this.deleteModal?.hide();},error:e=>{this.pending=false;this.modalError=this.erro(e);}});}
  aprovar(p:PedidoCompra):void{this.decidir(p,'aprovar');}
  receber(p:PedidoCompra):void{this.decidir(p,'receber');}
  reprovar(p:PedidoCompra):void{if(this.pending)return;this.pedidoReprovar=p;this.motivo='';this.reprovarErro='';this.reprovarModal?.show();}
  confirmarReprovacao():void{
    if(!this.pedidoReprovar||this.pending)return;
    if(this.motivo.trim().length<5||this.motivo.length>1000){this.reprovarErro='Informe o motivo da reprovação (5 a 1000 caracteres).';return;}
    this.decidir(this.pedidoReprovar,'reprovar',this.motivo.trim());
  }
  private decidir(p:PedidoCompra,acao:'aprovar'|'reprovar'|'receber',motivo=''):void{
    if(this.pending)return;this.iniciar();
    this.mutation=this.service.decidir(p,acao,motivo).subscribe({next:r=>{this.atualizar(r);this.reprovarModal?.hide();this.successMessage=`Pedido ${r.numero}: ${r.status}.`;},error:e=>{this.pending=false;if(acao==='reprovar')this.reprovarErro=this.erro(e);else this.errorMessage=this.erro(e);}});
  }
  private iniciar():void{this.request?.unsubscribe();this.loading=false;this.pending=true;this.errorMessage='';this.successMessage='';}
  private atualizar(p:PedidoCompra):void{const existe=this.pedidos.some(x=>x.id===p.id);this.pedidos=existe?this.pedidos.map(x=>x.id===p.id?p:x):[p,...this.pedidos];this.pending=false;this.page=Math.max(1,Math.min(this.page,this.totalPages));}
  private falha(e:any):void{this.pending=false;this.errorMessage=this.erro(e);}
  badgeClass(s:string):string{return ({'Rascunho':'bg-secondary','Aguardando Aprovação':'bg-warning text-dark','Aprovado':'bg-info text-dark','Recebido':'bg-success','Reprovado':'bg-danger','Cancelado':'bg-secondary'} as Record<string,string>)[s]??'bg-secondary';}
  nomeOrigem(origem:string):string{return origem==='ALMOXARIFADO'?'Almoxarifado':origem==='MANUTENCAO'?'Manutenção':'Compras';}
  private isFormValid():boolean{return !!(this.form.fornecedor?.trim()&&this.form.item?.trim()&&this.form.quantidade&&this.form.quantidade>0&&this.form.valorUnitario&&this.form.valorUnitario>0&&this.form.dataPedido&&this.form.dataEntrega&&this.form.dataEntrega>=this.form.dataPedido&&this.form.dataVencimento&&this.form.dataVencimento>=this.form.dataPedido);}
  private emptyForm():Partial<PedidoCompra>{const d=new Date();return {fornecedor:'',item:'',quantidade:1,valorUnitario:null,dataPedido:`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`,dataEntrega:null};}
  private erro(e:any):string{return e.error?.mensagem??'Não foi possível concluir a operação. Tente novamente.';}
  ngOnDestroy():void{this.request?.unsubscribe();this.mutation?.unsubscribe();this.poll?.unsubscribe();this.pedidoModal?.dispose();this.deleteModal?.dispose();this.reprovarModal?.dispose();this.detalheModal?.dispose();}
}
