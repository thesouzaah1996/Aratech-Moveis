import { Component, ElementRef, AfterViewInit, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { ChamadoService } from '../../../core/services/chamado.service';
import {
  Chamado,
  StatusChamado,
  PRIORIDADE_LABELS,
  TIPO_MANUTENCAO_LABELS
} from '../../../core/models/chamado.model';

declare const bootstrap: any;

const STATUS_FILA_LABELS: Record<StatusChamado, string> = {
  ABERTA: 'Aguardando',
  EM_MANUTENCAO: 'Em Manutenção',
  CONCLUIDA: 'Concluída'
};

@Component({
  selector: 'app-fila-chamados',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './fila-chamados.component.html',
  styleUrl: './fila-chamados.component.scss'
})
export class FilaChamadosComponent implements OnInit, AfterViewInit {
  @ViewChild('atribuirModal') atribuirModalEl!: ElementRef;
  @ViewChild('detalhesModal') detalhesModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Manutenção', route: '/manutencao' },
    { label: 'Fila de Chamados' }
  ];

  readonly tipoLabels = TIPO_MANUTENCAO_LABELS;
  readonly prioridadeLabels = PRIORIDADE_LABELS;
  readonly statusLabels = STATUS_FILA_LABELS;

  filtroStatus: StatusChamado | 'Todos' = 'Todos';
  searchTerm = '';
  page = 1;
  pageSize = 8;
  submitted = false;
  successMessage = '';
  errorMessage = '';
  mecanico = '';

  chamadoSelecionado: Chamado | null = null;

  private atribuirModal?: any;
  private detalhesModal?: any;

  chamados: Chamado[] = [];

  constructor(private chamadoService: ChamadoService) {}

  ngOnInit(): void {
    this.carregarChamados();
  }

  ngAfterViewInit(): void {
    this.atribuirModal = new bootstrap.Modal(this.atribuirModalEl.nativeElement);
    this.detalhesModal = new bootstrap.Modal(this.detalhesModalEl.nativeElement);
  }

  private carregarChamados(): void {
    this.chamadoService.getAll().subscribe({
      next: chamados => this.chamados = chamados,
      error: () => this.showError('Erro ao carregar a fila de chamados.')
    });
  }

  get filtrados(): Chamado[] {
    const ordem: Record<string, number> = { ALTA: 0, MEDIA: 1, BAIXA: 2 };
    let lista = [...this.chamados].sort((a, b) => ordem[a.prioridade] - ordem[b.prioridade]);

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

  get paged(): Chamado[] {
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

  openAtribuir(chamado: Chamado): void {
    this.submitted = false;
    this.mecanico = chamado.mecanico ?? '';
    this.chamadoSelecionado = chamado;
    this.atribuirModal.show();
  }

  openDetalhes(chamado: Chamado): void {
    this.chamadoSelecionado = chamado;
    this.detalhesModal.show();
  }

  abrirAtribuirDoDetalhes(chamado: Chamado): void {
    this.detalhesModal.hide();
    setTimeout(() => this.openAtribuir(chamado), 300);
  }

  confirmarAtribuicao(): void {
    this.submitted = true;
    if (!this.mecanico.trim() || !this.chamadoSelecionado) return;

    this.chamadoService.atribuirMecanico(this.chamadoSelecionado.id, this.mecanico.trim()).subscribe({
      next: atualizado => {
        const idx = this.chamados.findIndex(c => c.id === atualizado.id);
        if (idx > -1) this.chamados[idx] = atualizado;

        this.atribuirModal.hide();
        this.chamadoSelecionado = null;
        this.mecanico = '';
        this.submitted = false;

        this.showSuccess('Chamado atribuído com sucesso!');
      },
      error: (err) => {
        this.atribuirModal.hide();
        this.showError(err.error?.message ?? 'Erro ao atribuir mecânico.');
      }
    });
  }

  statusBadge(status: StatusChamado): string {
    if (status === 'CONCLUIDA')      return 'bg-success';
    if (status === 'EM_MANUTENCAO')  return 'bg-warning text-dark';
    return 'bg-secondary';
  }

  prioridadeClass(p: Chamado['prioridade']): string {
    if (p === 'ALTA')  return 'badge-prioridade alta';
    if (p === 'MEDIA') return 'badge-prioridade media';
    return 'badge-prioridade baixa';
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    this.errorMessage = '';
    setTimeout(() => (this.successMessage = ''), 3500);
  }

  private showError(msg: string): void {
    this.errorMessage = msg;
    this.successMessage = '';
  }
}
