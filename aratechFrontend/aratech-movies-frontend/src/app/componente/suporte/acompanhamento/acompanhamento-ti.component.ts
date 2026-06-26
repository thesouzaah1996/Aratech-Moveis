import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

export type StatusChamado = 'Aberto' | 'Em Atendimento' | 'Concluído' | 'Cancelado';
export type PrioridadeTi  = 'Baixa'  | 'Média'          | 'Alta'      | 'Crítica';

export interface ChamadoTi {
  id: string;
  tipo: string;
  prioridade: PrioridadeTi;
  titulo: string;
  solicitante: string;
  setor: string;
  status: StatusChamado;
  data: string;
}

const PRIORIDADE_ORDEM: Record<PrioridadeTi, number> = { Crítica: 0, Alta: 1, Média: 2, Baixa: 3 };

@Component({
  selector: 'app-acompanhamento-ti',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './acompanhamento-ti.component.html',
  styleUrl: './acompanhamento-ti.component.scss'
})
export class AcompanhamentoTiComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Suporte de TI', route: '/suporte' },
    { label: 'Acompanhamento' }
  ];

  filtroStatus: StatusChamado | 'Todos' = 'Todos';
  searchTerm = '';
  page = 1;
  readonly pageSize = 8;

  chamados: ChamadoTi[] = [];

  get filtrados(): ChamadoTi[] {
    let lista = [...this.chamados].sort((a, b) =>
      PRIORIDADE_ORDEM[a.prioridade] - PRIORIDADE_ORDEM[b.prioridade]
    );

    if (this.filtroStatus !== 'Todos') {
      lista = lista.filter(c => c.status === this.filtroStatus);
    }

    const term = this.searchTerm.trim().toLowerCase();
    if (term) {
      lista = lista.filter(c =>
        c.id.toLowerCase().includes(term) ||
        c.titulo.toLowerCase().includes(term) ||
        c.solicitante.toLowerCase().includes(term) ||
        c.tipo.toLowerCase().includes(term)
      );
    }

    return lista;
  }

  get paged(): ChamadoTi[] {
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

  setFiltro(status: StatusChamado | 'Todos'): void {
    this.filtroStatus = status;
    this.page = 1;
  }

  statusBadge(status: StatusChamado): string {
    if (status === 'Concluído')       return 'bg-success';
    if (status === 'Em Atendimento')  return 'bg-warning text-dark';
    if (status === 'Cancelado')       return 'bg-secondary';
    return 'bg-danger';
  }

  prioridadeClass(p: PrioridadeTi): string {
    if (p === 'Crítica') return 'badge-prioridade critica';
    if (p === 'Alta')    return 'badge-prioridade alta';
    if (p === 'Média')   return 'badge-prioridade media';
    return 'badge-prioridade baixa';
  }
}
