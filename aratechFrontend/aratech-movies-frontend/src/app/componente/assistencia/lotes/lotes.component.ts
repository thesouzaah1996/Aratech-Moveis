import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

export type StatusLoteAssistencia = 'Aguardando Análise' | 'Em Reparo' | 'Concluído' | 'Devolvido';

export interface LoteAssistencia {
  id: number;
  codigo: string;
  produto: string;
  quantidade: number;
  dataEntrada: string;
  responsavel: string;
  defeito: string;
  status: StatusLoteAssistencia;
}

@Component({
  selector: 'app-assistencia-lotes',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './lotes.component.html',
  styleUrl: './lotes.component.scss'
})
export class AssistenciaLotesComponent {
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
    let lista = [...this.lotes];
    if (this.filtroStatus !== 'Todos') lista = lista.filter(l => l.status === this.filtroStatus);
    const term = this.searchTerm.trim().toLowerCase();
    if (term) lista = lista.filter(l =>
      l.codigo.toLowerCase().includes(term) ||
      l.produto.toLowerCase().includes(term) ||
      l.defeito.toLowerCase().includes(term)
    );
    return lista;
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

  avancarStatus(lote: LoteAssistencia): void {
    const fluxo: StatusLoteAssistencia[] = ['Aguardando Análise', 'Em Reparo', 'Concluído'];
    const idx = fluxo.indexOf(lote.status);
    if (idx < fluxo.length - 1) {
      lote.status = fluxo[idx + 1];
      this.showSuccess(`Lote ${lote.codigo} avançado para "${lote.status}".`);
    }
  }

  devolver(lote: LoteAssistencia): void {
    lote.status = 'Devolvido';
    this.showSuccess(`Lote ${lote.codigo} marcado como devolvido.`);
  }

  badgeClass(status: StatusLoteAssistencia): string {
    const map: Record<StatusLoteAssistencia, string> = {
      'Aguardando Análise': 'bg-secondary',
      'Em Reparo':          'bg-warning text-dark',
      'Concluído':          'bg-success',
      'Devolvido':          'bg-info text-dark'
    };
    return map[status] ?? 'bg-secondary';
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }
}
