import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusReceber = 'Pendente' | 'Vencida' | 'Recebida' | 'Cancelada';

export interface ContaReceber {
  id: number;
  descricao: string;
  cliente: string;
  valor: number;
  vencimento: string;
  recebimento?: string;
  status: StatusReceber;
}

@Component({
  selector: 'app-contas-receber',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './contas-receber.component.html',
  styleUrl: './contas-receber.component.scss'
})
export class ContasReceberComponent implements AfterViewInit {
  @ViewChild('contaModal') contaModalEl!: ElementRef;
  @ViewChild('receberModal') receberModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Financeiro', route: '/financeiro' },
    { label: 'Contas a Receber' }
  ];

  contas: ContaReceber[] = [];
  searchTerm = '';
  filtroStatus: StatusReceber | 'Todos' = 'Todos';
  page = 1;
  pageSize = 8;
  submitted = false;
  successMessage = '';

  form: Partial<ContaReceber> = this.emptyForm();
  contaSelecionada: ContaReceber | null = null;
  dataRecebimento = '';

  private contaModal?: any;
  private receberModal?: any;

  ngAfterViewInit(): void {
    this.contaModal   = new bootstrap.Modal(this.contaModalEl.nativeElement);
    this.receberModal = new bootstrap.Modal(this.receberModalEl.nativeElement);
  }

  get filtradas(): ContaReceber[] {
    let lista = [...this.contas];
    if (this.filtroStatus !== 'Todos') lista = lista.filter(c => c.status === this.filtroStatus);
    const term = this.searchTerm.trim().toLowerCase();
    if (term) lista = lista.filter(c =>
      c.descricao.toLowerCase().includes(term) ||
      c.cliente.toLowerCase().includes(term)
    );
    return lista;
  }

  get paged(): ContaReceber[] {
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
  get totalPendente(): number { return this.contas.filter(c => c.status === 'Pendente' || c.status === 'Vencida').reduce((s, c) => s + c.valor, 0); }

  setPage(p: number): void { if (p >= 1 && p <= this.totalPages) this.page = p; }
  setFiltro(s: string): void { this.filtroStatus = s as StatusReceber | 'Todos'; this.page = 1; }

  openAdd(): void {
    this.submitted = false;
    this.form = this.emptyForm();
    this.contaModal.show();
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;
    const newId = this.contas.length ? Math.max(...this.contas.map(c => c.id)) + 1 : 1;
    const status: StatusReceber = this.form.vencimento! < new Date().toISOString().split('T')[0] ? 'Vencida' : 'Pendente';
    this.contas.push({ ...this.form, id: newId, status } as ContaReceber);
    this.contaModal.hide();
    this.showSuccess('Título registrado com sucesso!');
  }

  openReceber(c: ContaReceber): void {
    this.contaSelecionada = c;
    this.dataRecebimento = new Date().toISOString().split('T')[0];
    this.receberModal.show();
  }

  confirmarRecebimento(): void {
    if (!this.contaSelecionada) return;
    const idx = this.contas.findIndex(c => c.id === this.contaSelecionada!.id);
    if (idx > -1) {
      this.contas[idx].status      = 'Recebida';
      this.contas[idx].recebimento = this.dataRecebimento;
    }
    this.receberModal.hide();
    this.showSuccess(`Recebimento de "${this.contaSelecionada.descricao}" registrado!`);
    this.contaSelecionada = null;
  }

  badgeClass(status: StatusReceber): string {
    const map: Record<StatusReceber, string> = {
      'Pendente':   'bg-warning text-dark',
      'Vencida':    'bg-danger',
      'Recebida':   'bg-success',
      'Cancelada':  'bg-secondary'
    };
    return map[status] ?? 'bg-secondary';
  }

  private isFormValid(): boolean {
    return !!(this.form.descricao?.trim() && this.form.cliente?.trim() &&
              this.form.valor && this.form.valor > 0 && this.form.vencimento);
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private emptyForm(): Partial<ContaReceber> {
    return { descricao: '', cliente: '', valor: 0, vencimento: '' };
  }
}
