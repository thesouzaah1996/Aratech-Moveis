import { PermissaoDirective } from '../../../core/permissao.directive';
import { criarModal } from '../../../core/ui/modal';
import { Component, ElementRef, AfterViewInit, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, interval, forkJoin } from 'rxjs';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { CarregamentoService } from '../../../core/services/carregamento.service';
import { Recebimento, STATUS_RECEBIMENTO_LABELS, StatusRecebimento } from '../../../core/models/recebimento.model';
import { validarChegada, normalizarChegada } from '../../../core/models/chegada-form';
import { RegistroChegadaForm, ResumoFila } from '../../../core/models/registro-chegada.model';
@Component({ selector: 'app-carregamento-aguardando-descarga', standalone: true, imports: [PermissaoDirective,DatePipe, FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './aguardando-descarga.component.html', styleUrl: './aguardando-descarga.component.scss' })
export class CarregamentoAguardandoDescargaComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('historicoModal') historicoModalEl!: ElementRef;
  @ViewChild('editModal') editModalEl!: ElementRef;
  @ViewChild('confirmAcaoModal') confirmAcaoModalEl!: ElementRef;
  breadcrumb: BreadcrumbItem[] = [{ label: 'Início', route: '/dashboard' }, { label: 'Carregamento', route: '/carregamento' }, { label: 'Recebimento' }];
  readonly statusLabels = { ...STATUS_RECEBIMENTO_LABELS, EM_RECEBIMENTO: 'Em operação' };
  fila: Recebimento[] = []; historico: Recebimento[] = [];
  resumo: ResumoFila = { aguardando: 0, autorizados: 0, emRecebimento: 0, ultimaChegadaId: null, ultimaEmpresa: null };
  successMessage = ''; errorMessage = ''; novaChegada = ''; modalError = ''; historicoError = '';
  loading = false; salvando = false; historicoLoading = false;
  itemParaConfirmar: Recebimento | null = null; acaoParaConfirmar: 'autorizar' | 'finalizar' | null = null;
  page = 1; pageSize = 8; total = 0; historicoPage = 1; historicoTotal = 0;
  editId: number | null = null;
  editForm: RegistroChegadaForm = { notaFiscal: '', empresa: '', nomeMotorista: '', placa: '', descricaoCarga: '', setorResponsavel: 'CARREGAMENTO' };
  editando = false; editSubmitted = false; editError = '';
  private editModal?: any;
  private primeiraCarga = true;
  private historicoModal?: any; private confirmAcaoModal?: any;
  private request?: Subscription; private historyRequest?: Subscription; private mutation?: Subscription; private polling?: Subscription;
  constructor(private readonly carregamentoService: CarregamentoService) {}
  ngOnInit(): void { this.carregarTodos(); this.polling = interval(5000).subscribe(() => { if (!this.loading && !this.salvando && !this.editando) this.carregarTodos(); }); }
  ngAfterViewInit(): void { this.editModal = criarModal(this.editModalEl.nativeElement); this.historicoModal = criarModal(this.historicoModalEl.nativeElement); this.confirmAcaoModal = criarModal(this.confirmAcaoModalEl.nativeElement); }
  ngOnDestroy(): void { this.request?.unsubscribe(); this.historyRequest?.unsubscribe(); this.mutation?.unsubscribe(); this.polling?.unsubscribe(); this.historicoModal?.dispose(); this.confirmAcaoModal?.dispose(); this.editModal?.dispose(); }
  carregarTodos(): void {
    this.request?.unsubscribe(); this.loading = true;
    this.request = forkJoin({ pagina: this.carregamentoService.getTodos(this.page - 1, this.pageSize), resumo: this.carregamentoService.resumo() }).subscribe({
      next: ({ pagina, resumo }) => {
        this.loading = false; this.errorMessage = ''; this.total = pagina.totalElements;
        if (!this.primeiraCarga && resumo.ultimaChegadaId !== null && resumo.ultimaChegadaId > (this.resumo.ultimaChegadaId ?? 0))
          { this.novaChegada = 'Nova chegada na portaria: ' + resumo.ultimaEmpresa + '. Consulte a fila de espera.'; this.successMessage = ''; }
        this.primeiraCarga = false; this.resumo = resumo;
        if (this.page > Math.max(1, pagina.totalPages)) { this.page = Math.max(1, pagina.totalPages); this.carregarTodos(); return; }
        this.fila = pagina.content;
        if (this.historicoModalEl?.nativeElement.classList.contains('show') && !this.historicoLoading) this.carregarHistorico();
      }, error: () => { this.loading = false; this.errorMessage = 'Nao foi possivel atualizar as operações. Os dados exibidos podem estar desatualizados.'; }
    });
  }
  get filaPaginada(): Recebimento[] { return this.fila; }
  get totalPages(): number { return Math.ceil(this.total / this.pageSize); }
  get visiblePages(): number[] { const start = Math.max(1, Math.min(this.page - 5, this.totalPages - 7)); const end = Math.min(this.totalPages, start + 7); return Array.from({ length: end - start + 1 }, (_, i) => start + i); }
  get paginationStart(): number { return this.total ? (this.page - 1) * this.pageSize + 1 : 0; }
  get paginationEnd(): number { return Math.min(this.page * this.pageSize, this.total); }
  setPage(p: number): void { if (this.loading || p < 1 || p > this.totalPages) return; this.page = p; this.carregarTodos(); }
  abrirEdicao(item: Recebimento): void {
    if (item.status !== 'AGUARDANDO' || this.salvando || this.editando) return;
    this.editId = item.id;
    this.editForm = { notaFiscal: item.notaFiscal, empresa: item.empresa, nomeMotorista: item.nomeMotorista,
      placa: item.placa, descricaoCarga: item.descricaoCarga, setorResponsavel: item.setorResponsavel, tipoOperacao: item.tipoOperacao };
    this.editSubmitted = false; this.editError = ''; this.editModal.show();
  }
  salvarEdicao(): void {
    if (!this.editId || this.editando || this.salvando) return;
    this.editSubmitted = true; this.editError = validarChegada(this.editForm); if (this.editError) return;
    this.editando = true;
    this.mutation = this.carregamentoService.atualizar(this.editId, normalizarChegada(this.editForm)).subscribe({
      next: () => { this.editando = false; this.editModal.hide(); this.successMessage = 'Descarregamento atualizado. Os dados tambem foram atualizados na portaria.'; this.carregarTodos(); },
      error: e => { this.editando = false; this.editError = e.error?.mensagem || 'Nao foi possivel editar a operação.'; }
    });
  }
  fecharHistorico(): void { this.historyRequest?.unsubscribe(); this.historicoLoading = false; this.historicoModal.hide(); }
  cancelarEdicao(): void { if (!this.editando) this.editModal.hide(); }
  cancelarAcao(): void { if (!this.salvando) { this.confirmAcaoModal.hide(); this.itemParaConfirmar = null; this.acaoParaConfirmar = null; } }
  abrirHistorico(): void { this.historicoPage = 1; this.carregarHistorico(); this.historicoModal.show(); }
  carregarHistorico(): void {
    this.historyRequest?.unsubscribe(); this.historicoLoading = true; this.historicoError = '';
    this.historyRequest = this.carregamentoService.getHistoricoCompleto().subscribe({
      next: itens => { this.historico = itens; this.historicoTotal = itens.length; this.historicoLoading = false; },
      error: e => { this.historicoLoading = false; this.historicoError = e.error?.mensagem || 'Nao foi possivel carregar o historico.'; }
    });
  }
  get historicoPages(): number { return Math.ceil(this.historicoTotal / this.pageSize); }
  setHistoricoPage(p: number): void { if (this.historicoLoading || p < 1 || p > this.historicoPages) return; this.historicoPage = p; this.carregarHistorico(); }
  get iconeConfirmacao(): string { return this.acaoParaConfirmar === 'autorizar' ? 'bi-unlock-fill' : 'bi-check2-circle'; }
  get tituloConfirmacao(): string { return this.acaoParaConfirmar === 'autorizar' ? 'Autorizar Libera\u00e7\u00e3o' : 'Finalizar Operação'; }
  get mensagemConfirmacao(): string { return this.acaoParaConfirmar === 'autorizar' ? 'Autorizar esta carga? A portaria sera avisada e podera liberar a entrada.' : 'Finalizar esta operação e mover o registro para o historico?'; }
  abrirConfirmacao(item: Recebimento, acao: 'autorizar' | 'finalizar'): void {
    if ((acao === 'autorizar' && item.status !== 'AGUARDANDO') || (acao === 'finalizar' && item.status !== 'EM_RECEBIMENTO')) return;
    this.itemParaConfirmar = item; this.acaoParaConfirmar = acao; this.modalError = ''; this.confirmAcaoModal.show();
  }
  confirmarAcao(): void {
    if (!this.itemParaConfirmar || !this.acaoParaConfirmar || this.salvando) return;
    const autorizar = this.acaoParaConfirmar === 'autorizar'; this.salvando = true;
    const operacao = autorizar ? this.carregamentoService.autorizar(this.itemParaConfirmar.id) : this.carregamentoService.finalizar(this.itemParaConfirmar.id);
    this.mutation = operacao.subscribe({
      next: () => { this.salvando = false; this.confirmAcaoModal.hide(); this.itemParaConfirmar = null; this.acaoParaConfirmar = null;
        this.successMessage = autorizar ? 'Operação autorizada. Aguarde a entrada confirmada pela portaria.' : 'Operação finalizada.'; this.carregarTodos(); },
      error: e => { this.salvando = false; this.modalError = e.error?.mensagem || 'Nao foi possivel concluir a operacao.'; this.carregarTodos(); }
    });
  }
  badgeClass(status: StatusRecebimento): string { return { AGUARDANDO: 'bg-warning text-dark', AUTORIZADO: 'bg-primary', EM_RECEBIMENTO: 'bg-info text-dark', FINALIZADO: 'bg-success' }[status]; }
}
