import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { CategoriaService } from '../../../core/services/categoria.service';
import { FornecedorService } from '../../../core/services/fornecedor.service';
import { ProdutoService } from '../../../core/services/produto.service';
import { Produto, ProdutoForm } from '../../../core/models/produto.model';
import { LookupItem } from '../../../core/models/lookup.model';

declare const bootstrap: any;

@Component({
  selector: 'app-estoque-produtos',
  standalone: true,
  imports: [FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './estoque-produtos.component.html',
  styleUrl: './estoque-produtos.component.scss'
})
export class EstoqueProdutosComponent implements OnInit {
  @ViewChild('productModal') productModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;
  @ViewChild('entradaModal') entradaModalEl!: ElementRef;
  @ViewChild('saidaModal') saidaModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Almoxarifado', route: '/almoxarifado' },
    { label: 'Estoque de Produtos' }
  ];

  produtos: Produto[] = [];
  categorias: LookupItem[] = [];
  fornecedores: LookupItem[] = [];
  loading = false;
  searchQuery = '';
  successMessage = '';
  errorMessage = '';
  isEditing = false;
  submitted = false;
  page = 1;
  pageSize = 8;

  form: ProdutoForm = this.emptyForm();
  produtoParaExcluir: Produto | null = null;
  produtoMovimentacao: Produto | null = null;
  quantidadeMovimentacao: number | null = null;
  movimentacaoError = '';

  private productModal?: any;
  private deleteModal?: any;
  private entradaModal?: any;
  private saidaModal?: any;

  constructor(
    private categoriaService: CategoriaService,
    private fornecedorService: FornecedorService,
    private produtoService: ProdutoService
  ) {}

  ngOnInit(): void {
    this.loading = true;
    this.categoriaService.lookup().subscribe({
      next: cats => this.categorias = cats,
      error: () => {}
    });
    this.fornecedorService.lookup().subscribe({
      next: forn => this.fornecedores = forn,
      error: () => {}
    });
    this.produtoService.getAll().subscribe({
      next: produtos => {
        this.produtos = produtos;
        this.loading = false;
      },
      error: () => {
        this.showError('Erro ao carregar produtos.');
        this.loading = false;
      }
    });
  }

  ngAfterViewInit(): void {
    this.productModal = new bootstrap.Modal(this.productModalEl.nativeElement);
    this.deleteModal  = new bootstrap.Modal(this.deleteModalEl.nativeElement);
    this.entradaModal = new bootstrap.Modal(this.entradaModalEl.nativeElement);
    this.saidaModal   = new bootstrap.Modal(this.saidaModalEl.nativeElement);
  }

  categoriaNome(id: number | null): string {
    return this.categorias.find(c => c.id === id)?.nome ?? '—';
  }

  get filtered(): Produto[] {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) return this.produtos;
    return this.produtos.filter(p =>
      p.nome.toLowerCase().includes(q) ||
      p.sku.toLowerCase().includes(q) ||
      (p.descricao ?? '').toLowerCase().includes(q)
    );
  }

  get paged(): Produto[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtered.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filtered.length / this.pageSize);
  }

  get visiblePages(): number[] {
    const half = 5;
    const start = Math.max(1, Math.min(this.page - half, this.totalPages - 7));
    const end = Math.min(this.totalPages, start + 7);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  openAdd(): void {
    this.submitted = false;
    this.isEditing = false;
    this.form = this.emptyForm();
    this.productModal.show();
  }

  openEdit(produto: Produto): void {
    this.submitted = false;
    this.isEditing = true;
    this.form = {
      id: produto.id,
      categoriaID: produto.categoriaID,
      fornecedorID: produto.fornecedorID ?? null,
      nome: produto.nome,
      sku: produto.sku,
      quantidade: produto.quantidade,
      localArmazenamento: produto.localArmazenamento ?? '',
      descricao: produto.descricao ?? '',
      vencimentoProduto: produto.vencimentoProduto ?? null
    };
    this.productModal.show();
  }

  openDelete(produto: Produto): void {
    this.produtoParaExcluir = produto;
    this.deleteModal.show();
  }

  openEntrada(produto: Produto): void {
    this.produtoMovimentacao = produto;
    this.quantidadeMovimentacao = null;
    this.movimentacaoError = '';
    this.entradaModal.show();
  }

  openSaida(produto: Produto): void {
    this.produtoMovimentacao = produto;
    this.quantidadeMovimentacao = null;
    this.movimentacaoError = '';
    this.saidaModal.show();
  }

  confirmEntrada(): void {
    if (!this.produtoMovimentacao) return;

    if (!this.quantidadeMovimentacao || this.quantidadeMovimentacao <= 0) {
      this.movimentacaoError = 'Informe uma quantidade válida.';
      return;
    }

    const produto = this.produtoMovimentacao;
    const novaQuantidade = produto.quantidade + this.quantidadeMovimentacao;

    this.produtoService.update(produto.id, this.formFromProduto(produto, novaQuantidade)).subscribe({
      next: atualizado => {
        const idx = this.produtos.findIndex(p => p.id === atualizado.id);
        if (idx > -1) this.produtos[idx] = atualizado;
        this.entradaModal.hide();
        this.showSuccess('Entrada de estoque registrada com sucesso!');
      },
      error: () => this.showError('Erro ao registrar entrada de estoque.')
    });
  }

  confirmSaida(): void {
    if (!this.produtoMovimentacao) return;

    if (!this.quantidadeMovimentacao || this.quantidadeMovimentacao <= 0) {
      this.movimentacaoError = 'Informe uma quantidade válida.';
      return;
    }

    if (this.quantidadeMovimentacao > this.produtoMovimentacao.quantidade) {
      this.movimentacaoError = 'Quantidade maior que o estoque disponível.';
      return;
    }

    const produto = this.produtoMovimentacao;
    const novaQuantidade = produto.quantidade - this.quantidadeMovimentacao;

    this.produtoService.update(produto.id, this.formFromProduto(produto, novaQuantidade)).subscribe({
      next: atualizado => {
        const idx = this.produtos.findIndex(p => p.id === atualizado.id);
        if (idx > -1) this.produtos[idx] = atualizado;
        this.saidaModal.hide();
        this.showSuccess('Saída de estoque registrada com sucesso!');
      },
      error: () => this.showError('Erro ao registrar saída de estoque.')
    });
  }

  private formFromProduto(produto: Produto, quantidade: number): ProdutoForm {
    return {
      id: produto.id,
      categoriaID: produto.categoriaID,
      fornecedorID: produto.fornecedorID ?? null,
      nome: produto.nome,
      sku: produto.sku,
      quantidade,
      localArmazenamento: produto.localArmazenamento ?? '',
      descricao: produto.descricao ?? '',
      vencimentoProduto: produto.vencimentoProduto ?? null
    };
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;

    if (this.isEditing && this.form.id) {
      this.produtoService.update(this.form.id, this.form).subscribe({
        next: updated => {
          const idx = this.produtos.findIndex(p => p.id === updated.id);
          if (idx > -1) this.produtos[idx] = updated;
          this.productModal.hide();
          this.showSuccess('Produto atualizado com sucesso!');
        },
        error: (err) => this.showError(err.error?.message ?? 'Erro ao atualizar produto.')
      });
    } else {
      this.produtoService.add(this.form).subscribe({
        next: novo => {
          this.produtos.unshift(novo);
          this.productModal.hide();
          this.showSuccess('Produto criado com sucesso!');
        },
        error: (err) => this.showError(err.error?.message ?? 'Erro ao criar produto.')
      });
    }
  }

  confirmDelete(): void {
    if (!this.produtoParaExcluir) return;
    this.produtoService.delete(this.produtoParaExcluir.id).subscribe({
      next: () => {
        this.produtos = this.produtos.filter(p => p.id !== this.produtoParaExcluir!.id);
        this.produtoParaExcluir = null;
        this.deleteModal.hide();
        this.showSuccess('Produto excluído com sucesso!');
      },
      error: (err) => this.showError(err.error?.message ?? 'Erro ao excluir produto.')
    });
  }

  setPage(p: number): void { this.page = p; }

  isSkuInvalid(): boolean {
    return !this.form.sku.trim() || !/^[A-Za-z0-9\-_]+$/.test(this.form.sku.trim());
  }

  private isFormValid(): boolean {
    if (!this.form.nome.trim()) return false;
    if (!this.isEditing && this.isSkuInvalid()) return false;
    if (this.form.categoriaID === null) return false;
    if (this.form.fornecedorID === null) return false;
    if (this.form.quantidade === null || this.form.quantidade === undefined) return false;
    if (!this.form.localArmazenamento.trim()) return false;
    return true;
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    this.errorMessage = '';
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private showError(msg: string): void {
    this.errorMessage = msg;
    this.successMessage = '';
  }

  fornecedorNome(id: number | null | undefined): string {
    return this.fornecedores.find(f => f.id === id)?.nome ?? '—';
  }

  private emptyForm(): ProdutoForm {
    return { categoriaID: null, fornecedorID: null, nome: '', sku: '', quantidade: 0, localArmazenamento: '', descricao: '', vencimentoProduto: null };
  }
}
