import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export interface PecaAssistencia {
  id: number;
  nome: string;
  codigo: string;
  quantidade: number;
  quantidadeMinima: number;
  unidade: string;
  localizacao: string;
  descricao: string;
}

@Component({
  selector: 'app-estoque-pecas',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './estoque-pecas.component.html',
  styleUrl: './estoque-pecas.component.scss'
})
export class EstoquePecasComponent implements AfterViewInit {
  @ViewChild('pecaModal') pecaModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Assistência Técnica', route: '/assistencia' },
    { label: 'Estoque de Peças' }
  ];

  pecas: PecaAssistencia[] = [];
  searchTerm = '';
  apenasAbaixoMinimo = false;
  page = 1;
  pageSize = 8;
  isEditing = false;
  submitted = false;
  successMessage = '';

  form: Partial<PecaAssistencia> = this.emptyForm();
  pecaParaExcluir: PecaAssistencia | null = null;

  private pecaModal?: any;
  private deleteModal?: any;

  ngAfterViewInit(): void {
    this.pecaModal   = new bootstrap.Modal(this.pecaModalEl.nativeElement);
    this.deleteModal = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  get filtradas(): PecaAssistencia[] {
    let lista = [...this.pecas];
    if (this.apenasAbaixoMinimo) lista = lista.filter(p => p.quantidade <= p.quantidadeMinima);
    const term = this.searchTerm.trim().toLowerCase();
    if (term) lista = lista.filter(p =>
      p.nome.toLowerCase().includes(term) ||
      p.codigo.toLowerCase().includes(term)
    );
    return lista;
  }

  get paged(): PecaAssistencia[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtradas.slice(start, start + this.pageSize);
  }

  get totalPages(): number { return Math.ceil(this.filtradas.length / this.pageSize); }
  get visiblePages(): number[] {
    const start = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    return Array.from({ length: Math.min(this.totalPages, start + 4) - start + 1 }, (_, i) => start + i);
  }
  get paginationStart(): number { return this.filtradas.length === 0 ? 0 : (this.page - 1) * this.pageSize + 1; }
  get paginationEnd(): number { return Math.min(this.page * this.pageSize, this.filtradas.length); }
  get totalAbaixoMinimo(): number { return this.pecas.filter(p => p.quantidade <= p.quantidadeMinima).length; }

  setPage(p: number): void { if (p >= 1 && p <= this.totalPages) this.page = p; }

  openAdd(): void {
    this.isEditing = false;
    this.submitted = false;
    this.form = this.emptyForm();
    this.pecaModal.show();
  }

  openEdit(peca: PecaAssistencia): void {
    this.isEditing = true;
    this.submitted = false;
    this.form = { ...peca };
    this.pecaModal.show();
  }

  openDelete(peca: PecaAssistencia): void {
    this.pecaParaExcluir = peca;
    this.deleteModal.show();
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;
    if (this.isEditing) {
      const idx = this.pecas.findIndex(p => p.id === this.form.id);
      if (idx > -1) this.pecas[idx] = { ...this.form } as PecaAssistencia;
      this.showSuccess('Peça atualizada com sucesso!');
    } else {
      const newId = this.pecas.length ? Math.max(...this.pecas.map(p => p.id)) + 1 : 1;
      this.pecas.push({ ...this.form, id: newId } as PecaAssistencia);
      this.showSuccess('Peça adicionada ao estoque!');
    }
    this.pecaModal.hide();
  }

  confirmDelete(): void {
    if (!this.pecaParaExcluir) return;
    this.pecas = this.pecas.filter(p => p.id !== this.pecaParaExcluir!.id);
    this.pecaParaExcluir = null;
    this.deleteModal.hide();
    this.showSuccess('Peça removida do estoque!');
  }

  estoqueClass(peca: PecaAssistencia): string {
    if (peca.quantidade === 0)                        return 'text-danger fw-semibold';
    if (peca.quantidade <= peca.quantidadeMinima)     return 'text-warning fw-semibold';
    return '';
  }

  private isFormValid(): boolean {
    return !!(this.form.nome?.trim() && this.form.codigo?.trim() &&
              this.form.unidade?.trim() && this.form.localizacao?.trim());
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private emptyForm(): Partial<PecaAssistencia> {
    return { nome: '', codigo: '', quantidade: 0, quantidadeMinima: 2, unidade: 'un', localizacao: '', descricao: '' };
  }
}
