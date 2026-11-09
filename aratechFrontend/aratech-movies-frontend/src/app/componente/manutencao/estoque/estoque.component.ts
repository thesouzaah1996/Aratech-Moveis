import { PermissaoDirective } from '../../../core/permissao.directive';
import { SessaoState } from '../../../core/services/sessao-state.service';
import { DatePipe } from '@angular/common';
import { Component, ElementRef, OnInit, OnDestroy, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription, debounceTime } from 'rxjs';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { PecaEstoqueService } from '../../../core/services/peca-estoque.service';
import { PecaEstoque, PecaEstoqueForm, MovimentacaoPecaEstoque } from '../../../core/models/peca-estoque.model';

import { criarModal, ModalDaTela } from '../../../core/ui/modal';
import { quantidadePecaValida, validarPecaEstoque } from '../../../core/models/peca-estoque-form';

@Component({
  selector: 'app-estoque-manutencao',standalone: true,
  imports: [PermissaoDirective,DatePipe,FormsModule,NavbarComponent,BreadcrumbComponent],
  templateUrl: './estoque.component.html',styleUrl: './estoque.component.scss'
})
export class EstoqueManutencaoComponent implements OnInit,AfterViewInit,OnDestroy {
  @ViewChild('pecaModal') pecaModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;
  @ViewChild('entradaModal') entradaModalEl!: ElementRef;
  @ViewChild('historicoModal') historicoModalEl!: ElementRef;
  @ViewChild('saidaModal') saidaModalEl!: ElementRef;
  breadcrumb: BreadcrumbItem[] = [
    {label: 'Início',route: '/dashboard'},{label: 'Manutenção',route: '/manutencao'},{label: 'Estoque'}
  ];
  pecas: PecaEstoque[] = [];
  loading = false;
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
  form: PecaEstoqueForm = this.emptyForm();
  pecaParaExcluir: PecaEstoque | null = null;
  pecaMovimentacao: PecaEstoque | null = null;
  quantidadeMovimentacao: number | null = null;
  quantidadeEnviada: number | null = null;
  movimentacaoError = '';
  motivoMovimentacao = '';
  motivoEnviado: string | null = null;
  historico: MovimentacaoPecaEstoque[] = [];
  historicoPeca: PecaEstoque | null = null;
  historicoPage = 1;
  historicoTotal = 0;
  historicoLoading = false;
  historicoError = '';
  private historicoModal?: ModalDaTela;
  private historicoRequest?: Subscription;
  private chaveMovimentacao = '';
  private pecaModal?: ModalDaTela;
  private deleteModal?: ModalDaTela;
  private entradaModal?: ModalDaTela;
  private saidaModal?: ModalDaTela;
  private request?: Subscription;
  private mutation?: Subscription;
  private search = new Subject<void>();
  private searchSubscription?: Subscription;

  constructor(private readonly pecaEstoqueService: PecaEstoqueService, public sessao: SessaoState = new SessaoState()) {}

  minimoId: number | null = null;
  minimoValor: number | null = null;
  minimoSalvando = false;
  private minimoRequest?: Subscription;
  editarMinimo(item: PecaEstoque): void {
    if (this.minimoSalvando || !this.sessao.permite('MANUTENCAO_ESTOQUE.ALTERAR')) return;
    this.minimoId = item.id; this.minimoValor = item.quantidadeMinima ?? null; this.errorMessage = '';
  }
  cancelarMinimo(): void { if (!this.minimoSalvando) this.minimoId = null; }
  salvarMinimo(item: PecaEstoque): void {
    if (this.minimoSalvando || this.minimoId !== item.id) return;
    const valor = this.minimoValor;
    if (valor !== null && (!Number.isFinite(valor) || valor < 0 || Math.abs(valor * 1000 - Math.round(valor * 1000)) > 0.001 || valor > 999999999998.999)) {
      this.errorMessage = 'Informe uma quantidade mínima válida, ou deixe vazio para desativar.'; return;
    }
    this.minimoSalvando = true; this.errorMessage = '';
    this.minimoRequest = this.pecaEstoqueService.minimo(item.id, valor).subscribe({
      next: r => { item.quantidadeMinima = r.quantidadeMinima; this.minimoSalvando = false; this.minimoId = null; this.successMessage = 'Quantidade mínima atualizada.'; },
      error: e => { this.minimoSalvando = false; this.errorMessage = e.error?.mensagem ?? 'Não foi possível alterar a quantidade mínima.'; }
    });
  }

