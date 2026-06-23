import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusOrdem = 'Planejada' | 'Em Produção' | 'Finalizada' | 'Cancelada';

export interface OrdemProducao {
  id: number;
  numero: string;
  produto: string;
  quantidade: number;
  dataInicio: string;
  dataPrevisao: string;
  responsavel: string;
  status: StatusOrdem;
}

@Component({
  selector: 'app-ordens',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './ordens.component.html',
  styleUrl: './ordens.component.scss'
})
export class OrdensProducaoComponent implements AfterViewInit {
  @ViewChild('ordemModal') ordemModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'PCP', route: '/pcp' },
    { label: 'Ordens de Produção' }
  ];

  ordens: OrdemProducao[] = [];
  searchTerm = '';
  filtroStatus: StatusOrdem | 'Todas' = 'Todas';
  page = 1;
  pageSize = 8;
  isEditing = false;
  submitted = false;
  successMessage = '';

  form: Partial<OrdemProducao> = this.emptyForm();
  ordemParaExcluir: OrdemProducao | null = null;

  private ordemModal?: any;
  private deleteModal?: any;

  ngAfterViewInit(): void {
    this.ordemModal  = new bootstrap.Modal(this.ordemModalEl.nativeElement);
    this.deleteModal = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  get filtradas(): OrdemProducao[] {
    let lista = [...this.ordens];
    if (this.filtroStatus !== 'Todas') lista = lista.filter(o => o.status === this.filtroStatus);
    const term = this.searchTerm.trim().toLowerCase();
    if (term) lista = lista.filter(o =>
      o.numero.toLowerCase().includes(term) ||
      o.produto.toLowerCase().includes(term) ||
      o.responsavel.toLowerCase().includes(term)
    );
    return lista;
  }

  get paged(): OrdemProducao[] {
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

  setPage(p: number): void { if (p >= 1 && p <= this.totalPages) this.page = p; }
  setFiltro(s: string): void { this.filtroStatus = s as StatusOrdem | 'Todas'; this.page = 1; }

  openAdd(): void {
    this.isEditing = false;
    this.submitted = false;
    this.form = this.emptyForm();
    this.ordemModal.show();
  }

  openEdit(o: OrdemProducao): void {
    this.isEditing = true;
    this.submitted = false;
    this.form = { ...o };
    this.ordemModal.show();
  }

  openDelete(o: OrdemProducao): void {
    this.ordemParaExcluir = o;
    this.deleteModal.show();
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;
    if (this.isEditing) {
      const idx = this.ordens.findIndex(o => o.id === this.form.id);
      if (idx > -1) this.ordens[idx] = { ...this.form } as OrdemProducao;
      this.showSuccess('Ordem atualizada com sucesso!');
    } else {
      const newId = this.ordens.length ? Math.max(...this.ordens.map(o => o.id)) + 1 : 1;
      const num = `OP-${String(newId).padStart(4, '0')}`;
      this.ordens.push({ ...this.form, id: newId, numero: num, status: 'Planejada' } as OrdemProducao);
      this.showSuccess('Ordem de produção criada com sucesso!');
    }
    this.ordemModal.hide();
  }

  avancarStatus(o: OrdemProducao): void {
    const fluxo: StatusOrdem[] = ['Planejada', 'Em Produção', 'Finalizada'];
    const idx = fluxo.indexOf(o.status);
    if (idx < fluxo.length - 1) {
      o.status = fluxo[idx + 1];
      this.showSuccess(`Ordem ${o.numero} avançada para "${o.status}".`);
    }
  }

  confirmDelete(): void {
    if (!this.ordemParaExcluir) return;
    this.ordens = this.ordens.filter(o => o.id !== this.ordemParaExcluir!.id);
    this.ordemParaExcluir = null;
    this.deleteModal.hide();
    this.showSuccess('Ordem removida com sucesso!');
  }

  badgeClass(status: StatusOrdem): string {
    const map: Record<StatusOrdem, string> = {
      'Planejada':    'bg-secondary',
      'Em Produção':  'bg-warning text-dark',
      'Finalizada':   'bg-success',
      'Cancelada':    'bg-danger'
    };
    return map[status] ?? 'bg-secondary';
  }

  private isFormValid(): boolean {
    return !!(this.form.produto?.trim() && this.form.quantidade && this.form.quantidade > 0 &&
              this.form.dataInicio && this.form.dataPrevisao && this.form.responsavel?.trim());
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private emptyForm(): Partial<OrdemProducao> {
    return { produto: '', quantidade: 1, dataInicio: '', dataPrevisao: '', responsavel: '' };
  }
}
