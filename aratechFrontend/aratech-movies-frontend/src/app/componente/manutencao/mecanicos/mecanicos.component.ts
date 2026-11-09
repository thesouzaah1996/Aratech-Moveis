import { PermissaoDirective } from '../../../core/permissao.directive';
import { Component, ElementRef, AfterViewInit, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { MecanicoService } from '../../../core/services/mecanico.service';
import {
  Mecanico, MecanicoForm, Especialidade, Turno,
  ESPECIALIDADES, ESPECIALIDADE_LABELS, TURNOS, TURNO_LABELS
} from '../../../core/models/mecanico.model';

import { Subscription } from 'rxjs';
import { criarModal, ModalDaTela } from '../../../core/ui/modal';

@Component({
  selector: 'app-mecanicos',
  standalone: true,
  imports: [PermissaoDirective,FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './mecanicos.component.html',
  styleUrl: './mecanicos.component.scss'
})
export class MecanicosComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('mecanicoModal') mecanicoModalEl!: ElementRef;
  @ViewChild('editModal') editModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Manutenção', route: '/manutencao' },
    { label: 'Mecânicos' }
  ];

  readonly especialidadesDisponiveis = ESPECIALIDADES;
  readonly especialidadeLabels = ESPECIALIDADE_LABELS;
  readonly turnosDisponiveis = TURNOS;
  readonly turnoLabels = TURNO_LABELS;

  mecanicos: Mecanico[] = [];
  page = 1;
  pageSize = 8;
  loading = false;
  successMessage = '';
  errorMessage = '';
  searchTerm = '';
  filtroStatus: 'todos' | 'ativos' | 'inativos' = 'todos';

  form: MecanicoForm = this.emptyForm();
  formEdit: MecanicoForm = this.emptyForm();
  mecanicoParaDesativar: Mecanico | null = null;
  private mecanicoEditandoId: number | null = null;

  submitted = false;
  submittedEdit = false;
  salvando = false;
  modalError = '';
  editError = '';
  statusError = '';
  private request?: Subscription;
  private mutation?: Subscription;

  private mecanicoModal?: ModalDaTela;
  private editModal?: ModalDaTela;
  private deleteModal?: ModalDaTela;

  constructor(private mecanicoService: MecanicoService) {}

  ngOnInit(): void {
    this.loadMecanicos();
  }

  ngAfterViewInit(): void {
    this.mecanicoModal = criarModal(this.mecanicoModalEl.nativeElement);
    this.editModal = criarModal(this.editModalEl.nativeElement);
    this.deleteModal = criarModal(this.deleteModalEl.nativeElement);
  }

  ngOnDestroy(): void {
    this.request?.unsubscribe(); this.mutation?.unsubscribe();
    this.mecanicoModal?.dispose(); this.editModal?.dispose(); this.deleteModal?.dispose();
  }

  loadMecanicos(): void {
    this.request?.unsubscribe(); this.loading = true;
    this.request = this.mecanicoService.getAll().subscribe({
      next: (lista) => {
        this.mecanicos = lista;
        this.loading = false; this.errorMessage = '';
        this.page = Math.max(1, Math.min(this.page, this.totalPages));
      },
      error: () => {
        this.showError('Erro ao carregar mecânicos.');
        this.loading = false;
      }
    });
  }

  get filtrados(): Mecanico[] {
    let lista = this.mecanicos;

    if (this.filtroStatus === 'ativos') {
      lista = lista.filter(m => m.ativo);
    } else if (this.filtroStatus === 'inativos') {
      lista = lista.filter(m => !m.ativo);
    }

    const term = this.searchTerm.trim().toLowerCase();
    if (!term) return lista;
    return lista.filter(m =>
      m.nome.toLowerCase().includes(term) ||
      m.id.toString().includes(term)
    );
  }

  onSearch(): void {
    this.page = 1;
  }

  setFiltroStatus(status: 'todos' | 'ativos' | 'inativos'): void {
    this.filtroStatus = status;
    this.page = 1;
  }

  get paged(): Mecanico[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtrados.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filtrados.length / this.pageSize);
  }

  get visiblePages(): number[] {
    const half = 5;
    const start = Math.max(1, Math.min(this.page - half, this.totalPages - 7));
    const end = Math.min(this.totalPages, start + 7);
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

  openAdd(): void {
    if (this.salvando) return;
    this.modalError = '';
    this.submitted = false;
    this.form = this.emptyForm();
    this.mecanicoModal?.show();
  }

  save(): void {
    if (this.salvando) return;
    this.submitted = true; this.modalError = '';
    if (!this.isFormValid(this.form)) { this.modalError = 'Informe um nome de 3 a 150 caracteres, o turno e ao menos uma especialidade valida.'; return; }
    this.salvando = true;
    this.mutation = this.mecanicoService.add(this.normalizar(this.form)).subscribe({
      next: () => { this.salvando = false; this.mecanicoModal?.hide(); this.loadMecanicos(); this.showSuccess('Mecanico adicionado com sucesso.'); },
      error: e => { this.salvando = false; this.modalError = this.erro(e, 'Nao foi possivel salvar o mecanico.'); }
    });
  }
  cancelarCadastro(): void { if (!this.salvando) this.mecanicoModal?.hide(); }
  cancelarEdicao(): void { if (!this.salvando) this.editModal?.hide(); }
  cancelarDesativacao(): void { if (!this.salvando) this.deleteModal?.hide(); }

  openEditModal(m: Mecanico): void {
    if (this.salvando) return;
    this.editError = '';
    this.submittedEdit = false;
    this.mecanicoEditandoId = m.id;
    this.formEdit = { nome: m.nome, especialidades: [...m.especialidades], turno: m.turno };
    this.editModal?.show();
  }

  toggleEspecialidade(form: MecanicoForm, especialidade: Especialidade): void {
    if (this.salvando) return;
    const idx = form.especialidades.indexOf(especialidade);
    if (idx > -1) {
      form.especialidades.splice(idx, 1);
    } else {
      form.especialidades.push(especialidade);
    }
  }

  saveEdit(): void {
    if (this.salvando) return;
    this.submittedEdit = true; this.editError = '';
    if (!this.mecanicoEditandoId || !this.isFormValid(this.formEdit)) { this.editError = 'Informe um nome de 3 a 150 caracteres, o turno e ao menos uma especialidade valida.'; return; }
    this.salvando = true;
    this.mutation = this.mecanicoService.update(this.mecanicoEditandoId, this.normalizar(this.formEdit)).subscribe({
      next: () => { this.salvando = false; this.editModal?.hide(); this.mecanicoEditandoId = null; this.loadMecanicos(); this.showSuccess('Mecanico atualizado com sucesso.'); },
      error: e => { this.salvando = false; this.editError = this.erro(e, 'Nao foi possivel atualizar o mecanico.'); }
    });
  }

  ativarMecanico(id: number): void {
    if (this.salvando) return;
    this.salvando = true;
    this.mutation = this.mecanicoService.enable(id).subscribe({
      next: () => { this.salvando = false; this.loadMecanicos(); this.showSuccess('Mecanico ativado com sucesso.'); },
      error: e => { this.salvando = false; this.showError(this.erro(e, 'Nao foi possivel ativar o mecanico.')); }
    });
  }
  openDesativar(m: Mecanico): void {
    if (this.salvando) return;
    this.statusError = ''; this.mecanicoParaDesativar = m; this.deleteModal?.show();
  }
  confirmDesativar(): void {
    if (!this.mecanicoParaDesativar || this.salvando) return;
    this.salvando = true; this.statusError = '';
    this.mutation = this.mecanicoService.disable(this.mecanicoParaDesativar.id).subscribe({
      next: () => { this.salvando = false; this.deleteModal?.hide(); this.mecanicoParaDesativar = null; this.loadMecanicos(); this.showSuccess('Mecanico desativado com sucesso.'); },
      error: e => { this.salvando = false; this.statusError = this.erro(e, 'Nao foi possivel desativar o mecanico.'); }
    });
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

  private isFormValid(form: MecanicoForm): boolean {
    return form.nome.trim().length >= 3 && form.nome.length <= 150 && !/[\p{Cc}\p{Cf}]/u.test(form.nome)
      && form.especialidades.length > 0 && form.especialidades.every(e => ESPECIALIDADES.includes(e))
      && TURNOS.includes(form.turno as Turno);
  }
  private normalizar(form: MecanicoForm): MecanicoForm {
    return { nome: form.nome.trim().replace(/ +/g, ' '), especialidades: [...new Set(form.especialidades)], turno: form.turno };
  }
  private erro(e: any, fallback: string): string { return e.error?.mensagem || e.error?.message || fallback; }

  private emptyForm(): MecanicoForm {
    return { nome: '', especialidades: [], turno: '' };
  }
}
