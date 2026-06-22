import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusFila = 'Aguardando' | 'Em Manutenção' | 'Concluída';
export type Prioridade  = 'Alta' | 'Média' | 'Baixa';

export interface ChamadoFila {
  id: number;
  equipamento: string;
  tipo: string;
  prioridade: Prioridade;
  solicitante: string;
  setor: string;
  descricao: string;
  mecanico: string;
  status: StatusFila;
  data: string;
}

const PRIORIDADE_ORDEM: Record<Prioridade, number> = { Alta: 0, Média: 1, Baixa: 2 };

@Component({
  selector: 'app-fila-chamados',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './fila-chamados.component.html',
  styleUrl: './fila-chamados.component.scss'
})
export class FilaChamadosComponent implements AfterViewInit {
  @ViewChild('atribuirModal') atribuirModalEl!: ElementRef;
  @ViewChild('detalhesModal') detalhesModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Manutenção', route: '/manutencao' },
    { label: 'Fila de Chamados' }
  ];

  filtroStatus: StatusFila | 'Todos' = 'Todos';
  searchTerm = '';
  page = 1;
  pageSize = 8;
  submitted = false;
  successMessage = '';
  mecanico = '';

  chamadoSelecionado: ChamadoFila | null = null;

  private atribuirModal?: any;
  private detalhesModal?: any;

  // TODO: carregar via serviço quando backend estiver pronto
  chamados: ChamadoFila[] = [];

  ngAfterViewInit(): void {
    this.atribuirModal = new bootstrap.Modal(this.atribuirModalEl.nativeElement);
    this.detalhesModal = new bootstrap.Modal(this.detalhesModalEl.nativeElement);
  }

  get filtrados(): ChamadoFila[] {
    let lista = [...this.chamados].sort((a, b) =>
      PRIORIDADE_ORDEM[a.prioridade] - PRIORIDADE_ORDEM[b.prioridade]
    );

    if (this.filtroStatus !== 'Todos') {
      lista = lista.filter(c => c.status === this.filtroStatus);
    }

    const term = this.searchTerm.trim().toLowerCase();
    if (term) {
      lista = lista.filter(c =>
        c.equipamento.toLowerCase().includes(term) ||
        c.solicitante.toLowerCase().includes(term) ||
        c.id.toString().includes(term)
      );
    }

    return lista;
  }

  get paged(): ChamadoFila[] {
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

  setFiltro(status: StatusFila | 'Todos'): void {
    this.filtroStatus = status;
    this.page = 1;
  }

  openAtribuir(chamado: ChamadoFila): void {
    this.submitted = false;
    this.mecanico = chamado.mecanico;
    this.chamadoSelecionado = chamado;
    this.atribuirModal.show();
  }

  openDetalhes(chamado: ChamadoFila): void {
    this.chamadoSelecionado = chamado;
    this.detalhesModal.show();
  }

  abrirAtribuirDoDetalhes(chamado: ChamadoFila): void {
    this.detalhesModal.hide();
    setTimeout(() => this.openAtribuir(chamado), 300);
  }

  confirmarAtribuicao(): void {
    this.submitted = true;
    if (!this.mecanico.trim() || !this.chamadoSelecionado) return;

    // TODO: chamar serviço quando backend estiver pronto
    const idx = this.chamados.findIndex(c => c.id === this.chamadoSelecionado!.id);
    if (idx > -1) {
      this.chamados[idx].mecanico = this.mecanico.trim();
      this.chamados[idx].status   = 'Em Manutenção';
    }

    this.atribuirModal.hide();
    this.chamadoSelecionado = null;
    this.mecanico = '';
    this.submitted = false;

    this.showSuccess('Chamado atribuído com sucesso!');
  }

  statusBadge(status: StatusFila): string {
    if (status === 'Concluída')     return 'bg-success';
    if (status === 'Em Manutenção') return 'bg-warning text-dark';
    return 'bg-secondary';
  }

  prioridadeClass(p: Prioridade): string {
    if (p === 'Alta')  return 'badge-prioridade alta';
    if (p === 'Média') return 'badge-prioridade media';
    return 'badge-prioridade baixa';
  }


  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3500);
  }
}
