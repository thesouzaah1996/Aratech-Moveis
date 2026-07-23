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

  private pecaModal?: any;
  private deleteModal?: any;

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
    this.pecaModal   = new bootstrap.Modal(this.pecaModalEl.nativeElement);
    this.deleteModal = new bootstrap.Modal(this.deleteModalEl.nativeElement);
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
    const start = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    const end   = Math.min(this.totalPages, start + 4);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  get paginationStart(): number {
    return this.filtradas.length === 0 ? 0 : (this.page - 1) * this.pageSize + 1;
  }

  get paginationEnd(): number {
    return Math.min(this.page * this.pageSize, this.filtradas.length);
  }

  setPage(p: number): void {
    if (p < 1 || p > this.totalPages) return;
    this.page = p;
  }

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
