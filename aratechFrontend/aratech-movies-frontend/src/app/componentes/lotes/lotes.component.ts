import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../navbar/navbar.component';
import { FooterComponent } from '../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusLote = 'Em Produção' | 'Finalizado' | 'Enviado p/ Assistência' | 'Enviado p/ Almoxarifado';

export interface Lote {
  id: number;
  codigo: string;
  produto: string;
  quantidade: number;
  dataProducao: string;
  responsavel: string;
  status: StatusLote;
}

@Component({
  selector: 'app-lotes',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './lotes.component.html',
  styleUrl: './lotes.component.scss'
})
export class LotesComponent implements AfterViewInit {
  @ViewChild('despachoModal') despachoModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Administração', route: '/administracao' },
    { label: 'Lotes' }
  ];

  lotes: Lote[] = [];
  searchTerm = '';
  filtroStatus: StatusLote | 'Todos' = 'Todos';
  page = 1;
  pageSize = 8;
  successMessage = '';
  loteSelecionado: Lote | null = null;
  destinoDespacho: 'assistencia' | 'almoxarifado' | null = null;

  private despachoModal?: any;

  ngAfterViewInit(): void {
    this.despachoModal = new bootstrap.Modal(this.despachoModalEl.nativeElement);
  }

  get filtrados(): Lote[] {
    let lista = [...this.lotes];
    if (this.filtroStatus !== 'Todos') lista = lista.filter(l => l.status === this.filtroStatus);
    const term = this.searchTerm.trim().toLowerCase();
    if (term) lista = lista.filter(l =>
      l.codigo.toLowerCase().includes(term) ||
      l.produto.toLowerCase().includes(term)
    );
    return lista;
  }

  get paged(): Lote[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtrados.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filtrados.length / this.pageSize);
  }

  get visiblePages(): number[] {
    const start = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    const end = Math.min(this.totalPages, start + 4);
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

  setFiltro(status: string): void {
    this.filtroStatus = status as StatusLote | 'Todos';
    this.page = 1;
  }

  abrirDespacho(lote: Lote, destino: 'assistencia' | 'almoxarifado'): void {
    this.loteSelecionado = lote;
    this.destinoDespacho = destino;
    this.despachoModal.show();
  }

  confirmarDespacho(): void {
    if (!this.loteSelecionado || !this.destinoDespacho) return;
    const novoStatus: StatusLote = this.destinoDespacho === 'assistencia'
      ? 'Enviado p/ Assistência'
      : 'Enviado p/ Almoxarifado';
    const idx = this.lotes.findIndex(l => l.id === this.loteSelecionado!.id);
    if (idx > -1) this.lotes[idx].status = novoStatus;
    this.despachoModal.hide();
    const destLabel = this.destinoDespacho === 'assistencia' ? 'Assistência Técnica' : 'Almoxarifado';
    this.showSuccess(`Lote ${this.loteSelecionado.codigo} enviado para ${destLabel}!`);
    this.loteSelecionado = null;
    this.destinoDespacho = null;
  }

  podeDespachar(lote: Lote): boolean {
    return lote.status === 'Finalizado';
  }

  badgeClass(status: StatusLote): string {
    const map: Record<StatusLote, string> = {
      'Em Produção':             'bg-warning text-dark',
      'Finalizado':              'bg-success',
      'Enviado p/ Assistência':  'bg-info text-dark',
      'Enviado p/ Almoxarifado': 'bg-primary',
    };
    return map[status] ?? 'bg-secondary';
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3500);
  }
}
