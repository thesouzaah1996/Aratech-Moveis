import { PermissaoDirective } from '../../../core/permissao.directive';
import { criarModal } from '../../../core/ui/modal';
import { Component, ElementRef, AfterViewInit, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, interval, forkJoin, Subject, debounceTime } from 'rxjs';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { RegistroChegadaService } from '../../../core/services/registro-chegada.service';
import { RegistroChegada, RegistroChegadaForm, StatusCaminhao, ResumoFila, SETOR_LABELS, STATUS_CAMINHAO_LABELS } from '../../../core/models/registro-chegada.model';
import { validarChegada, normalizarChegada } from '../../../core/models/chegada-form';
@Component({ selector: 'app-controle-acesso', standalone: true, imports: [PermissaoDirective,FormsModule, DatePipe, NavbarComponent, BreadcrumbComponent],
  templateUrl: './controle-acesso.component.html', styleUrl: './controle-acesso.component.scss' })
export class ControleAcessoComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('historicoModal') historicoModalEl!: ElementRef;
  @ViewChild('confirmEntradaModal') confirmEntradaModalEl!: ElementRef;
  @ViewChild('editModal') editModalEl!: ElementRef;
  breadcrumb: BreadcrumbItem[] = [{ label: 'Início', route: '/dashboard' }, { label: 'Portaria', route: '/portaria' }, { label: 'Controle de Acesso' }];
  readonly setorLabels = SETOR_LABELS;
  readonly statusLabels = { ...STATUS_CAMINHAO_LABELS, AUTORIZADO: 'Liberada pelo setor', EM_RECEBIMENTO: 'Entrada liberada' };
  finalizandoEntrada = false;
  fila: RegistroChegada[] = []; historico: RegistroChegada[] = [];
  resumo: ResumoFila = { aguardando: 0, autorizados: 0, emRecebimento: 0, ultimaChegadaId: null, ultimaEmpresa: null };
  form = this.emptyForm(); editForm = this.emptyForm();
  submitted = false; editSubmitted = false; successMessage = ''; errorMessage = ''; atualizacaoError = ''; editError = ''; entradaError = ''; historicoError = '';
  loading = false; salvando = false; editando = false; confirmando = false; historicoLoading = false;
  page = 1; pageSize = 8; total = 0; historicoPage = 1; historicoTotal = 0;
  filtroNF = ''; filtroData = ''; itemParaConfirmar: RegistroChegada | null = null; editId: number | null = null;
  cadastroPendente: RegistroChegadaForm | null = null;
  private filtroHistorico = new Subject<void>();
  private filtroHistoricoSub?: Subscription;
  private primeiraCarga = true;
  private chaveOperacao = crypto.randomUUID();
  private historicoModal?: any; private confirmEntradaModal?: any; private editModal?: any;
  private request?: Subscription; private historyRequest?: Subscription; private mutation?: Subscription; private polling?: Subscription;
  constructor(private readonly registroChegadaService: RegistroChegadaService) {}
  ngOnInit(): void {
    this.filtroHistoricoSub = this.filtroHistorico.pipe(debounceTime(300)).subscribe(() => this.filtrarHistorico());
    this.carregarFila();
    this.polling = interval(5000).subscribe(() => { if (!this.loading && !this.salvando && !this.editando && !this.confirmando) this.carregarFila(); });
  }
  ngAfterViewInit(): void {
    this.historicoModal = criarModal(this.historicoModalEl.nativeElement);
    this.confirmEntradaModal = criarModal(this.confirmEntradaModalEl.nativeElement);
    this.editModal = criarModal(this.editModalEl.nativeElement);
  }
  ngOnDestroy(): void {
    this.filtroHistoricoSub?.unsubscribe(); this.filtroHistorico.complete();
    this.request?.unsubscribe(); this.historyRequest?.unsubscribe(); this.mutation?.unsubscribe(); this.polling?.unsubscribe();
    this.historicoModal?.dispose(); this.confirmEntradaModal?.dispose(); this.editModal?.dispose();
  }
  carregarFila(): void {
    this.request?.unsubscribe(); this.loading = true;
    this.request = forkJoin({ pagina: this.registroChegadaService.getFila(this.page - 1, this.pageSize), resumo: this.registroChegadaService.resumo() }).subscribe({
      next: ({ pagina, resumo }) => {
        const autorizadasAgora = pagina.content.filter(item => item.status === 'AUTORIZADO'
          && this.fila.some(anterior => anterior.id === item.id && anterior.status === 'AGUARDANDO'));
        if (!this.primeiraCarga && (resumo.autorizados > this.resumo.autorizados || autorizadasAgora.length > 0)) {
          this.successMessage = 'O setor responsavel autorizou uma entrada. Confira as cargas autorizadas e libere o acesso.';
        }
        this.primeiraCarga = false;
        this.loading = false; this.atualizacaoError = ''; this.total = pagina.totalElements; this.resumo = resumo;
        if (this.historicoModalEl?.nativeElement.classList.contains('show') && !this.historicoLoading) this.carregarHistorico();
        if (this.page > Math.max(1, pagina.totalPages)) { this.page = Math.max(1, pagina.totalPages); this.carregarFila(); return; }
        this.fila = pagina.content;
      }, error: () => { this.loading = false; this.atualizacaoError = 'Nao foi possivel atualizar a fila. Os dados exibidos podem estar desatualizados.'; }
    });
  }
  get filaPaginada(): RegistroChegada[] { return this.fila; }
  get totalPages(): number { return Math.ceil(this.total / this.pageSize); }
  get visiblePages(): number[] { const start = Math.max(1, Math.min(this.page - 5, this.totalPages - 7)); const end = Math.min(this.totalPages, start + 7); return Array.from({ length: end - start + 1 }, (_, i) => start + i); }
  get paginationStart(): number { return this.total ? (this.page - 1) * this.pageSize + 1 : 0; }
  get paginationEnd(): number { return Math.min(this.page * this.pageSize, this.total); }
  setPage(page: number): void { if (this.loading || page < 1 || page > this.totalPages) return; this.page = page; this.carregarFila(); }
  registrarChegada(): void {
    if (this.salvando) return; this.submitted = true;
    if (!this.cadastroPendente) { const erro = validarChegada(this.form); if (erro) { this.errorMessage = erro; return; } this.cadastroPendente = normalizarChegada(this.form); }
    this.salvando = true; this.errorMessage = '';
    this.mutation = this.registroChegadaService.add(this.cadastroPendente, this.chaveOperacao).subscribe({
      next: () => {
        this.salvando = false; this.cadastroPendente = null; this.chaveOperacao = crypto.randomUUID(); this.form = this.emptyForm(); this.submitted = false;
        this.successMessage = 'Chegada registrada. O setor responsavel foi avisado pela sua fila.'; this.page = 1; this.carregarFila();
      }, error: e => {
        this.salvando = false;
        if (e.status >= 400 && e.status < 500) { this.cadastroPendente = null; this.chaveOperacao = crypto.randomUUID(); }
        this.errorMessage = this.erro(e) + (this.cadastroPendente ? ' Tente registrar novamente: os mesmos dados serao reenviados para evitar duplicidade.' : '');
      }
    });
  }
  abrirConfirmacaoEntrada(item: RegistroChegada): void {
    if (item.status === 'AGUARDANDO') { this.errorMessage = 'Entrada bloqueada: aguarde a autorização do setor responsável antes de liberar o caminhão.'; return; }
    if (item.status !== 'AUTORIZADO' && item.status !== 'EM_RECEBIMENTO') return;
    this.finalizandoEntrada = item.status === 'EM_RECEBIMENTO'; this.itemParaConfirmar = item; this.entradaError = ''; this.confirmEntradaModal.show(); }
  confirmarEntrada(): void {
    if (!this.itemParaConfirmar || this.confirmando) return; this.confirmando = true;
    const operacao = this.finalizandoEntrada ? this.registroChegadaService.finalizar(this.itemParaConfirmar.id) : this.registroChegadaService.confirmarEntrada(this.itemParaConfirmar.id);
    this.mutation = operacao.subscribe({
      next: registro => {
        const estavaNaFila = this.fila.some(item => item.id === registro.id);
        this.fila = this.finalizandoEntrada ? this.fila.filter(item => item.id !== registro.id) : this.fila.map(item => item.id === registro.id ? registro : item);
        if (this.finalizandoEntrada && estavaNaFila) this.total = Math.max(0, this.total - 1);
        this.confirmando = false; this.confirmEntradaModal.hide(); this.itemParaConfirmar = null;
        this.successMessage = this.finalizandoEntrada ? 'Registro finalizado e movido para o historico da portaria e do setor responsavel.' : 'Entrada liberada. Após a entrada do caminhão, finalize o registro para enviá-lo ao histórico.';
        this.carregarFila();
      },
      error: e => { this.confirmando = false; this.entradaError = this.erro(e); this.carregarFila(); }
    });
  }
  abrirEdicao(item: RegistroChegada): void {
    if (item.status !== 'AGUARDANDO') return; this.editId = item.id; this.editForm = { notaFiscal: item.notaFiscal, empresa: item.empresa,
      nomeMotorista: item.nomeMotorista, placa: item.placa, descricaoCarga: item.descricaoCarga, setorResponsavel: item.setorResponsavel, tipoOperacao: item.tipoOperacao };
    this.editError = ''; this.editSubmitted = false; this.editModal.show();
  }
  salvarEdicao(): void {
    if (!this.editId || this.editando) return; this.editSubmitted = true; this.editError = validarChegada(this.editForm); if (this.editError) return;
    this.editando = true;
    this.mutation = this.registroChegadaService.atualizar(this.editId, normalizarChegada(this.editForm)).subscribe({
      next: () => { this.editando = false; this.editModal.hide(); this.successMessage = 'Registro atualizado.'; this.carregarFila(); },
      error: e => { this.editando = false; this.editError = this.erro(e); }
    });
  }
  fecharHistorico(): void { this.historyRequest?.unsubscribe(); this.historicoLoading = false; this.historicoModal.hide(); }
  cancelarEdicao(): void { if (!this.editando) this.editModal.hide(); }
  cancelarEntrada(): void { if (!this.confirmando) { this.confirmEntradaModal.hide(); this.itemParaConfirmar = null; } }
  abrirHistorico(): void { this.historicoPage = 1; this.carregarHistorico(); this.historicoModal.show(); }
  carregarHistorico(): void {
    this.historyRequest?.unsubscribe(); this.historicoLoading = true; this.historicoError = '';
    this.historyRequest = this.registroChegadaService.getHistoricoCompleto(this.filtroNF, this.filtroData).subscribe({
      next: itens => { this.historico = itens; this.historicoTotal = itens.length; this.historicoLoading = false; },
      error: e => { this.historicoLoading = false; this.historicoError = this.erro(e); }
    });
  }
  get historicoFiltrado(): RegistroChegada[] { return this.historico; }
  onFiltroHistorico(): void { this.filtroHistorico.next(); }
  filtrarHistorico(): void { this.historicoPage = 1; this.carregarHistorico(); }
  limparFiltros(): void { this.filtroNF = ''; this.filtroData = ''; this.filtrarHistorico(); }
  get historicoPages(): number { return Math.ceil(this.historicoTotal / this.pageSize); }
  setHistoricoPage(p: number): void { if (this.historicoLoading || p < 1 || p > this.historicoPages) return; this.historicoPage = p; this.carregarHistorico(); }
  badgeClass(status: StatusCaminhao): string { return { AGUARDANDO: 'bg-warning text-dark', AUTORIZADO: 'bg-primary', EM_RECEBIMENTO: 'bg-info text-dark', FINALIZADO: 'bg-success' }[status]; }
  private emptyForm(): RegistroChegadaForm { return { notaFiscal: '', empresa: '', nomeMotorista: '', placa: '', descricaoCarga: '', setorResponsavel: 'ALMOXARIFADO' }; }
  private erro(e: any): string { return e.error?.mensagem || 'Nao foi possivel concluir a operacao. Tente novamente.'; }
}
