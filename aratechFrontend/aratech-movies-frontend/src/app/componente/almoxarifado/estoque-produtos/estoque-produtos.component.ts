import { PermissaoDirective } from '../../../core/permissao.directive';
import { SessaoState } from '../../../core/services/sessao-state.service';
import { DatePipe } from '@angular/common';
import { Component, ElementRef, OnInit, OnDestroy, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription, debounceTime, forkJoin } from 'rxjs';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { CategoriaService } from '../../../core/services/categoria.service';
import { FornecedorService } from '../../../core/services/fornecedor.service';
import { ProdutoService } from '../../../core/services/produto.service';
import { Produto, ProdutoForm, MovimentacaoEstoque } from '../../../core/models/produto.model';
import { LookupItem } from '../../../core/models/lookup.model';

declare const bootstrap: any;

@Component({
  selector: 'app-estoque-produtos',standalone: true,
  imports: [PermissaoDirective,DatePipe,FormsModule,NavbarComponent,BreadcrumbComponent],
  templateUrl: './estoque-produtos.component.html',styleUrl: './estoque-produtos.component.scss'
})
export class EstoqueProdutosComponent implements OnInit,AfterViewInit,OnDestroy {
  @ViewChild('productModal') productModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;
  @ViewChild('entradaModal') entradaModalEl!: ElementRef;
  @ViewChild('historicoModal') historicoModalEl!: ElementRef;
  @ViewChild('saidaModal') saidaModalEl!: ElementRef;
  breadcrumb: BreadcrumbItem[] = [
    {label: 'Início',route: '/dashboard'},{label: 'Almoxarifado',route: '/almoxarifado'},{label: 'Estoque de Produtos'}
  ];
  produtos: Produto[] = [];
  categorias: LookupItem[] = [];
  fornecedores: LookupItem[] = [];
  loading = false;
  lookupLoading = false;
  lookupError = '';
  searchQuery = '';
  successMessage = '';
  errorMessage = '';
  modalError = '';
  deleteError = '';
  isEditing = false;
  submitted = false;
  saving = false;
  movimentando = false;
  arquivando = false;
  page = 1;
  pageSize = 8;
  total = 0;
  form: ProdutoForm = this.emptyForm();
  produtoParaExcluir: Produto | null = null;
  produtoMovimentacao: Produto | null = null;
  quantidadeMovimentacao: number | null = null;
  quantidadeEnviada: number | null = null;
  movimentacaoError = '';
  motivoMovimentacao = '';
  motivoEnviado: string | null = null;
  historico: MovimentacaoEstoque[] = [];
  historicoProduto: Produto | null = null;
  historicoPage = 1;
  historicoTotal = 0;
  historicoLoading = false;
  historicoError = '';
  private historicoModal?: any;
  private historicoRequest?: Subscription;
  private chaveMovimentacao = '';
  private productModal?: any;
  private deleteModal?: any;
  private entradaModal?: any;
  private saidaModal?: any;
  private request?: Subscription;
  private mutation?: Subscription;
  private lookupRequest?: Subscription;
  private search = new Subject<void>();
  private searchSubscription?: Subscription;

  constructor(private categoriaService: CategoriaService,private fornecedorService: FornecedorService,
              private produtoService: ProdutoService, public sessao: SessaoState = new SessaoState()) {}

  minimoId: number | null = null;
  minimoValor: number | null = null;
  minimoSalvando = false;
  private minimoRequest?: Subscription;
  editarMinimo(item: Produto): void {
    if (this.minimoSalvando || !this.sessao.permite('ALMOXARIFADO_ESTOQUE.ALTERAR')) return;
    this.minimoId = item.id; this.minimoValor = item.quantidadeMinima ?? null; this.errorMessage = '';
  }
  cancelarMinimo(): void { if (!this.minimoSalvando) this.minimoId = null; }
  salvarMinimo(item: Produto): void {
    if (this.minimoSalvando || this.minimoId !== item.id) return;
    const valor = this.minimoValor;
    if (valor !== null && (!Number.isFinite(valor) || valor < 0 || !Number.isInteger(valor) || valor > 999999999)) {
      this.errorMessage = 'Informe uma quantidade mínima válida, ou deixe vazio para desativar.'; return;
    }
    this.minimoSalvando = true; this.errorMessage = '';
    this.minimoRequest = this.produtoService.minimo(item.id, valor).subscribe({
      next: r => { item.quantidadeMinima = r.quantidadeMinima; this.minimoSalvando = false; this.minimoId = null; this.successMessage = 'Quantidade mínima atualizada.'; },
      error: e => { this.minimoSalvando = false; this.errorMessage = e.error?.mensagem ?? 'Não foi possível alterar a quantidade mínima.'; }
    });
  }

  ngOnInit(): void {
    this.searchSubscription = this.search.pipe(debounceTime(300)).subscribe(() => this.carregar());
    this.carregarLookups();
    this.carregar();
  }

