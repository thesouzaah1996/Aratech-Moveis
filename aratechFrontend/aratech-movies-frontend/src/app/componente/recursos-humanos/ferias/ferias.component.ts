import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusFerias = 'Pendente' | 'Aprovada' | 'Em Curso' | 'Concluída' | 'Recusada';
export type TipoAfastamento = 'Férias' | 'Licença Médica' | 'Licença Maternidade' | 'Licença Paternidade' | 'Afastamento';

export interface PeriodoFerias {
  id: number;
  funcionario: string;
  matricula: string;
  setor: string;
  tipo: TipoAfastamento;
  dataInicio: string;
  dataFim: string;
  dias: number;
  status: StatusFerias;
}

@Component({
  selector: 'app-ferias',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './ferias.component.html',
  styleUrl: './ferias.component.scss'
})
export class FeriasComponent implements AfterViewInit {
  @ViewChild('feriasModal') feriasModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Recursos Humanos', route: '/recursos-humanos' },
    { label: 'Férias e Afastamentos' }
  ];

  // TODO: carregar via serviço quando backend estiver pronto
  periodos: PeriodoFerias[] = [];

  searchTerm = '';
  filtroStatus: StatusFerias | 'Todos' = 'Todos';
  page = 1;
  pageSize = 8;
  submitted = false;
  successMessage = '';
  errorMessage = '';

  form: Partial<PeriodoFerias> = this.emptyForm();
  periodoParaExcluir: PeriodoFerias | null = null;

  private feriasModal?: any;
  private deleteModal?: any;

  ngAfterViewInit(): void {
    this.feriasModal = new bootstrap.Modal(this.feriasModalEl.nativeElement);
    this.deleteModal = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  get filtrados(): PeriodoFerias[] {
    let lista = [...this.periodos];

    if (this.filtroStatus !== 'Todos') lista = lista.filter(p => p.status === this.filtroStatus);

    const term = this.searchTerm.trim().toLowerCase();
    if (term) {
      lista = lista.filter(p =>
        p.funcionario.toLowerCase().includes(term) ||
        p.matricula.toLowerCase().includes(term)
      );
    }

    return lista;
  }

  get paged(): PeriodoFerias[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtrados.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filtrados.length / this.pageSize);
  }

  get visiblePages(): number[] {
    const start = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    const end   = Math.min(this.totalPages, start + 4);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  get paginationStart(): number {
    return this.filtrados.length === 0 ? 0 : (this.page - 1) * this.pageSize + 1;
  }

  get paginationEnd(): number {
    return Math.min(this.page * this.pageSize, this.filtrados.length);
  }

  setPage(p: number): void {
    if (p < 1 || p > this.totalPages) return;
    this.page = p;
  }

  setFiltro(s: StatusFerias | 'Todos'): void {
    this.filtroStatus = s;
    this.page = 1;
  }

  openAdd(): void {
    this.submitted = false;
    this.form = this.emptyForm();
    this.feriasModal.show();
  }

  openDelete(p: PeriodoFerias): void {
    this.periodoParaExcluir = p;
    this.deleteModal.show();
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;
    // TODO: chamar serviço quando backend estiver pronto
    const newId = this.periodos.length ? Math.max(...this.periodos.map(p => p.id)) + 1 : 1;
    const inicio = new Date(this.form.dataInicio! + 'T00:00:00');
    const fim    = new Date(this.form.dataFim! + 'T00:00:00');
    const dias   = Math.ceil((fim.getTime() - inicio.getTime()) / 86400000) + 1;
    this.periodos.push({ ...this.form, id: newId, dias, status: 'Pendente' } as PeriodoFerias);
    this.feriasModal.hide();
    this.showSuccess('Período cadastrado com sucesso!');
  }

  confirmDelete(): void {
    if (!this.periodoParaExcluir) return;
    this.periodos = this.periodos.filter(p => p.id !== this.periodoParaExcluir!.id);
    this.periodoParaExcluir = null;
    this.deleteModal.hide();
    this.showSuccess('Registro removido com sucesso!');
  }

  statusBadge(status: StatusFerias): string {
    const map: Record<StatusFerias, string> = {
      'Pendente':   'bg-secondary',
      'Aprovada':   'bg-success',
      'Em Curso':   'bg-warning text-dark',
      'Concluída':  'bg-primary',
      'Recusada':   'bg-danger'
    };
    return map[status] ?? 'bg-secondary';
  }

  formatDate(iso: string): string {
    if (!iso) return '—';
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
  }

  private isFormValid(): boolean {
    return !!(
      this.form.funcionario?.trim() &&
      this.form.matricula?.trim() &&
      this.form.tipo &&
      this.form.dataInicio &&
      this.form.dataFim &&
      this.form.dataFim >= this.form.dataInicio
    );
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private emptyForm(): Partial<PeriodoFerias> {
    return { funcionario: '', matricula: '', setor: '', tipo: undefined, dataInicio: '', dataFim: '' };
  }
}
