import { Component, ElementRef, AfterViewInit, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { FornecedorService } from '../../../core/services/fornecedor.service';
import { Fornecedor, FornecedorForm } from '../../../core/models/fornecedor.model';

declare const bootstrap: any;

@Component({
  selector: 'app-fornecedores',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './fornecedores.component.html',
  styleUrl: './fornecedores.component.scss'
})
export class FornecedoresComponent implements OnInit, AfterViewInit {
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
    this.loadFornecedores();
  }

  ngAfterViewInit(): void {
    this.supplierModal = new bootstrap.Modal(this.supplierModalEl.nativeElement);
    this.deleteModal   = new bootstrap.Modal(this.deleteModalEl.nativeElement);
    this.viewModal     = new bootstrap.Modal(this.viewModalEl.nativeElement);
    this.editModal     = new bootstrap.Modal(this.editModalEl.nativeElement);
  }

  loadFornecedores(): void {
    this.loading = true;
    this.fornecedorService.getAll().subscribe({
      next: (lista) => {
        this.fornecedores = lista;
        this.loading = false;
      },
      error: () => {
        this.showError('Erro ao carregar fornecedores.');
        this.loading = false;
      }
    });
  }


  get filtrados(): Fornecedor[] {
    let lista = this.fornecedores;

    if (this.filtroStatus === 'ativos') {
      lista = lista.filter(f => f.ativo);
    } else if (this.filtroStatus === 'inativos') {
      lista = lista.filter(f => !f.ativo);
    }

    const term = this.searchTerm.trim().toLowerCase();
    if (!term) return lista;
    return lista.filter(f =>
      f.nome.toLowerCase().includes(term) ||
      f.id.toString().includes(term)
    );
  }

  onSearch(): void {
    this.page = 1;
  }

  setFiltroStatus(status: 'todos' | 'ativos' | 'inativos'): void {
    this.filtroStatus = status;
    this.page = 1;
  }


  get paged(): Fornecedor[] {
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
    this.supplierModal.show();
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid(this.form)) return;
    this.fornecedorService.add(this.cleanedPayload(this.form)).subscribe({
      next: () => {
        this.supplierModal.hide();
        this.loadFornecedores();
        this.showSuccess('Fornecedor adicionado com sucesso!');
      },
      error: (err) => {
        this.showError(err.error?.message ?? 'Erro ao salvar fornecedor.');
      }
    });
  }


  openView(f: Fornecedor): void {
    this.fornecedorEmVisualizacao = f;
    this.viewModal.show();
  }


  openEditModal(f: Fornecedor): void {
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
    this.submittedEdit = true;
    if (!this.fornecedorEditandoId || !this.isFormValid(this.formEdit)) return;
    this.fornecedorService.update(this.fornecedorEditandoId, this.cleanedPayload(this.formEdit)).subscribe({
      next: () => {
        this.editModal.hide();
        this.fornecedorEditandoId = null;
        this.loadFornecedores();
        this.showSuccess('Fornecedor atualizado com sucesso!');
      },
      error: (err) => {
        this.showError(err.error?.message ?? 'Erro ao atualizar fornecedor.');
      }
    });
  }


  onSwitchClick(event: Event, f: Fornecedor): void {
    event.preventDefault();
    if (f.ativo) {
      this.openDesativar(f);
    } else {
      this.ativarFornecedor(f.id);
    }
  }

  ativarFornecedor(id: number): void {
    this.fornecedorService.enable(id).subscribe({
      next: () => {
        this.loadFornecedores();
        this.showSuccess('Fornecedor ativado com sucesso!');
      },
      error: (err) => {
        this.showError(err.error?.message ?? 'Erro ao ativar fornecedor.');
      }
    });
  }

  openDesativar(f: Fornecedor): void {
    this.fornecedorParaDesativar = f;
    this.deleteModal.show();
  }

  confirmDesativar(): void {
    if (!this.fornecedorParaDesativar) return;

    this.fornecedorService.disable(this.fornecedorParaDesativar.id).subscribe({
      next: () => {
        this.deleteModal.hide();
        this.fornecedorParaDesativar = null;
        this.loadFornecedores();
        this.showSuccess('Fornecedor desativado com sucesso!');
      },
      error: (err) => {
        this.showError(err.error?.message ?? 'Erro ao desativar fornecedor.');
      }
    });
  }


  applyPhoneMask(event: Event, maxDigits: number, isEdit = false): void {
    const targetForm = isEdit ? this.formEdit : this.form;
    const input = event.target as HTMLInputElement;
    let digits = input.value.replace(/\D/g, '').slice(0, maxDigits);
    const isMobile = maxDigits === 11;

    if (digits.length > (isMobile ? 7 : 6)) {
      const mid = isMobile ? 5 : 4;
      input.value = digits.replace(new RegExp(`^(\\d{2})(\\d{${mid}})(\\d{0,4})`), '($1) $2-$3');
    } else if (digits.length > 2) {
      input.value = digits.replace(/^(\d{2})(\d{0,5})/, '($1) $2');
    } else if (digits.length > 0) {
      input.value = '(' + digits;
    }

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
    return !!(
      form.nome.trim() &&
      form.email.trim() && this.isValidEmail(form.email) &&
      form.telefone.trim() &&
      form.representante.nomeRepresentante.trim() &&
      form.representante.telefoneRepresentante.trim() &&
      form.representante.emailRepresentante.trim() && this.isValidEmail(form.representante.emailRepresentante)
    );
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
