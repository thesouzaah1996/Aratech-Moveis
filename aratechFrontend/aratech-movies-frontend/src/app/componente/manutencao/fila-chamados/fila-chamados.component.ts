import { PermissaoDirective } from '../../../core/permissao.directive';
import { Component, ElementRef, AfterViewInit, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription, debounceTime, interval } from 'rxjs';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { FilaManutencaoService } from '../../../core/services/fila-manutencao.service';
import { MecanicoService } from '../../../core/services/mecanico.service';
import { SessaoState } from '../../../core/services/sessao-state.service';
import { ItemFilaManutencao } from '../../../core/models/fila-manutencao.model';
import { StatusChamado, PRIORIDADE_LABELS, TIPO_MANUTENCAO_LABELS } from '../../../core/models/chamado.model';
import { FINALIDADE_PECA_LABELS } from '../../../core/models/solicitacao-peca.model';
import { LookupItem } from '../../../core/models/lookup.model';
import { criarModal, ModalDaTela } from '../../../core/ui/modal';
@Component({ selector: 'app-fila-chamados', standalone: true, imports: [PermissaoDirective,CommonModule, FormsModule, NavbarComponent, BreadcrumbComponent], templateUrl: './fila-chamados.component.html', styleUrl: './fila-chamados.component.scss' })
export class FilaChamadosComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('atribuirModal') atribuirModalEl!: ElementRef<HTMLElement>;
  @ViewChild('detalhesModal') detalhesModalEl!: ElementRef<HTMLElement>;
  @ViewChild('concluirModal') concluirModalEl!: ElementRef<HTMLElement>;
  breadcrumb: BreadcrumbItem[] = [{ label: 'Início', route: '/dashboard' }, { label: 'Manutenção', route: '/manutencao' }, { label: 'Fila de Chamados' }];
  readonly prioridadeLabels = PRIORIDADE_LABELS;
  readonly statusLabels: Record<StatusChamado, string> = { ABERTA: 'Aguardando', EM_MANUTENCAO: 'Em Manutenção', CONCLUIDA: 'Concluída' };
  filtroStatus: StatusChamado | 'Todos' = 'Todos';
  searchTerm = ''; page = 1; pageSize = 8; total = 0;
  loading = false; salvando = false; mecanicosLoading = false; detalhesLoading = false;
  submitted = false; successMessage = ''; errorMessage = ''; modalError = ''; detalhesError = '';
  mecanicoId: number | null = null;
  chamadoSelecionado: ItemFilaManutencao | null = null;
  mecanicosAtivos: LookupItem[] = [];
  mecanicosError = '';
  atribuicoesPendentes: Record<number, number | null> = {};
  errosAtribuicao: Record<number, string> = {};
  atribuindoId: number | null = null;
  private atualizandoMecanicos = false;
  chamados: ItemFilaManutencao[] = [];
  private atribuirModal?: ModalDaTela; private detalhesModal?: ModalDaTela; private concluirModal?: ModalDaTela;
  private request?: Subscription; private mutation?: Subscription; private lookupRequest?: Subscription; private detalhesRequest?: Subscription;
  private polling?: Subscription; private buscaRequest?: Subscription; private busca = new Subject<void>(); private destruido = false;
  constructor(private readonly filaService: FilaManutencaoService, private readonly mecanicoService: MecanicoService,public sessao:SessaoState=new SessaoState()) {}
  ngOnInit(): void {
    this.carregarChamados();
    this.carregarMecanicos();
    this.buscaRequest = this.busca.pipe(debounceTime(300)).subscribe(() => this.carregarChamados());
    this.polling = interval(5000).subscribe(() => {
      if (!this.salvando) {
        if (!this.loading) this.carregarChamados();
        if (!this.atualizandoMecanicos) this.carregarMecanicos();
      }
    });
  }
  ngAfterViewInit(): void {
    this.atribuirModal = criarModal(this.atribuirModalEl.nativeElement);
    this.detalhesModal = criarModal(this.detalhesModalEl.nativeElement);
    this.concluirModal = criarModal(this.concluirModalEl.nativeElement);
  }
  ngOnDestroy(): void {
    this.destruido = true;
    for (const s of [this.request, this.mutation, this.lookupRequest, this.detalhesRequest, this.polling, this.buscaRequest]) s?.unsubscribe();
    this.busca.complete(); this.atribuirModal?.dispose(); this.detalhesModal?.dispose(); this.concluirModal?.dispose();
  }
  carregarChamados(): void {
    this.request?.unsubscribe(); this.loading = true;
    this.request = this.filaService.getPagina(this.page - 1, this.pageSize, this.filtroStatus === 'Todos' ? undefined : this.filtroStatus, this.searchTerm).subscribe({
      next: p => {
        this.loading = false; this.errorMessage = ''; this.total = p.total;
        const ultima = Math.max(1, Math.ceil(this.total / this.pageSize));
        if (this.page > ultima) { this.page = ultima; this.carregarChamados(); return; }
        this.chamados = p.itens;
        if (this.detalhesModalEl?.nativeElement.classList.contains('show') && this.chamadoSelecionado && !this.detalhesLoading) this.atualizarDetalhes(this.chamadoSelecionado.id);
      },
      error: e => { this.loading = false; this.errorMessage = e.error?.mensagem || 'Nao foi possivel atualizar a fila. Os dados exibidos podem estar desatualizados.'; }
    });
  }
  onSearch(): void { this.page = 1; this.busca.next(); }
  get paged(): ItemFilaManutencao[] { return this.chamados; }
  get totalPages(): number { return Math.ceil(this.total / this.pageSize); }
  get visiblePages(): number[] { const start = Math.max(1, Math.min(this.page - 2, this.totalPages - 4)); return Array.from({ length: Math.max(0, Math.min(this.totalPages, start + 4) - start + 1) }, (_, i) => start + i); }
  get paginationStart(): number { return this.total ? (this.page - 1) * this.pageSize + 1 : 0; }
  get paginationEnd(): number { return Math.min(this.page * this.pageSize, this.total); }
  setPage(p: number): void { if (!this.loading && p >= 1 && p <= this.totalPages) { this.page = p; this.carregarChamados(); } }
  setFiltro(status: StatusChamado | 'Todos'): void { this.filtroStatus = status; this.page = 1; this.carregarChamados(); }
  origemLabel(c: ItemFilaManutencao): string { return c.origem === 'PECA' ? 'Peça' : 'Manutenção'; }
  tipoDescricao(c: ItemFilaManutencao): string { return c.peca ? FINALIDADE_PECA_LABELS[c.peca.finalidade] : c.tipo ? TIPO_MANUTENCAO_LABELS[c.tipo] : '—'; }
  carregarMecanicos(): void {
    if (this.atualizandoMecanicos) return;
    this.atualizandoMecanicos = true;
    this.mecanicosLoading = this.mecanicosAtivos.length === 0;
    this.lookupRequest = this.filaService.mecanicos().subscribe({
      next: lista => { this.mecanicosAtivos = lista; this.mecanicosLoading = false; this.atualizandoMecanicos = false; this.mecanicosError = ''; },
      error: e => { this.mecanicosLoading = false; this.atualizandoMecanicos = false; this.mecanicosError = e.error?.mensagem || 'Nao foi possivel atualizar os mecanicos cadastrados.'; }
    });
  }
  mecanicoDisponivel(id: number | null | undefined): boolean { return !!id && this.mecanicosAtivos.some(m => m.id === id); }
  selecionarMecanico(id: number, mecanicoId: number | null): void {
    this.atribuicoesPendentes[id] = mecanicoId;
    delete this.errosAtribuicao[id];
  }
  atribuirDaLinha(c: ItemFilaManutencao): void {
    if (this.salvando || c.status !== 'ABERTA') return;
    const mecanicoId = this.atribuicoesPendentes[c.id];
    if (!mecanicoId || !this.mecanicoDisponivel(mecanicoId)) { this.errosAtribuicao[c.id] = 'Selecione um mecanico ativo cadastrado.'; return; }
    this.salvando = true; this.atribuindoId = c.id; delete this.errosAtribuicao[c.id];
    this.mutation = this.filaService.atribuirMecanico(c.id, mecanicoId).subscribe({
      next: atualizado => {
        this.salvando = false; this.atribuindoId = null;
        this.chamados = this.chamados.map(item => item.id === atualizado.id ? atualizado : item);
        delete this.atribuicoesPendentes[c.id];
        this.successMessage = 'Chamado atribuido ao mecanico com sucesso.';
        this.carregarChamados();
      },
      error: e => {
        this.salvando = false; this.atribuindoId = null;
        this.errosAtribuicao[c.id] = e.error?.mensagem || 'Nao foi possivel atribuir o chamado.';
        this.carregarMecanicos(); this.carregarChamados();
      }
    });
  }
  openAtribuir(c: ItemFilaManutencao): void {
    if (this.salvando || c.status === 'CONCLUIDA') return;
    this.submitted = false; this.modalError = ''; this.mecanicoId = c.mecanico?.id ?? null; this.chamadoSelecionado = c;
    this.carregarMecanicos();
    this.atribuirModal?.show();
  }
  openDetalhes(c: ItemFilaManutencao): void { this.chamadoSelecionado = c; this.atualizarDetalhes(c.id); this.detalhesModal?.show(); }
  private atualizarDetalhes(id: number): void {
    this.detalhesRequest?.unsubscribe(); this.detalhesLoading = true; this.detalhesError = '';
    this.detalhesRequest = this.filaService.getById(id).subscribe({ next: c => { this.chamadoSelecionado = c; this.detalhesLoading = false; }, error: e => { this.detalhesLoading = false; this.detalhesError = e.error?.mensagem || 'Nao foi possivel atualizar os detalhes.'; } });
  }
  abrirAtribuirDoDetalhes(c: ItemFilaManutencao): void {
    this.detalhesModalEl.nativeElement.addEventListener('hidden.bs.modal', () => { if (!this.destruido) this.openAtribuir(c); }, { once: true }); this.detalhesModal?.hide();
  }
  cancelarAtribuicao(): void { if (!this.salvando) this.atribuirModal?.hide(); }
  fecharDetalhes(): void { this.detalhesRequest?.unsubscribe(); this.detalhesLoading = false; this.detalhesModal?.hide(); }
  confirmarAtribuicao(): void {
    this.submitted = true;
    if (!this.mecanicoId || !this.chamadoSelecionado || this.salvando || this.mecanicosLoading) return;
    if (!this.mecanicosAtivos.some(m => m.id === this.mecanicoId)) { this.modalError = 'Selecione um mecanico ativo.'; return; }
    this.salvando = true; this.modalError = '';
    this.mutation = this.filaService.atribuirMecanico(this.chamadoSelecionado.id, this.mecanicoId).subscribe({
      next: () => { this.salvando = false; this.atribuirModal?.hide(); this.successMessage = 'Mecanico atribuido com sucesso.'; this.carregarChamados(); },
      error: e => { this.salvando = false; this.modalError = e.error?.mensagem || 'Nao foi possivel atribuir o mecanico.'; this.carregarChamados(); }
    });
  }
  openConcluir(c: ItemFilaManutencao): void { if (!this.salvando && c.status === 'EM_MANUTENCAO') { this.chamadoSelecionado = c; this.modalError = ''; this.concluirModal?.show(); } }
  cancelarConclusao(): void { if (!this.salvando) this.concluirModal?.hide(); }
  confirmarConclusao(): void {
    if (!this.chamadoSelecionado || this.salvando) return;
    this.salvando = true; this.modalError = '';
    this.mutation = this.filaService.concluir(this.chamadoSelecionado.id).subscribe({
      next: () => { this.salvando = false; this.concluirModal?.hide(); this.successMessage = 'Atendimento concluido com sucesso.'; this.carregarChamados(); },
      error: e => { this.salvando = false; this.modalError = e.error?.mensagem || 'Nao foi possivel concluir o atendimento.'; this.carregarChamados(); }
    });
  }
  statusBadge(status: StatusChamado): string { return status === 'CONCLUIDA' ? 'bg-success' : status === 'EM_MANUTENCAO' ? 'bg-warning text-dark' : 'bg-secondary'; }
  prioridadeClass(p: ItemFilaManutencao['prioridade']): string { return 'badge-prioridade ' + (p === 'ALTA' ? 'alta' : p === 'MEDIA' ? 'media' : 'baixa'); }
}
