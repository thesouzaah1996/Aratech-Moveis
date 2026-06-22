import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export interface Funcionario {
  id: number;
  matricula: string;
  nome: string;
  cargo: string;
  setor: string;
  email: string;
  telefone: string;
  ativo: boolean;
}

interface FormFuncionario {
  matricula: string;
  nome: string;
  cargo: string;
  setor: string;
  email: string;
  telefone: string;
}

@Component({
  selector: 'app-funcionarios',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './funcionarios.component.html',
  styleUrl: './funcionarios.component.scss'
})
export class FuncionariosComponent implements AfterViewInit {
  @ViewChild('funcionarioModal') funcionarioModalEl!: ElementRef;
  @ViewChild('viewModal')        viewModalEl!: ElementRef;
  @ViewChild('editModal')        editModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Recursos Humanos', route: '/recursos-humanos' },
    { label: 'Funcionários' }
  ];

  // TODO: carregar via serviço quando backend estiver pronto
  funcionarios: Funcionario[] = [];

  searchTerm = '';
  filtroStatus: 'todos' | 'ativos' | 'inativos' = 'todos';
  page = 1;
  pageSize = 8;
  submitted = false;
  submittedEdit = false;
  successMessage = '';
  errorMessage = '';

  form: FormFuncionario = this.emptyForm();
  formEdit: FormFuncionario = this.emptyForm();
  funcionarioEmVisualizacao: Funcionario | null = null;
  private funcionarioEditandoId: number | null = null;

  private funcionarioModal?: any;
  private viewModal?: any;
  private editModal?: any;

  ngAfterViewInit(): void {
    this.funcionarioModal = new bootstrap.Modal(this.funcionarioModalEl.nativeElement);
    this.viewModal        = new bootstrap.Modal(this.viewModalEl.nativeElement);
    this.editModal        = new bootstrap.Modal(this.editModalEl.nativeElement);
  }

  get filtrados(): Funcionario[] {
    let lista = [...this.funcionarios];

    if (this.filtroStatus === 'ativos')   lista = lista.filter(f => f.ativo);
    if (this.filtroStatus === 'inativos') lista = lista.filter(f => !f.ativo);

    const term = this.searchTerm.trim().toLowerCase();
    if (term) {
      lista = lista.filter(f =>
        f.nome.toLowerCase().includes(term) ||
        f.matricula.toLowerCase().includes(term) ||
        f.setor.toLowerCase().includes(term)
      );
    }

    return lista;
  }

  get paged(): Funcionario[] {
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

  setFiltroStatus(s: 'todos' | 'ativos' | 'inativos'): void {
    this.filtroStatus = s;
    this.page = 1;
  }

  onSearch(): void { this.page = 1; }

  openAdd(): void {
    this.submitted = false;
    this.form = this.emptyForm();
    this.funcionarioModal.show();
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid(this.form)) return;
    // TODO: chamar serviço quando backend estiver pronto
    const newId = this.funcionarios.length ? Math.max(...this.funcionarios.map(f => f.id)) + 1 : 1;
    this.funcionarios.push({ ...this.form, id: newId, ativo: true });
    this.funcionarioModal.hide();
    this.showSuccess('Funcionário cadastrado com sucesso!');
  }

  openView(f: Funcionario): void {
    this.funcionarioEmVisualizacao = f;
    this.viewModal.show();
  }

  openEdit(f: Funcionario): void {
    this.submittedEdit = false;
    this.funcionarioEditandoId = f.id;
    this.formEdit = { matricula: f.matricula, nome: f.nome, cargo: f.cargo, setor: f.setor, email: f.email, telefone: f.telefone };
    this.editModal.show();
  }

  saveEdit(): void {
    this.submittedEdit = true;
    if (!this.isFormValid(this.formEdit) || !this.funcionarioEditandoId) return;
    // TODO: chamar serviço quando backend estiver pronto
    const idx = this.funcionarios.findIndex(f => f.id === this.funcionarioEditandoId);
    if (idx > -1) Object.assign(this.funcionarios[idx], this.formEdit);
    this.editModal.hide();
    this.showSuccess('Funcionário atualizado com sucesso!');
  }

  toggleAtivo(f: Funcionario): void {
    // TODO: chamar serviço quando backend estiver pronto
    f.ativo = !f.ativo;
    this.showSuccess(`Funcionário ${f.ativo ? 'ativado' : 'desativado'} com sucesso!`);
  }

  applyPhoneMask(event: Event, isEdit = false): void {
    const input = event.target as HTMLInputElement;
    let digits = input.value.replace(/\D/g, '').slice(0, 11);
    if (digits.length > 7) {
      input.value = digits.replace(/^(\d{2})(\d{5})(\d{0,4})/, '($1) $2-$3');
    } else if (digits.length > 2) {
      input.value = digits.replace(/^(\d{2})(\d{0,5})/, '($1) $2');
    } else if (digits.length > 0) {
      input.value = '(' + digits;
    }
    if (isEdit) this.formEdit.telefone = input.value;
    else        this.form.telefone     = input.value;
  }

  isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
  }

  private isFormValid(f: FormFuncionario): boolean {
    return !!(f.matricula.trim() && f.nome.trim() && f.cargo.trim() && f.setor.trim() &&
              f.email.trim() && this.isValidEmail(f.email));
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    this.errorMessage = '';
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private emptyForm(): FormFuncionario {
    return { matricula: '', nome: '', cargo: '', setor: '', email: '', telefone: '' };
  }
}
