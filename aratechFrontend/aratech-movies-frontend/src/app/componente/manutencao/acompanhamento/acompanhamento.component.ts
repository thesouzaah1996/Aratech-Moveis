import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

export type StatusOrdem = 'Aberta' | 'Em Manutenção' | 'Concluída';
export type Prioridade   = 'Alta'  | 'Média'          | 'Baixa';

export interface OrdemAcompanhamento {
  id: number;
  equipamento: string;
  tipo: string;
  prioridade: Prioridade;
  solicitante: string;
  setor: string;
  mecanico: string;
  status: StatusOrdem;
  data: string;
}

const PRIORIDADE_ORDEM: Record<Prioridade, number> = { Alta: 0, Média: 1, Baixa: 2 };

@Component({
  selector: 'app-acompanhamento',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './acompanhamento.component.html',
  styleUrl: './acompanhamento.component.scss'
})
export class AcompanhamentoComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Manutenção', route: '/manutencao' },
    { label: 'Acompanhamento' }
  ];

  filtroStatus: StatusOrdem | 'Todos' = 'Todos';
  searchTerm = '';
  page = 1;
  pageSize = 8;

  ordens: OrdemAcompanhamento[] = [];

  get filtradas(): OrdemAcompanhamento[] {
    let lista = [...this.ordens].sort((a, b) =>
      PRIORIDADE_ORDEM[a.prioridade] - PRIORIDADE_ORDEM[b.prioridade]
    );

    if (this.filtroStatus !== 'Todos') {
      lista = lista.filter(o => o.status === this.filtroStatus);
    }

    const term = this.searchTerm.trim().toLowerCase();
    if (term) {
      lista = lista.filter(o =>
        o.equipamento.toLowerCase().includes(term) ||
        o.solicitante.toLowerCase().includes(term) ||
        o.id.toString().includes(term)
      );
    }

    return lista;
  }

  get paged(): OrdemAcompanhamento[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtradas.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filtradas.length / this.pageSize);
  }

  get visiblePages(): number[] {
    const start = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    const end   = Math.min(this.totalPages, start + 4);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  get paginationStart(): number {
    return this.filtradas.length === 0 ? 0 : (this.page - 1) * this.pageSize + 1;
  }

  get paginationEnd(): number {
    return Math.min(this.page * this.pageSize, this.filtradas.length);
  }

  setPage(p: number): void {
    if (p < 1 || p > this.totalPages) return;
    this.page = p;
  }

  setFiltro(status: StatusOrdem | 'Todos'): void {
    this.filtroStatus = status;
    this.page = 1;
  }

  statusBadge(status: StatusOrdem): string {
    if (status === 'Concluída')      return 'bg-success';
    if (status === 'Em Manutenção')  return 'bg-warning text-dark';
    return 'bg-secondary';
  }

  prioridadeClass(p: Prioridade): string {
    if (p === 'Alta')  return 'badge-prioridade alta';
    if (p === 'Média') return 'badge-prioridade media';
    return 'badge-prioridade baixa';
  }

}
