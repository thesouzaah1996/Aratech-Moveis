import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

import { DatePipe } from '@angular/common';
import { Subject, Subscription, debounceTime, interval } from 'rxjs';
import { ChamadoTiService } from '../../../core/services/chamado-ti.service';
import { ChamadoTi, PrioridadeTi, StatusChamadoTi } from '../../../core/models/chamado-ti.model';

@Component({
  selector: 'app-acompanhamento-ti',
  standalone: true,
  imports: [DatePipe, FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './acompanhamento-ti.component.html',
  styleUrl: './acompanhamento-ti.component.scss'
})
export class AcompanhamentoTiComponent implements OnInit, OnDestroy {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Suporte de TI', route: '/suporte' },
    { label: 'Acompanhamento' }
  ];

  filtroStatus: StatusChamadoTi | 'Todos' = 'Todos';
  searchTerm = '';
  page = 1;
  readonly pageSize = 8;

  chamados: ChamadoTi[] = [];

  total = 0;
  loading = false;
  errorMessage = '';
  private request?: Subscription;
  private polling?: Subscription;
  private searchRequest?: Subscription;
  private search = new Subject<void>();
  constructor(private readonly chamadoTiService: ChamadoTiService) {}
  ngOnInit(): void {
    this.carregar();
    this.searchRequest = this.search.pipe(debounceTime(300)).subscribe(() => this.carregar());
    this.polling = interval(5000).subscribe(() => { if (!this.loading) this.carregar(); });
  }
  ngOnDestroy(): void { this.request?.unsubscribe(); this.polling?.unsubscribe(); this.searchRequest?.unsubscribe(); this.search.complete(); }
  carregar(): void {
    this.request?.unsubscribe(); this.loading = true;
    this.request = this.chamadoTiService.getPagina(this.page-1,this.pageSize,this.filtroStatus === 'Todos' ? undefined : this.filtroStatus,this.searchTerm).subscribe({
      next: p => {
        this.total=p.total;this.loading=false;this.errorMessage='';
        const ultima=Math.max(1,Math.ceil(p.total/this.pageSize));
        if(this.page>ultima){this.page=ultima;this.carregar();return;}
        this.chamados=p.chamados;
      },
      error: e => {this.loading=false;this.errorMessage=e.error?.mensagem || 'Nao foi possivel atualizar os chamados. Os dados exibidos podem estar desatualizados.';}
    });
  }
  onSearch(): void {this.page=1;this.search.next();}
  get paged(): ChamadoTi[] {return this.chamados;}

  get totalPages(): number {
    return Math.ceil(this.total / this.pageSize);
  }

  get visiblePages(): number[] {
    const start = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    const end   = Math.min(this.totalPages, start + 4);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  get paginationStart(): number {
    return this.total === 0 ? 0 : (this.page - 1) * this.pageSize + 1;
  }

  get paginationEnd(): number {
    return Math.min(this.page * this.pageSize, this.total);
  }

  setPage(p: number): void {
    if (this.loading || p < 1 || p > this.totalPages) return;
    this.page = p; this.carregar();
  }

  setFiltro(status: StatusChamadoTi | 'Todos'): void {
    this.filtroStatus = status;
    this.page = 1; this.carregar();
  }

  statusBadge(status: StatusChamadoTi): string {
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
