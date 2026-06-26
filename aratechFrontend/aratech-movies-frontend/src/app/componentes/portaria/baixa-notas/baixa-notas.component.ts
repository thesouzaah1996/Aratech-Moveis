import { Component, ElementRef, AfterViewInit, OnInit, ViewChild } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusNota = 'Pendente' | 'Baixada';

export interface NotaFiscal {
  id: number;
  numero: string;
  empresa: string;
  valor: number;
  dataEmissao: string;
  status: StatusNota;
  observacoes?: string;
}

@Component({
  selector: 'app-baixa-notas',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './baixa-notas.component.html',
  styleUrl: './baixa-notas.component.scss'
})
export class BaixaNotasComponent implements OnInit, AfterViewInit {
  @ViewChild('notaModal') notaModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Portaria', route: '/portaria' },
    { label: 'Baixa de Notas' }
  ];

  notas: NotaFiscal[] = [];
  isEditing = false;
  notaParaExcluir: NotaFiscal | null = null;

  form: Omit<NotaFiscal, 'id'> = this.emptyForm();

  page = 1;
  readonly pageSize = 8;

  searchQuery = '';

  private notaModal?: any;
  private deleteModal?: any;

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.notaModal = new bootstrap.Modal(this.notaModalEl.nativeElement);
    this.deleteModal = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  get filtered(): NotaFiscal[] {
    const q = this.searchQuery.toLowerCase();
    if (!q) return this.notas;
    return this.notas.filter(n =>
      n.numero.toLowerCase().includes(q) ||
      n.empresa.toLowerCase().includes(q) ||
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
    this.form = this.emptyForm();
    this.notaModal.show();
  }

  openEdit(nota: NotaFiscal): void {
    this.isEditing = true;
    this.form = { ...nota };
    this.notaModal.show();
  }

  openDelete(nota: NotaFiscal): void {
    this.notaParaExcluir = nota;
    this.deleteModal.show();
  }

  save(): void {
    this.notaModal.hide();
  }

  baixarNota(nota: NotaFiscal): void {
    nota.status = 'Baixada';
  }

  confirmDelete(): void {
    this.notas = this.notas.filter(n => n.id !== this.notaParaExcluir?.id);
    this.notaParaExcluir = null;
    this.deleteModal.hide();
  }

  badgeClass(status: StatusNota): string {
    return status === 'Baixada' ? 'bg-success' : 'bg-warning text-dark';
  }

  private emptyForm(): Omit<NotaFiscal, 'id'> {
    return { numero: '', empresa: '', valor: 0, dataEmissao: '', status: 'Pendente', observacoes: '' };
  }
}
