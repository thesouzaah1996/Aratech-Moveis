import { PermissaoDirective } from '../../../core/permissao.directive';
import { Component, ElementRef, AfterViewInit, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Subject, Subscription, debounceTime } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { FornecedorService } from '../../../core/services/fornecedor.service';
import { Fornecedor, FornecedorForm } from '../../../core/models/fornecedor.model';

declare const bootstrap: any;

@Component({
  selector: 'app-fornecedores',
  standalone: true,
  imports: [PermissaoDirective,FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './fornecedores.component.html',
  styleUrl: './fornecedores.component.scss'
})
export class FornecedoresComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('supplierModal') supplierModalEl!: ElementRef;
  @ViewChild('deleteModal')   deleteModalEl!: ElementRef;
  @ViewChild('viewModal')     viewModalEl!: ElementRef;
  @ViewChild('editModal')     editModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Almoxarifado', route: '/almoxarifado' },
    { label: 'Fornecedores' }
  ];

  fornecedores: Fornecedor[] = [];
  page = 1;
  pageSize = 8;
  total = 0;
  saving = false;
  changingStatus = false;
  modalError = '';
  editError = '';
  statusError = '';
  private request?: Subscription;
  private detailRequest?: Subscription;
  private mutation?: Subscription;
  private search = new Subject<void>();
  private searchSubscription?: Subscription;
  loading = false;
  successMessage = '';
  errorMessage = '';
  searchTerm = '';
  filtroStatus: 'todos' | 'ativos' | 'inativos' = 'todos';

  form: FornecedorForm = this.emptyForm();
  formEdit: FornecedorForm = this.emptyForm();
  fornecedorEmVisualizacao: Fornecedor | null = null;
  fornecedorParaDesativar: Fornecedor | null = null;
  private fornecedorEditandoId: number | null = null;

  submitted = false;
  submittedEdit = false;

  private supplierModal?: any;
  private deleteModal?: any;
  private viewModal?: any;
  private editModal?: any;

  constructor(private fornecedorService: FornecedorService) {}

  ngOnInit(): void {
    this.searchSubscription = this.search.pipe(debounceTime(300)).subscribe(() => this.loadFornecedores());
    this.loadFornecedores();
  }

  ngAfterViewInit(): void {
    this.supplierModal = new bootstrap.Modal(this.supplierModalEl.nativeElement);
    this.deleteModal   = new bootstrap.Modal(this.deleteModalEl.nativeElement);
    this.viewModal     = new bootstrap.Modal(this.viewModalEl.nativeElement);
    this.editModal     = new bootstrap.Modal(this.editModalEl.nativeElement);
  }

  ngOnDestroy(): void {
    this.request?.unsubscribe();
    this.detailRequest?.unsubscribe();
    this.mutation?.unsubscribe();
    this.searchSubscription?.unsubscribe();
    this.search.complete();
    this.supplierModal?.dispose();
    this.deleteModal?.dispose();
    this.editModal?.dispose();
    this.viewModal?.dispose();
  }

  loadFornecedores(): void {
    this.request?.unsubscribe();
    this.loading = true;
    this.errorMessage = '';
    const ativo = this.filtroStatus === 'todos' ? undefined : this.filtroStatus === 'ativos';
    this.request = this.fornecedorService.getAll(this.page - 1, this.pageSize, this.searchTerm, ativo).subscribe({
      next: data => {
        this.total = data.total;
        const ultima = Math.max(1, Math.ceil(data.total / data.tamanho));
        if (this.page > ultima) {
          this.page = ultima;
          this.loadFornecedores();
          return;
        }
        this.fornecedores = data.fornecedores;
        this.page = data.pagina + 1;
        this.pageSize = data.tamanho;
        this.loading = false;
      },
      error: err => {
        this.total = 0;
        this.fornecedores = [];
        this.showError(this.mensagemErro(err, 'Erro ao carregar fornecedores.'));
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.request?.unsubscribe();
    this.page = 1;
    this.loading = true;
    this.search.next();
  }

  setFiltroStatus(status: 'todos' | 'ativos' | 'inativos'): void {
    this.filtroStatus = status;
    this.page = 1;
    this.loadFornecedores();
  }

  get totalPages(): number { return Math.ceil(this.total / this.pageSize); }
  get visiblePages(): number[] {
    const inicio = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    return Array.from({length: Math.max(0, Math.min(5, this.totalPages))}, (_, i) => inicio + i);
  }
  get paginationStart(): number { return this.total === 0 ? 0 : (this.page - 1) * this.pageSize + 1; }
  get paginationEnd(): number { return Math.min(this.page * this.pageSize, this.total); }

  setPage(page: number): void {
    if (this.loading || page < 1 || page > this.totalPages || page === this.page) return;
    this.page = page;
    this.loadFornecedores();
  }

  openAdd(): void {
    this.modalError = '';
    this.submitted = false;
    this.form = this.emptyForm();
    this.supplierModal.show();
  }

  save(): void {
    if (this.saving) return;
    this.submitted = true;
    this.modalError = '';
    if (!this.isFormValid(this.form)) {
      this.modalError = 'Preencha nome (2 a 150 caracteres), e-mail e telefone (10 ou 11 digitos) do fornecedor e do representante.';
      return;
    }
    this.saving = true;
    this.mutation = this.fornecedorService.add(this.cleanedPayload(this.form)).subscribe({
      next: () => {
        this.saving = false;
        this.supplierModal.hide();
        this.loadFornecedores();
        this.showSuccess('Fornecedor adicionado com sucesso!');
      },
      error: (err) => {
        this.saving = false;
        this.modalError = this.mensagemErro(err, 'Erro ao salvar fornecedor.');
      }
    });
  }


  openView(f: Fornecedor): void {
    this.detailRequest?.unsubscribe();
    this.detailRequest = this.fornecedorService.getById(f.id).subscribe({
      next: atual => { this.fornecedorEmVisualizacao = atual; this.viewModal.show(); },
      error: err => this.showError(this.mensagemErro(err, 'Erro ao consultar fornecedor.'))
    });
  }


  openEditModal(f: Fornecedor): void {
    this.editError = '';
    this.submittedEdit = false;
    this.fornecedorEditandoId = f.id;
    this.formEdit = {
      nome: f.nome,
      email: f.email,
      telefone: f.telefone,
      representante: {
        nomeRepresentante:     f.representante?.nomeRepresentante     ?? '',
        telefoneRepresentante: f.representante?.telefoneRepresentante ?? '',
        emailRepresentante:    f.representante?.emailRepresentante    ?? ''
      }
    };
    this.editModal.show();
  }

  saveEdit(): void {
    if (this.saving) return;
    this.submittedEdit = true;
    this.editError = '';
    if (!this.fornecedorEditandoId || !this.isFormValid(this.formEdit)) {
      this.editError = 'Confira os dados do fornecedor e todos os campos do representante.';
      return;
    }
    this.saving = true;
    this.mutation = this.fornecedorService.update(this.fornecedorEditandoId, this.cleanedPayload(this.formEdit)).subscribe({
      next: () => {
        this.saving = false;
        this.editModal.hide();
        this.fornecedorEditandoId = null;
        this.loadFornecedores();
        this.showSuccess('Fornecedor atualizado com sucesso!');
      },
      error: (err) => {
        this.saving = false;
        this.editError = this.mensagemErro(err, 'Erro ao atualizar fornecedor.');
      }
    });
  }


  onSwitchClick(event: Event, f: Fornecedor): void {
    event.preventDefault();
    if (this.changingStatus) return;
    if (f.ativo) {
      this.openDesativar(f);
    } else {
      this.ativarFornecedor(f.id);
    }
  }

  ativarFornecedor(id: number): void {
    if (this.changingStatus) return;
    this.changingStatus = true;
    this.mutation = this.fornecedorService.enable(id).subscribe({
      next: () => {
        this.loadFornecedores();
        this.changingStatus = false;
        this.showSuccess('Fornecedor ativado com sucesso!');
      },
      error: (err) => {
        this.changingStatus = false;
        this.showError(this.mensagemErro(err, 'Erro ao ativar fornecedor.'));
      }
    });
  }

  openDesativar(f: Fornecedor): void {
    this.statusError = '';
    this.fornecedorParaDesativar = f;
    this.deleteModal.show();
  }

  confirmDesativar(): void {
    if (!this.fornecedorParaDesativar || this.changingStatus) return;
    this.changingStatus = true;
    this.mutation = this.fornecedorService.disable(this.fornecedorParaDesativar.id).subscribe({
      next: () => {
        this.changingStatus = false;
        this.deleteModal.hide();
        this.fornecedorParaDesativar = null;
        this.loadFornecedores();
        this.showSuccess('Fornecedor desativado com sucesso!');
      },
      error: (err) => {
        this.changingStatus = false;
        this.statusError = this.mensagemErro(err, 'Erro ao desativar fornecedor.');
      }
    });
  }


  applyPhoneMask(event: Event, maxDigits: number, isEdit = false): void {
    const targetForm = isEdit ? this.formEdit : this.form;
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/\D/g, '').slice(0, 11);
    input.value = digits.replace(/^(\d{2})(\d)/, '($1) $2');
    input.value = input.value.replace(digits.length > 10 ? /(\d{5})(\d)/ : /(\d{4})(\d)/, '$1-$2');

    if (maxDigits === 10) {
      targetForm.telefone = input.value;
    } else {
      targetForm.representante.telefoneRepresentante = input.value;
    }
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

  isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
  }

  private isFormValid(form: FornecedorForm): boolean {
    const nome = (v: string) => v.trim().length >= 2 && v.trim().length <= 150 && !/[\p{Cc}\p{Cf}]/u.test(v);
    const email = (v: string) => v.trim().length <= 150 && this.isValidEmail(v);
    const telefone = (v: string) => /^\d{10,11}$/.test(v.replace(/\D/g, ''));
    return nome(form.nome) && email(form.email) && telefone(form.telefone)
      && nome(form.representante.nomeRepresentante) && telefone(form.representante.telefoneRepresentante)
      && email(form.representante.emailRepresentante);
  }

  private mensagemErro(err: any, fallback: string): string {
    return Object.values(err.error?.erros ?? {}).join(' ') || err.error?.mensagem
      || (err.status === 0 ? 'Não foi possível conectar ao servidor.' : fallback);
  }

  private cleanedPayload(form: FornecedorForm): FornecedorForm {
    return {
      ...form,
      telefone: form.telefone.replace(/\D/g, ''),
      representante: {
        ...form.representante,
        telefoneRepresentante: form.representante.telefoneRepresentante.replace(/\D/g, '')
      }
    };
  }

  private emptyForm(): FornecedorForm {
    return {
      nome: '',
      email: '',
      telefone: '',
      representante: { nomeRepresentante: '', telefoneRepresentante: '', emailRepresentante: '' }
    };
  }
}
