import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusNota = 'Pendente' | 'Vencida' | 'Paga' | 'Cancelada';

export interface NotaFiscal {
  id: number;
  numero: string;
  numeroRecebimento?: string;
  fornecedor: string;
  valor: number;
  dataEmissao: string;
  vencimento: string;
  pagamento?: string;
  status: StatusNota;
  observacoes?: string;
}

@Component({
  selector: 'app-baixa-notas',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, NavbarComponent, BreadcrumbComponent],
  templateUrl: './baixa-notas.component.html',
  styleUrl: './baixa-notas.component.scss'
})
export class BaixaNotasComponent implements AfterViewInit {
  @ViewChild('notaModal') notaModalEl!: ElementRef;
  @ViewChild('baixaModal') baixaModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Financeiro', route: '/financeiro' },
    { label: 'Baixa de Notas' }
  ];

  notas: NotaFiscal[] = [];
  isEditing = false;
  submitted = false;
  successMessage = '';
  notaParaExcluir: NotaFiscal | null = null;
  notaSelecionada: NotaFiscal | null = null;
  dataPagamento = '';

  form: Omit<NotaFiscal, 'id' | 'status'> = this.emptyForm();

  page = 1;
  readonly pageSize = 8;

  searchQuery = '';

  private notaModal?: any;
  private baixaModal?: any;
  private deleteModal?: any;

  ngAfterViewInit(): void {
    this.notaModal = new bootstrap.Modal(this.notaModalEl.nativeElement);
    this.baixaModal = new bootstrap.Modal(this.baixaModalEl.nativeElement);
    this.deleteModal = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  get filtered(): NotaFiscal[] {
    const q = this.searchQuery.toLowerCase();
    if (!q) return this.notas;
    return this.notas.filter(n =>
      n.numero.toLowerCase().includes(q) ||
      n.fornecedor.toLowerCase().includes(q) ||
      (n.numeroRecebimento ?? '').toLowerCase().includes(q) ||
      (n.observacoes ?? '').toLowerCase().includes(q)
    );
  }

  get paged(): NotaFiscal[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtered.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filtered.length / this.pageSize) || 1;
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  setPage(p: number): void {
    if (p >= 1 && p <= this.totalPages) this.page = p;
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.page = 1;
  }

  openAdd(): void {
    this.isEditing = false;
    this.submitted = false;
    this.form = this.emptyForm();
    this.notaModal.show();
  }

  openEdit(nota: NotaFiscal): void {
    this.isEditing = true;
    this.submitted = false;
    const { status, ...resto } = nota;
    this.form = { ...resto };
    this.notaModal.show();
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;

    const status: StatusNota = this.form.vencimento < new Date().toISOString().split('T')[0] ? 'Vencida' : 'Pendente';

    if (this.isEditing) {
      const idx = this.notas.findIndex(n => n.numero === this.form.numero);
      if (idx > -1) this.notas[idx] = { ...this.notas[idx], ...this.form, status };
    } else {
      const newId = this.notas.length ? Math.max(...this.notas.map(n => n.id)) + 1 : 1;
      this.notas.push({ ...this.form, id: newId, status });
    }

    this.notaModal.hide();
    this.showSuccess('Nota fiscal lançada com sucesso!');
  }

  openBaixa(nota: NotaFiscal): void {
    this.notaSelecionada = nota;
    this.dataPagamento = new Date().toISOString().split('T')[0];
    this.baixaModal.show();
  }

  confirmarBaixa(): void {
    if (!this.notaSelecionada) return;
    const idx = this.notas.findIndex(n => n.id === this.notaSelecionada!.id);
    if (idx > -1) {
      this.notas[idx].status = 'Paga';
      this.notas[idx].pagamento = this.dataPagamento;
    }
    this.baixaModal.hide();
    this.showSuccess(`Baixa da nota "${this.notaSelecionada.numero}" registrada!`);
    this.notaSelecionada = null;
  }

  openDelete(nota: NotaFiscal): void {
    this.notaParaExcluir = nota;
    this.deleteModal.show();
  }

  confirmDelete(): void {
    this.notas = this.notas.filter(n => n.id !== this.notaParaExcluir?.id);
    this.notaParaExcluir = null;
    this.deleteModal.hide();
  }

  badgeClass(status: StatusNota): string {
    const map: Record<StatusNota, string> = {
      'Pendente': 'bg-warning text-dark',
      'Vencida': 'bg-danger',
      'Paga': 'bg-success',
      'Cancelada': 'bg-secondary'
    };
    return map[status] ?? 'bg-secondary';
  }

  private isFormValid(): boolean {
    return !!(this.form.numero?.trim() && this.form.fornecedor?.trim() &&
              this.form.valor > 0 && this.form.dataEmissao && this.form.vencimento);
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private emptyForm(): Omit<NotaFiscal, 'id' | 'status'> {
    return { numero: '', numeroRecebimento: '', fornecedor: '', valor: 0, dataEmissao: '', vencimento: '', observacoes: '' };
  }
}
