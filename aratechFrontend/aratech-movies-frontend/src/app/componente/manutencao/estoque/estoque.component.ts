import { Component, ElementRef, AfterViewInit, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { PecaEstoqueService } from '../../../core/services/peca-estoque.service';
import { PecaEstoque, PecaEstoqueForm } from '../../../core/models/peca-estoque.model';

declare const bootstrap: any;

@Component({
  selector: 'app-estoque-manutencao',
  standalone: true,
  imports: [FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './estoque.component.html',
  styleUrl: './estoque.component.scss'
})
export class EstoqueManutencaoComponent implements OnInit, AfterViewInit {
  @ViewChild('pecaModal') pecaModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;
  @ViewChild('entradaModal') entradaModalEl!: ElementRef;
  @ViewChild('saidaModal') saidaModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Manutenção', route: '/manutencao' },
    { label: 'Estoque' }
  ];

  pecas: PecaEstoque[] = [];
  loading = false;
  searchTerm = '';
  isEditing = false;
  page = 1;
  pageSize = 8;
  submitted = false;
  successMessage = '';
  errorMessage = '';

  form: PecaEstoqueForm = this.emptyForm();
  pecaParaExcluir: PecaEstoque | null = null;
  pecaMovimentacao: PecaEstoque | null = null;
  quantidadeMovimentacao: number | null = null;
  movimentacaoError = '';

  private pecaModal?: any;
  private deleteModal?: any;
  private entradaModal?: any;
  private saidaModal?: any;

  constructor(private pecaEstoqueService: PecaEstoqueService) {}

  ngOnInit(): void {
    this.loading = true;
    this.pecaEstoqueService.getAll().subscribe({
      next: pecas => {
        this.pecas = pecas;
        this.loading = false;
      },
      error: () => {
        this.showError('Erro ao carregar o estoque de peças.');
        this.loading = false;
      }
    });
  }

  ngAfterViewInit(): void {
    this.pecaModal    = new bootstrap.Modal(this.pecaModalEl.nativeElement);
    this.deleteModal  = new bootstrap.Modal(this.deleteModalEl.nativeElement);
    this.entradaModal = new bootstrap.Modal(this.entradaModalEl.nativeElement);
    this.saidaModal   = new bootstrap.Modal(this.saidaModalEl.nativeElement);
  }

  get filtradas(): PecaEstoque[] {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) return this.pecas;
    return this.pecas.filter(p =>
      p.nome.toLowerCase().includes(term) ||
      p.codigo.toLowerCase().includes(term) ||
      p.localizacao.toLowerCase().includes(term) ||
      p.id.toString().includes(term)
    );
  }

  get paged(): PecaEstoque[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtradas.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filtradas.length / this.pageSize);
  }

  get visiblePages(): number[] {
    const half = 5;
    const start = Math.max(1, Math.min(this.page - half, this.totalPages - 7));
    const end = Math.min(this.totalPages, start + 7);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  setPage(p: number): void { this.page = p; }

  openAdd(): void {
    this.isEditing = false;
    this.submitted = false;
    this.form = this.emptyForm();
    this.pecaModal.show();
  }

  openEdit(peca: PecaEstoque): void {
    this.isEditing = true;
    this.submitted = false;
    this.form = {
      id: peca.id,
      nome: peca.nome,
      codigo: peca.codigo,
      quantidade: peca.quantidade,
      unidade: peca.unidade,
      localizacao: peca.localizacao,
      descricao: peca.descricao ?? ''
    };
    this.pecaModal.show();
  }

  openDelete(peca: PecaEstoque): void {
    this.pecaParaExcluir = peca;
    this.deleteModal.show();
  }

  openEntrada(peca: PecaEstoque): void {
    this.pecaMovimentacao = peca;
    this.quantidadeMovimentacao = null;
    this.movimentacaoError = '';
    this.entradaModal.show();
  }

  openSaida(peca: PecaEstoque): void {
    this.pecaMovimentacao = peca;
    this.quantidadeMovimentacao = null;
    this.movimentacaoError = '';
    this.saidaModal.show();
  }

  confirmEntrada(): void {
    if (!this.pecaMovimentacao) return;

    if (!this.quantidadeMovimentacao || this.quantidadeMovimentacao <= 0) {
      this.movimentacaoError = 'Informe uma quantidade válida.';
      return;
    }

    const peca = this.pecaMovimentacao;
    const novaQuantidade = peca.quantidade + this.quantidadeMovimentacao;

    this.pecaEstoqueService.update(peca.id, this.formFromPeca(peca, novaQuantidade)).subscribe({
      next: atualizada => {
        const idx = this.pecas.findIndex(p => p.id === atualizada.id);
        if (idx > -1) this.pecas[idx] = atualizada;
        this.entradaModal.hide();
        this.showSuccess('Entrada de estoque registrada com sucesso!');
      },
      error: () => this.showError('Erro ao registrar entrada de estoque.')
    });
  }

  confirmSaida(): void {
    if (!this.pecaMovimentacao) return;

    if (!this.quantidadeMovimentacao || this.quantidadeMovimentacao <= 0) {
      this.movimentacaoError = 'Informe uma quantidade válida.';
      return;
    }

    if (this.quantidadeMovimentacao > this.pecaMovimentacao.quantidade) {
      this.movimentacaoError = 'Quantidade maior que o estoque disponível.';
      return;
    }

    const peca = this.pecaMovimentacao;
    const novaQuantidade = peca.quantidade - this.quantidadeMovimentacao;

    this.pecaEstoqueService.update(peca.id, this.formFromPeca(peca, novaQuantidade)).subscribe({
      next: atualizada => {
        const idx = this.pecas.findIndex(p => p.id === atualizada.id);
        if (idx > -1) this.pecas[idx] = atualizada;
        this.saidaModal.hide();
        this.showSuccess('Saída de estoque registrada com sucesso!');
      },
      error: () => this.showError('Erro ao registrar saída de estoque.')
    });
  }

  private formFromPeca(peca: PecaEstoque, quantidade: number): PecaEstoqueForm {
    return {
      id: peca.id,
      nome: peca.nome,
      codigo: peca.codigo,
      quantidade,
      unidade: peca.unidade,
      localizacao: peca.localizacao,
      descricao: peca.descricao ?? ''
    };
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;

    if (this.isEditing && this.form.id) {
      this.pecaEstoqueService.update(this.form.id, this.form).subscribe({
        next: atualizada => {
          const idx = this.pecas.findIndex(p => p.id === atualizada.id);
          if (idx > -1) this.pecas[idx] = atualizada;
          this.pecaModal.hide();
          this.showSuccess('Peça atualizada com sucesso!');
        },
        error: (err) => this.showError(err.error?.message ?? 'Erro ao atualizar peça.')
      });
    } else {
      this.pecaEstoqueService.add(this.form).subscribe({
        next: nova => {
          this.pecas.unshift(nova);
          this.pecaModal.hide();
          this.showSuccess('Peça adicionada com sucesso!');
        },
        error: (err) => this.showError(err.error?.message ?? 'Erro ao adicionar peça.')
      });
    }
  }

  confirmDelete(): void {
    if (!this.pecaParaExcluir) return;
    const id = this.pecaParaExcluir.id;
    this.pecaEstoqueService.delete(id).subscribe({
      next: () => {
        this.pecas = this.pecas.filter(p => p.id !== id);
        this.pecaParaExcluir = null;
        this.deleteModal.hide();
        this.showSuccess('Peça removida com sucesso!');
      },
      error: (err) => {
        this.deleteModal.hide();
        this.showError(err.error?.message ?? 'Erro ao remover peça.');
      }
    });
  }

  estoqueClass(quantidade: number): string {
    if (quantidade === 0)  return 'text-danger fw-semibold';
    if (quantidade <= 5)   return 'text-warning fw-semibold';
    return '';
  }

  private isFormValid(): boolean {
    return !!(
      this.form.nome?.trim() &&
      this.form.codigo?.trim() &&
      this.form.unidade?.trim() &&
      this.form.localizacao?.trim()
    );
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

  private emptyForm(): PecaEstoqueForm {
    return { nome: '', codigo: '', quantidade: 0, unidade: 'un', localizacao: '', descricao: '' };
  }
}