  ngAfterViewInit(): void {
    this.productModal = new bootstrap.Modal(this.productModalEl.nativeElement);
    this.deleteModal = new bootstrap.Modal(this.deleteModalEl.nativeElement);
    this.entradaModal = new bootstrap.Modal(this.entradaModalEl.nativeElement);
    this.saidaModal = new bootstrap.Modal(this.saidaModalEl.nativeElement);
    this.historicoModal = new bootstrap.Modal(this.historicoModalEl.nativeElement);
  }

  ngOnDestroy(): void {
    this.minimoRequest?.unsubscribe();
    this.request?.unsubscribe();this.mutation?.unsubscribe();this.lookupRequest?.unsubscribe();this.historicoRequest?.unsubscribe();
    this.searchSubscription?.unsubscribe();this.search.complete();
    this.productModal?.dispose();this.deleteModal?.dispose();this.entradaModal?.dispose();this.saidaModal?.dispose();this.historicoModal?.dispose();
  }

  carregarLookups(): void {
    this.lookupRequest?.unsubscribe();
    this.lookupLoading = true;this.lookupError = '';
    this.lookupRequest = this.produtoService.opcoes().subscribe({
      next: ({categorias,fornecedores}) => {
        this.categorias = categorias;this.fornecedores = fornecedores;this.lookupLoading = false;
      },
      error: () => {
        this.lookupLoading = false;
        this.lookupError = 'Não foi possível carregar categorias e fornecedores ativos. Tente novamente.';
      }
    });
  }

  carregar(): void {
    this.request?.unsubscribe();this.loading = true;this.errorMessage = '';
    this.request = this.produtoService.getAll(this.page-1,this.pageSize,this.searchQuery).subscribe({
      next: data => {
        this.total = data.total;
        const ultima = Math.max(1,Math.ceil(data.total/data.tamanho));
        if (this.page>ultima) { this.page=ultima;this.carregar();return; }
        this.produtos = data.produtos;this.page = data.pagina+1;this.pageSize=data.tamanho;this.loading=false;
      },
      error: err => {this.produtos=[];this.total=0;this.loading=false;this.errorMessage=this.mensagemErro(err,'Erro ao carregar produtos.');}
    });
  }

  onSearch(): void {this.request?.unsubscribe();this.page=1;this.loading=true;this.search.next();}
  get totalPages(): number {return Math.ceil(this.total/this.pageSize);}
  get inicio(): number {return this.total===0 ? 0 : (this.page-1)*this.pageSize+1;}
  get fim(): number {return Math.min(this.page*this.pageSize,this.total);}
  get visiblePages(): number[] {
    const inicio=Math.max(1,Math.min(this.page-2,this.totalPages-4));
    return Array.from({length:Math.max(0,Math.min(5,this.totalPages))},(_,i)=>inicio+i);
  }
  setPage(p: number): void {
    if (this.loading || p<1 || p>this.totalPages || p===this.page) return;
    this.page=p;this.carregar();
  }

  openAdd(): void {
    this.submitted=false;this.isEditing=false;this.modalError='';this.form=this.emptyForm();this.carregarLookups();this.productModal.show();
  }
  openEdit(produto: Produto): void {
    this.submitted=false;this.isEditing=true;this.modalError='';
    this.form={id:produto.id,nome:produto.nome,sku:produto.sku,quantidade:produto.quantidade,
      categoriaID:produto.categoriaID,fornecedorID:produto.fornecedorID,localArmazenamento:produto.localArmazenamento,
      descricao:produto.descricao ?? '',vencimentoProduto:produto.vencimentoProduto ?? null};
    this.carregarLookups();this.productModal.show();
  }
  openDelete(produto: Produto): void {this.produtoParaExcluir=produto;this.deleteError='';this.deleteModal.show();}

