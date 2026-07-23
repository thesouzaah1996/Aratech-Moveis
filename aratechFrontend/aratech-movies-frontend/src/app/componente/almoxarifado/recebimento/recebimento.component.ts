import { Component, ElementRef, AfterViewInit, OnInit, ViewChild } from '@angular/core';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { RecebimentoService } from '../../../core/services/recebimento.service';
import { Recebimento, STATUS_RECEBIMENTO_LABELS, StatusRecebimento } from '../../../core/models/recebimento.model';

declare const bootstrap: any;

@Component({
  selector: 'app-recebimento',
  standalone: true,
  imports: [NavbarComponent, BreadcrumbComponent],
  templateUrl: './recebimento.component.html',
  styleUrl: './recebimento.component.scss'
})
export class RecebimentoComponent implements OnInit, AfterViewInit {
  @ViewChild('historicoModal') historicoModalEl!: ElementRef;
  @ViewChild('confirmAcaoModal') confirmAcaoModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Almoxarifado', route: '/almoxarifado' },
    { label: 'Recebimento' }
  ];

  readonly statusLabels = STATUS_RECEBIMENTO_LABELS;

  todos: Recebimento[] = [];
  historico: Recebimento[] = [];
  successMessage = '';
  errorMessage = '';

  itemParaConfirmar: Recebimento | null = null;
  acaoParaConfirmar: 'autorizar' | 'finalizar' | null = null;

  page = 1;
  pageSize = 8;

  private historicoModal?: any;
  private confirmAcaoModal?: any;

  constructor(private readonly recebimentoService: RecebimentoService) {}

  ngOnInit(): void {
    this.carregarTodos();
  }

  ngAfterViewInit(): void {
    this.historicoModal = new bootstrap.Modal(this.historicoModalEl.nativeElement);
    this.confirmAcaoModal = new bootstrap.Modal(this.confirmAcaoModalEl.nativeElement);
  }

  private carregarTodos(): void {
    this.recebimentoService.getTodos().subscribe({
      next: todos => (this.todos = todos),
      error: () => this.showError('Erro ao carregar os recebimentos.')
    });
  }

  private carregarHistorico(): void {
    this.recebimentoService.getHistorico().subscribe({
      next: historico => (this.historico = historico),
      error: () => this.showError('Erro ao carregar o histórico de recebimentos.')
    });
  }

  get fila(): Recebimento[] {
    return this.todos.filter(item => item.statusRecebimento !== 'FINALIZADO');
  }

  get filaPaginada(): Recebimento[] {
    const start = (this.page - 1) * this.pageSize;
    return this.fila.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.fila.length / this.pageSize);
  }

  get visiblePages(): number[] {
    const half = 5;
    const start = Math.max(1, Math.min(this.page - half, this.totalPages - 7));
    const end = Math.min(this.totalPages, start + 7);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  get paginationStart(): number {
    return this.fila.length === 0 ? 0 : (this.page - 1) * this.pageSize + 1;
  }

  get paginationEnd(): number {
    return Math.min(this.page * this.pageSize, this.fila.length);
  }

  setPage(p: number): void {
    if (p < 1 || p > this.totalPages) return;
    this.page = p;
  }

  abrirHistorico(): void {
    this.carregarHistorico();
    this.historicoModal.show();
  }

  get tituloConfirmacao(): string {
    return this.acaoParaConfirmar === 'autorizar' ? 'Autorizar Liberação' : 'Finalizar Recebimento';
  }

  get mensagemConfirmacao(): string {
    return this.acaoParaConfirmar === 'autorizar'
      ? 'Deseja realmente autorizar a liberação desta carga para recebimento?'
      : 'Deseja realmente finalizar este recebimento? Ele será movido para o histórico.';
  }

  get iconeConfirmacao(): string {
    return this.acaoParaConfirmar === 'autorizar' ? 'bi-unlock-fill' : 'bi-check2-circle';
  }

  abrirConfirmacao(item: Recebimento, acao: 'autorizar' | 'finalizar'): void {
    this.itemParaConfirmar = item;
    this.acaoParaConfirmar = acao;
    this.confirmAcaoModal.show();
  }

  confirmarAcao(): void {
    if (!this.itemParaConfirmar || !this.acaoParaConfirmar) return;

    if (this.acaoParaConfirmar === 'autorizar') {
      this.autorizar(this.itemParaConfirmar);
    } else {
      this.finalizar(this.itemParaConfirmar);
    }

    this.confirmAcaoModal.hide();
    this.itemParaConfirmar = null;
    this.acaoParaConfirmar = null;
  }

  private autorizar(item: Recebimento): void {
    this.recebimentoService.autorizar(item.notaFiscal).subscribe({
      next: atualizado => {
        this.todos = this.todos.map(i => (i.id === atualizado.id ? atualizado : i));
        this.showSuccess('Recebimento autorizado com sucesso!');
      },
      error: () => this.showError('Erro ao autorizar o recebimento.')
    });
  }

  private finalizar(item: Recebimento): void {
    this.recebimentoService.finalizar(item.notaFiscal).subscribe({
      next: () => {
        this.todos = this.todos.filter(i => i.id !== item.id);
        this.showSuccess('Recebimento finalizado com sucesso!');
      },
      error: () => this.showError('Erro ao finalizar o recebimento.')
    });
  }

  badgeClass(status: StatusRecebimento): string {
    const map: Record<StatusRecebimento, string> = {
      AGUARDANDO: 'bg-warning text-dark',
      AUTORIZADO: 'bg-primary',
      EM_RECEBIMENTO: 'bg-info text-dark',
      FINALIZADO: 'bg-success'
    };
    return map[status] ?? 'bg-secondary';
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    this.errorMessage = '';
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private showError(msg: string): void {
    this.errorMessage = msg;
    this.successMessage = '';
  }
}
