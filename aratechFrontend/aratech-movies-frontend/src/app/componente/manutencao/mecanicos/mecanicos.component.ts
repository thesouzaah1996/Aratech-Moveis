import { Component, ElementRef, AfterViewInit, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { MecanicoService } from '../../../core/services/mecanico.service';
import {
  Mecanico, MecanicoForm, Especialidade,
  ESPECIALIDADES, ESPECIALIDADE_LABELS, TURNOS, TURNO_LABELS
} from '../../../core/models/mecanico.model';

declare const bootstrap: any;

@Component({
  selector: 'app-mecanicos',
  standalone: true,
  imports: [FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './mecanicos.component.html',
  styleUrl: './mecanicos.component.scss'
})
export class MecanicosComponent implements OnInit, AfterViewInit {
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

  private mecanicoModal?: any;
  private editModal?: any;
  private deleteModal?: any;

  constructor(private mecanicoService: MecanicoService) {}

  ngOnInit(): void {
    this.loadMecanicos();
  }

  ngAfterViewInit(): void {
    this.mecanicoModal = new bootstrap.Modal(this.mecanicoModalEl.nativeElement);
    this.editModal = new bootstrap.Modal(this.editModalEl.nativeElement);
    this.deleteModal = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  loadMecanicos(): void {
    this.loading = true;
    this.mecanicoService.getAll().subscribe({
      next: (lista) => {
        this.mecanicos = lista;
        this.loading = false;
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
    this.submitted = false;
    this.form = this.emptyForm();
    this.mecanicoModal.show();
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid(this.form)) return;
    this.mecanicoService.add(this.form).subscribe({
      next: () => {
        this.mecanicoModal.hide();
        this.loadMecanicos();
        this.showSuccess('Mecânico adicionado com sucesso!');
      },
      error: () => {
        this.showError('Erro ao salvar mecânico.');
      }
    });
  }

  openEditModal(m: Mecanico): void {
    this.submittedEdit = false;
    this.mecanicoEditandoId = m.id;
    this.formEdit = { nome: m.nome, especialidades: [...m.especialidades], turno: m.turno };
    this.editModal.show();
  }

  toggleEspecialidade(form: MecanicoForm, especialidade: Especialidade): void {
    const idx = form.especialidades.indexOf(especialidade);
    if (idx > -1) {
      form.especialidades.splice(idx, 1);
    } else {
      form.especialidades.push(especialidade);
    }
  }

  saveEdit(): void {
    this.submittedEdit = true;
    if (!this.mecanicoEditandoId || !this.isFormValid(this.formEdit)) return;
    this.mecanicoService.update(this.mecanicoEditandoId, this.formEdit).subscribe({
      next: () => {
        this.editModal.hide();
        this.mecanicoEditandoId = null;
        this.loadMecanicos();
        this.showSuccess('Mecânico atualizado com sucesso!');
      },
      error: () => {
        this.showError('Erro ao atualizar mecânico.');
      }
    });
  }

  onSwitchClick(event: Event, m: Mecanico): void {
    event.preventDefault();
    if (m.ativo) {
      this.openDesativar(m);
    } else {
      this.ativarMecanico(m.id);
    }
  }

  ativarMecanico(id: number): void {
    this.mecanicoService.enable(id).subscribe({
      next: () => {
        this.loadMecanicos();
        this.showSuccess('Mecânico ativado com sucesso!');
      },
      error: () => {
        this.showError('Erro ao ativar mecânico.');
      }
    });
  }

  openDesativar(m: Mecanico): void {
    this.mecanicoParaDesativar = m;
    this.deleteModal.show();
  }

  confirmDesativar(): void {
    if (!this.mecanicoParaDesativar) return;

    this.mecanicoService.disable(this.mecanicoParaDesativar.id).subscribe({
      next: () => {
        this.deleteModal.hide();
        this.mecanicoParaDesativar = null;
        this.loadMecanicos();
        this.showSuccess('Mecânico desativado com sucesso!');
      },
      error: () => {
        this.showError('Erro ao desativar mecânico.');
      }
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
    return !!form.nome.trim() && form.especialidades.length > 0 && !!form.turno;
  }

  private emptyForm(): MecanicoForm {
    return { nome: '', especialidades: [], turno: '' };
  }
}
