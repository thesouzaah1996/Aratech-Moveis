import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusCotacao = 'Em Andamento' | 'Aguardando Resposta' | 'Finalizada' | 'Cancelada';

export interface Cotacao {
  id: number;
  numero: string;
  item: string;
  quantidade: number;
  fornecedor1: string;
  valor1: number;
  fornecedor2: string;
  valor2: number;
  fornecedor3: string;
  valor3: number;
  dataCriacao: string;
  status: StatusCotacao;
  fornecedorEscolhido?: string;
}

@Component({
  selector: 'app-cotacoes',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './cotacoes.component.html',
  styleUrl: './cotacoes.component.scss'
})
export class CotacoesComponent implements AfterViewInit {
  @ViewChild('cotacaoModal') cotacaoModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Compras', route: '/compras' },
    { label: 'Cotações' }
  ];

  cotacoes: Cotacao[] = [];
  searchTerm = '';
  page = 1;
  pageSize = 8;
  submitted = false;
  successMessage = '';

  form: Partial<Cotacao> = this.emptyForm();
  private cotacaoModal?: any;

  ngAfterViewInit(): void {
    this.cotacaoModal = new bootstrap.Modal(this.cotacaoModalEl.nativeElement);
  }

  get filtradas(): Cotacao[] {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) return this.cotacoes;
    return this.cotacoes.filter(c =>
      c.numero.toLowerCase().includes(term) ||
      c.item.toLowerCase().includes(term)
    );
  }

  get paged(): Cotacao[] {
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

  openAdd(): void {
    this.submitted = false;
    this.form = this.emptyForm();
    this.cotacaoModal.show();
  }

  save(): void {
    this.submitted = true;
    if (!this.form.item?.trim() || !this.form.quantidade) return;
    const newId = this.cotacoes.length ? Math.max(...this.cotacoes.map(c => c.id)) + 1 : 1;
    const num = `COT-${String(newId).padStart(4, '0')}`;
    this.cotacoes.push({ ...this.form, id: newId, numero: num, status: 'Em Andamento', dataCriacao: new Date().toLocaleDateString('pt-BR') } as Cotacao);
    this.cotacaoModal.hide();
    this.showSuccess('Cotação criada com sucesso!');
  }

  escolher(cot: Cotacao, fornecedor: string): void {
    cot.fornecedorEscolhido = fornecedor;
    cot.status = 'Finalizada';
    this.showSuccess(`Fornecedor "${fornecedor}" escolhido para cotação ${cot.numero}.`);
  }

  menorPreco(cot: Cotacao): string {
    const opcoes = [
      { f: cot.fornecedor1, v: cot.valor1 },
      { f: cot.fornecedor2, v: cot.valor2 },
      { f: cot.fornecedor3, v: cot.valor3 }
    ].filter(o => o.f && o.v > 0);
    if (!opcoes.length) return '';
    return opcoes.reduce((a, b) => a.v < b.v ? a : b).f;
  }

  badgeClass(status: StatusCotacao): string {
    const map: Record<StatusCotacao, string> = {
      'Em Andamento':        'bg-warning text-dark',
      'Aguardando Resposta': 'bg-info text-dark',
      'Finalizada':          'bg-success',
      'Cancelada':           'bg-secondary'
    };
    return map[status] ?? 'bg-secondary';
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private emptyForm(): Partial<Cotacao> {
    return { item: '', quantidade: 1, fornecedor1: '', valor1: 0, fornecedor2: '', valor2: 0, fornecedor3: '', valor3: 0 };
  }
}