  ngOnInit(): void {
    this.searchSubscription = this.search.pipe(debounceTime(300)).subscribe(() => this.carregar());

    this.carregar();
  }

  ngAfterViewInit(): void {
    this.pecaModal = criarModal(this.pecaModalEl.nativeElement);
    this.deleteModal = criarModal(this.deleteModalEl.nativeElement);
    this.entradaModal = criarModal(this.entradaModalEl.nativeElement);
    this.saidaModal = criarModal(this.saidaModalEl.nativeElement);
    this.historicoModal = criarModal(this.historicoModalEl.nativeElement);
  }

  ngOnDestroy(): void {
    this.minimoRequest?.unsubscribe();
    this.request?.unsubscribe();this.mutation?.unsubscribe();this.historicoRequest?.unsubscribe();
    this.searchSubscription?.unsubscribe();this.search.complete();
    this.pecaModal?.dispose();this.deleteModal?.dispose();this.entradaModal?.dispose();this.saidaModal?.dispose();this.historicoModal?.dispose();
  }

  carregar(): void {
    this.request?.unsubscribe();this.loading = true;this.errorMessage = '';
    this.request = this.pecaEstoqueService.getAll(this.page-1,this.pageSize,this.searchQuery).subscribe({
      next: data => {
        this.total = data.total;
        const ultima = Math.max(1,Math.ceil(data.total/data.tamanho));
        if (this.page>ultima) { this.page=ultima;this.carregar();return; }
        this.pecas = data.pecas;this.page = data.pagina+1;this.pageSize=data.tamanho;this.loading=false;
      },
      error: err => {this.pecas=[];this.total=0;this.loading=false;this.errorMessage=this.mensagemErro(err,'Erro ao carregar pecas.');}
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
    if (this.saving || this.movimentando || this.arquivando) return;
    this.submitted=false;this.isEditing=false;this.modalError='';this.form=this.emptyForm();this.pecaModal?.show();
  }
  openEdit(peca: PecaEstoque): void {
    if (this.saving || this.movimentando || this.arquivando) return;
    this.submitted=false;this.isEditing=true;this.modalError='';
    this.form={id:peca.id,nome:peca.nome,codigo:peca.codigo,quantidade:peca.quantidade,unidade:peca.unidade,localizacao:peca.localizacao,descricao:peca.descricao??''};
    this.pecaModal?.show();
  }
  openDelete(peca: PecaEstoque): void {this.pecaParaExcluir=peca;this.deleteError='';this.deleteModal?.show();}

  openEntrada(peca: PecaEstoque): void {this.prepararMovimento(peca);this.entradaModal?.show();}
  openSaida(peca: PecaEstoque): void {this.prepararMovimento(peca);this.saidaModal?.show();}
  private prepararMovimento(peca: PecaEstoque): void {
    this.pecaMovimentacao=peca;this.quantidadeMovimentacao=null;this.quantidadeEnviada=null;
    this.movimentacaoError='';this.motivoMovimentacao='';this.motivoEnviado=null;this.chaveMovimentacao=crypto.randomUUID();
  }
  confirmEntrada(): void {this.movimentar(true);}
  confirmSaida(): void {this.movimentar(false);}
  private movimentar(entrada: boolean): void {
    if (!this.pecaMovimentacao || this.movimentando) return;
    const quantidade=this.quantidadeEnviada ?? this.quantidadeMovimentacao;
    if (!quantidadePecaValida(quantidade)) {
      this.movimentacaoError='Informe uma quantidade positiva com ate 3 casas decimais.';return;
    }
    const motivo=this.motivoEnviado ?? this.motivoMovimentacao.trim();
    if(motivo.length<3||motivo.length>500||/[\p{Cc}\p{Cf}]/u.test(motivo.replace(/[\r\n\t]/g,''))){this.movimentacaoError='Informe o motivo da movimentacao (3 a 500 caracteres).';return;}
    this.motivoEnviado=motivo;
    this.quantidadeEnviada=quantidade;this.movimentando=true;this.movimentacaoError='';
    const peca=this.pecaMovimentacao;
    const operacao=entrada ? this.pecaEstoqueService.entrada(peca.id,quantidade!,this.chaveMovimentacao,motivo)
      : this.pecaEstoqueService.saida(peca.id,quantidade!,this.chaveMovimentacao,motivo);
    this.mutation=operacao.subscribe({
      next: () => {
        this.movimentando=false;(entrada ? this.entradaModal : this.saidaModal)?.hide();
        this.pecaMovimentacao=null;this.successMessage='Movimentação registrada com sucesso.';this.carregar();
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
    if (!validarPecaEstoque(this.form,this.isEditing)) {
      this.modalError='Confira nome, codigo, unidade, localizacao e quantidade com ate 3 casas decimais.';return;
    }
    this.saving=true;
    const operacao=this.isEditing && this.form.id ? this.pecaEstoqueService.update(this.form.id,this.form) : this.pecaEstoqueService.add(this.form);
    this.mutation=operacao.subscribe({
      next: () => {this.saving=false;this.pecaModal?.hide();this.successMessage='PecaEstoque salvo com sucesso.';this.carregar();},
      error: err => {this.saving=false;this.modalError=this.mensagemErro(err,'Erro ao salvar peca.');}
    });
  }

  confirmDelete(): void {
    if (!this.pecaParaExcluir || this.arquivando) return;
    this.arquivando=true;this.deleteError='';
    this.mutation=this.pecaEstoqueService.delete(this.pecaParaExcluir.id).subscribe({
      next: () => {this.arquivando=false;this.deleteModal?.hide();this.pecaParaExcluir=null;
        this.successMessage='PecaEstoque arquivado; histórico preservado.';this.carregar();},
      error: err => {this.arquivando=false;this.deleteError=this.mensagemErro(err,'Erro ao arquivar peca.');}
    });
  }

  openHistorico(peca:PecaEstoque):void {
    this.historicoPeca=peca;this.historicoPage=1;this.historico=[];this.historicoTotal=0;
    this.historicoModal?.show();this.carregarHistorico();
  }
  carregarHistorico():void {
    if(!this.historicoPeca)return;
    this.historicoRequest?.unsubscribe();this.historicoLoading=true;this.historicoError='';
    this.historicoRequest=this.pecaEstoqueService.historico(this.historicoPeca.id,this.historicoPage-1,8).subscribe({
      next:p=>{this.historico=p.movimentacoes;this.historicoTotal=p.total;this.historicoLoading=false;},
      error:e=>{this.historicoLoading=false;this.historicoError=this.mensagemErro(e,'Erro ao carregar historico.');}
    });
  }
  get historicoPages():number{return Math.ceil(this.historicoTotal/8);}
  paginaHistorico(p:number):void {if(p<1||p>this.historicoPages||this.historicoLoading)return;this.historicoPage=p;this.carregarHistorico();}

  estoqueClass(quantidade: number): string { return quantidade === 0 ? 'text-danger fw-semibold' : quantidade <= 5 ? 'text-warning fw-semibold' : ''; }
  cancelarCadastro(): void { if (!this.saving) this.pecaModal?.hide(); }
  cancelarArquivo(): void { if (!this.arquivando) this.deleteModal?.hide(); }
  cancelarEntrada(): void { if (!this.movimentando) this.entradaModal?.hide(); }
  cancelarSaida(): void { if (!this.movimentando) this.saidaModal?.hide(); }
  fecharHistorico(): void { this.historicoRequest?.unsubscribe(); this.historicoLoading=false; this.historicoModal?.hide(); }
  private mensagemErro(err: any,fallback: string): string {
    return Object.values(err.error?.erros ?? {}).join(' ') || err.error?.mensagem
      || (err.status===0 ? 'Não foi possível conectar ao servidor.' : fallback);
  }
  private emptyForm(): PecaEstoqueForm {
    return {nome:'',codigo:'',quantidade:0,unidade:'un',localizacao:'',descricao:''};
  }
}
