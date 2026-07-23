import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { ChamadoService } from '../../../core/services/chamado.service';
import {
  Chamado,
  StatusChamado,
  PRIORIDADE_LABELS,
  STATUS_CHAMADO_LABELS,
  TIPO_MANUTENCAO_LABELS
} from '../../../core/models/chamado.model';

const PRIORIDADE_ORDEM: Record<string, number> = { ALTA: 0, MEDIA: 1, BAIXA: 2 };

@Component({
  selector: 'app-acompanhamento',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './acompanhamento.component.html',
  styleUrl: './acompanhamento.component.scss'
})
export class AcompanhamentoComponent implements OnInit {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Manutenção', route: '/manutencao' },
    { label: 'Acompanhamento' }
  ];

  readonly tipoLabels = TIPO_MANUTENCAO_LABELS;
  readonly prioridadeLabels = PRIORIDADE_LABELS;
  readonly statusLabels = STATUS_CHAMADO_LABELS;

  filtroStatus: StatusChamado | 'Todos' = 'Todos';
  searchTerm = '';
  page = 1;
  pageSize = 8;
  errorMessage = '';

  ordens: Chamado[] = [];

  constructor(private chamadoService: ChamadoService) {}

  ngOnInit(): void {
    this.chamadoService.getAll().subscribe({
      next: ordens => this.ordens = ordens,
      error: () => this.errorMessage = 'Erro ao carregar as ordens de manutenção.'
    });
  }

  get filtradas(): Chamado[] {
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

  get paged(): Chamado[] {
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

  setFiltro(status: StatusChamado | 'Todos'): void {
    this.filtroStatus = status;
    this.page = 1;
  }

  statusBadge(status: StatusChamado): string {
    if (status === 'CONCLUIDA')     return 'bg-success';
    if (status === 'EM_MANUTENCAO') return 'bg-warning text-dark';
    return 'bg-secondary';
  }

  prioridadeClass(p: Chamado['prioridade']): string {
    if (p === 'ALTA')  return 'badge-prioridade alta';
    if (p === 'MEDIA') return 'badge-prioridade media';
    return 'badge-prioridade baixa';
  }

}
