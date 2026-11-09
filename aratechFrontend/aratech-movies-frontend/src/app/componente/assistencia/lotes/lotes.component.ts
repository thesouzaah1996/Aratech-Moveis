import { PermissaoDirective } from '../../../core/permissao.directive';
import { Component, AfterViewInit, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { criarModal, ModalDaTela } from '../../../core/ui/modal';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

import { LoteAssistencia, StatusLoteAssistencia, LoteAssistenciaService } from '../../../core/services/lote-assistencia.service';
import { HistoricoLotesComponent } from './historico-lotes.component';
export type { LoteAssistencia, StatusLoteAssistencia } from '../../../core/services/lote-assistencia.service';

@Component({
  selector: 'app-assistencia-lotes',
  standalone: true,
  imports: [PermissaoDirective,DatePipe, FormsModule, NavbarComponent, BreadcrumbComponent, HistoricoLotesComponent],
  templateUrl: './lotes.component.html',
  styleUrl: './lotes.component.scss'
})
export class AssistenciaLotesComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('viewModal') viewModalEl!: ElementRef<HTMLElement>;
  @ViewChild('pdfModal') pdfModalEl!: ElementRef<HTMLElement>;
  private viewModal?: ModalDaTela;
  private pdfModal?: ModalDaTela;
  loteDocumento: LoteAssistencia | null = null;
  loteSelecionado: LoteAssistencia | null = null;
  readonly statusDisponiveis: StatusLoteAssistencia[] = ['Não iniciado', 'Em andamento', 'Finalizado'];
  errorMessage = '';
  carregando = false;
  processando = false;
  constructor(private service: LoteAssistenciaService) {}
  ngOnInit(): void { this.carregar(); }
  carregar(): void {
    this.carregando = true;
    this.service.listar().subscribe({ next: lotes => { this.lotes = lotes; this.carregando = false; this.errorMessage = ''; }, error: () => { this.errorMessage = 'Não foi possível carregar os lotes. Tente novamente.'; this.carregando = false; } });
  }
  ngAfterViewInit(): void {
    this.viewModal = criarModal(this.viewModalEl.nativeElement);
    this.pdfModal = criarModal(this.pdfModalEl.nativeElement);
  }
  ngOnDestroy(): void { this.viewModal?.dispose(); this.pdfModal?.dispose(); }
  abrirPdf(lote: LoteAssistencia): void { this.loteDocumento = lote; this.pdfModal?.show(); }
  fecharPdf(): void { this.pdfModal?.hide(); }
  openView(lote: LoteAssistencia): void { this.loteSelecionado = lote; this.viewModal?.show(); }
  fecharVisualizacao(): void { this.viewModal?.hide(); }
  avancar(lote: LoteAssistencia): void {
    if (this.processando || lote.status === 'Finalizado') return;
    this.processando = true;
    this.service.avancar(lote.id, lote.status === 'Não iniciado' ? 'iniciar' : 'finalizar').subscribe({
      next: atualizado => { this.lotes = this.lotes.map(l => l.id === atualizado.id ? atualizado : l); this.processando = false; this.errorMessage = ''; this.showSuccess('Status do lote atualizado.'); },
      error: () => { this.processando = false; this.errorMessage = 'Não foi possível alterar o lote. Atualize a lista e tente novamente.'; }
    });
  }

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Assistência Técnica', route: '/assistencia' },
    { label: 'Lotes' }
  ];

  lotes: LoteAssistencia[] = [];
  searchTerm = '';
  filtroStatus: StatusLoteAssistencia | 'Todos' = 'Todos';
  page = 1;
  pageSize = 8;
  successMessage = '';

  get filtrados(): LoteAssistencia[] {
    let lista = this.lotes.filter(l => !l.carregadoEm);
    if (this.filtroStatus !== 'Todos') lista = lista.filter(l => l.status === this.filtroStatus);
    const term = this.searchTerm.trim().toLowerCase();
    if (term) lista = lista.filter(l =>
      l.codigo.toLowerCase().includes(term) ||
      (l.empresa ?? '').toLowerCase().includes(term)
    );
    return lista.sort((a, b) => a.dataEntrada.localeCompare(b.dataEntrada) || a.id - b.id);
  }

  get paged(): LoteAssistencia[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtrados.slice(start, start + this.pageSize);
  }

  get totalPages(): number { return Math.ceil(this.filtrados.length / this.pageSize); }
  get visiblePages(): number[] {
    const start = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    return Array.from({ length: Math.min(this.totalPages, start + 4) - start + 1 }, (_, i) => start + i);
  }
  get paginationStart(): number { return this.filtrados.length === 0 ? 0 : (this.page - 1) * this.pageSize + 1; }
  get paginationEnd(): number { return Math.min(this.page * this.pageSize, this.filtrados.length); }

  setPage(p: number): void { if (p >= 1 && p <= this.totalPages) this.page = p; }
  setFiltro(s: string): void { this.filtroStatus = s as StatusLoteAssistencia | 'Todos'; this.page = 1; }

  badgeClass(status: StatusLoteAssistencia): string {
    const map: Record<StatusLoteAssistencia, string> = {
      'Não iniciado': 'bg-secondary',
      'Finalizado':          'bg-success',
      'Em andamento':          'bg-info text-dark'
    };
    return map[status] ?? 'bg-secondary';
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }
}