  openEntrada(produto: Produto): void {this.prepararMovimento(produto);this.entradaModal.show();}
  openSaida(produto: Produto): void {this.prepararMovimento(produto);this.saidaModal.show();}
  private prepararMovimento(produto: Produto): void {
    this.produtoMovimentacao=produto;this.quantidadeMovimentacao=null;this.quantidadeEnviada=null;
    this.movimentacaoError='';this.motivoMovimentacao='';this.motivoEnviado=null;this.chaveMovimentacao=crypto.randomUUID();
  }
  confirmEntrada(): void {this.movimentar(true);}
  confirmSaida(): void {this.movimentar(false);}
  private movimentar(entrada: boolean): void {
    if (!this.produtoMovimentacao || this.movimentando) return;
    const quantidade=this.quantidadeEnviada ?? this.quantidadeMovimentacao;
    if (quantidade===null || !Number.isInteger(quantidade) || quantidade<=0 || quantidade>1000000000) {
      this.movimentacaoError='Informe uma quantidade inteira entre 1 e 1 bilhão.';return;
    }
    const motivo=this.motivoEnviado ?? this.motivoMovimentacao.trim();
    if(motivo.length<3||motivo.length>500){this.movimentacaoError='Informe o motivo da movimentacao (3 a 500 caracteres).';return;}
    this.motivoEnviado=motivo;
    this.quantidadeEnviada=quantidade;this.movimentando=true;this.movimentacaoError='';
    const produto=this.produtoMovimentacao;
    const operacao=entrada ? this.produtoService.entrada(produto.id,quantidade!,this.chaveMovimentacao,motivo)
      : this.produtoService.saida(produto.id,quantidade!,this.chaveMovimentacao,motivo);
    this.mutation=operacao.subscribe({
      next: () => {
        this.movimentando=false;(entrada ? this.entradaModal : this.saidaModal).hide();
        this.produtoMovimentacao=null;this.successMessage='Movimentação registrada com sucesso.';this.carregar();
      },
      error: err => {
        this.movimentando=false;this.movimentacaoError=this.mensagemErro(err,'Falha ao movimentar estoque. Tente novamente.');
        // Em falha de rede, a mesma chave e quantidade sao mantidas para repetir com seguranca.
        if (err.status>=400 && err.status<500) {this.quantidadeEnviada=null;this.motivoEnviado=null;}
      }
    });
  }

  save(): void {
    if (this.saving) return;
    this.submitted=true;this.modalError='';
    if (this.lookupLoading || this.lookupError || !this.isFormValid()) {
      this.modalError='Confira nome, SKU, categoria, fornecedor, local e quantidade inteira entre 0 e 1 bilhão.';return;
    }
    this.saving=true;
    const operacao=this.isEditing && this.form.id ? this.produtoService.update(this.form.id,this.form) : this.produtoService.add(this.form);
    this.mutation=operacao.subscribe({
      next: () => {this.saving=false;this.productModal.hide();this.successMessage='Produto salvo com sucesso.';this.carregar();},
      error: err => {this.saving=false;this.modalError=this.mensagemErro(err,'Erro ao salvar produto.');}
    });
  }

  confirmDelete(): void {
    if (!this.produtoParaExcluir || this.arquivando) return;
    this.arquivando=true;this.deleteError='';
    this.mutation=this.produtoService.delete(this.produtoParaExcluir.id).subscribe({
      next: () => {this.arquivando=false;this.deleteModal.hide();this.produtoParaExcluir=null;
        this.successMessage='Produto arquivado; histórico preservado.';this.carregar();},
      error: err => {this.arquivando=false;this.deleteError=this.mensagemErro(err,'Erro ao arquivar produto.');}
    });
  }

  openHistorico(produto:Produto):void {
    this.historicoProduto=produto;this.historicoPage=1;this.historico=[];this.historicoTotal=0;
    this.historicoModal.show();this.carregarHistorico();
  }
  carregarHistorico():void {
    if(!this.historicoProduto)return;
    this.historicoRequest?.unsubscribe();this.historicoLoading=true;this.historicoError='';
    this.historicoRequest=this.produtoService.historico(this.historicoProduto.id,this.historicoPage-1,8).subscribe({
      next:p=>{this.historico=p.movimentacoes;this.historicoTotal=p.total;this.historicoLoading=false;},
      error:e=>{this.historicoLoading=false;this.historicoError=this.mensagemErro(e,'Erro ao carregar historico.');}
    });
  }
  get historicoPages():number{return Math.ceil(this.historicoTotal/8);}
  paginaHistorico(p:number):void {if(p<1||p>this.historicoPages||this.historicoLoading)return;this.historicoPage=p;this.carregarHistorico();}

  isSkuInvalid(): boolean {return !/^[A-Za-z0-9_-]{1,64}$/.test(this.form.sku.trim());}
  private isFormValid(): boolean {
    const f=this.form;
    return f.nome.trim().length>=2 && f.nome.trim().length<=150 && !this.isSkuInvalid()
      && Number.isInteger(f.categoriaID) && f.categoriaID!>0 && Number.isInteger(f.fornecedorID) && f.fornecedorID!>0
      && (this.isEditing || (Number.isInteger(f.quantidade) && f.quantidade>=0 && f.quantidade<=1000000000))
      && f.localArmazenamento.trim().length>=2 && f.localArmazenamento.trim().length<=100 && f.descricao.length<=2000;
  }
  private mensagemErro(err: any,fallback: string): string {
    return Object.values(err.error?.erros ?? {}).join(' ') || err.error?.mensagem
      || (err.status===0 ? 'Não foi possível conectar ao servidor.' : fallback);
  }
  private emptyForm(): ProdutoForm {
    return {categoriaID:null,fornecedorID:null,nome:'',sku:'',quantidade:0,localArmazenamento:'',descricao:'',vencimentoProduto:null};
  }
}
