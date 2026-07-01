import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export interface Funcionario {
  id: number;
  nome: string;
  cargo: string;
  setor: string;
  email: string;
  telefone: string;
  ativo: boolean;
}

interface FormFuncionario {
  nome: string;
  cpf: string;
  rg: string;
  pis: string;
  dataNascimento: string;
  sexo: string;
  estadoCivil: string;
  nomeMae: string;
  nomePai: string;
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
  telefone: string;
  celular: string;
  dataAdmissao: string;
  cargo: string;
  tipoFuncionario: string;
  setor: string;
  tipoContrato: string;
  salario: string;
  jornadaHoras: string;
  email: string;
  banco: string;
  agencia: string;
  conta: string;
  tipoConta: string;
  pix: string;
}

interface ArquivosAdmissao {
  rg: File | null;
  ctps: File | null;
  aso: File | null;
  compResidencia: File | null;
  foto: File | null;
  tituloEleitor: File | null;
  reservista: File | null;
  diplomaEscolar: File | null;
  certidaoCasamento: File | null;
}

interface FormFuncionarioEdit {
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
  @ViewChild('admissaoModal') admissaoModalEl!: ElementRef;
  @ViewChild('viewModal')     viewModalEl!: ElementRef;
  @ViewChild('editModal')     editModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início',           route: '/dashboard' },
    { label: 'Recursos Humanos', route: '/recursos-humanos' },
    { label: 'Funcionários' }
  ];

  readonly ufs = ['AC','AL','AM','AP','BA','CE','DF','ES','GO','MA','MG','MS','MT',
                  'PA','PB','PE','PI','PR','RJ','RN','RO','RR','RS','SC','SE','SP','TO'];
  readonly bancos = ['Banco do Brasil','Bradesco','Caixa Econômica Federal','Itaú','Nubank',
                     'Santander','Sicoob','Sicredi','Inter','C6 Bank','Outro'];
  readonly estadosCivis = ['Solteiro(a)','Casado(a)','Divorciado(a)','Viúvo(a)','União Estável'];
  readonly tiposContrato = ['CLT','Temporário','Aprendiz','Estágio','PJ'];
  tiposFuncionario: string[] = [];

  funcionarios: Funcionario[] = [];
  searchTerm = '';
  filtroStatus: 'todos' | 'ativos' | 'inativos' = 'todos';
  page = 1;
  pageSize = 8;
  successMessage = '';
  funcionarioEmVisualizacao: Funcionario | null = null;

  activeTab = 0;
  tabSubmitted = [false, false, false, false, false];
  form: FormFuncionario = this.emptyForm();
  arquivos: ArquivosAdmissao = this.emptyArquivos();

  submittedEdit = false;
  formEdit: FormFuncionarioEdit = this.emptyFormEdit();
  private editandoId: number | null = null;

  private admissaoModal?: any;
  private viewModal?: any;
  private editModal?: any;

  ngAfterViewInit(): void {
    this.admissaoModal = new bootstrap.Modal(this.admissaoModalEl.nativeElement);
    this.viewModal     = new bootstrap.Modal(this.viewModalEl.nativeElement);
    this.editModal     = new bootstrap.Modal(this.editModalEl.nativeElement);
  }

  get filtrados(): Funcionario[] {
    let lista = [...this.funcionarios];
    if (this.filtroStatus === 'ativos')   lista = lista.filter(f => f.ativo);
    if (this.filtroStatus === 'inativos') lista = lista.filter(f => !f.ativo);
    const term = this.searchTerm.trim().toLowerCase();
    if (term) lista = lista.filter(f =>
      f.nome.toLowerCase().includes(term) ||
      f.setor.toLowerCase().includes(term)
    );
    return lista;
  }

  get paged(): Funcionario[] {
    const s = (this.page - 1) * this.pageSize;
    return this.filtrados.slice(s, s + this.pageSize);
  }

  get totalPages(): number { return Math.ceil(this.filtrados.length / this.pageSize); }

  get visiblePages(): number[] {
    const s = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    const e = Math.min(this.totalPages, s + 4);
    return Array.from({ length: e - s + 1 }, (_, i) => s + i);
  }

  get paginationStart(): number { return this.filtrados.length === 0 ? 0 : (this.page - 1) * this.pageSize + 1; }
  get paginationEnd(): number   { return Math.min(this.page * this.pageSize, this.filtrados.length); }

  setPage(p: number): void { if (p >= 1 && p <= this.totalPages) this.page = p; }
  setFiltroStatus(s: 'todos' | 'ativos' | 'inativos'): void { this.filtroStatus = s; this.page = 1; }
  onSearch(): void { this.page = 1; }

  openAdd(): void {
    this.form = this.emptyForm();
    this.arquivos = this.emptyArquivos();
    this.activeTab = 0;
    this.tabSubmitted = [false, false, false, false, false];
    this.admissaoModal.show();
  }

  nextTab(): void {
    this.tabSubmitted[this.activeTab] = true;
    if (!this.isTabValid(this.activeTab)) return;
    this.activeTab++;
  }

  prevTab(): void { if (this.activeTab > 0) this.activeTab--; }

  goToTab(i: number): void {
    if (i > this.activeTab) {
      this.tabSubmitted[this.activeTab] = true;
      if (!this.isTabValid(this.activeTab)) return;
    }
    this.activeTab = i;
  }

  save(): void {
    this.tabSubmitted[4] = true;
    if (!this.isTabValid(4)) return;
    const newId = this.funcionarios.length ? Math.max(...this.funcionarios.map(f => f.id)) + 1 : 1;
    this.funcionarios.push({
      id: newId,
      nome: this.form.nome,
      cargo: this.form.cargo,
      setor: this.form.setor,
      email: this.form.email,
      telefone: this.form.celular || this.form.telefone,
      ativo: true
    });
    this.admissaoModal.hide();
    this.showSuccess('Funcionário cadastrado com sucesso!');
  }

  isTabValid(_tab: number): boolean {
    return true;
  }

  inv(tab: number, cond: boolean): boolean {
    return this.tabSubmitted[tab] && cond;
  }

  tabHasError(tab: number): boolean {
    return this.tabSubmitted[tab] && !this.isTabValid(tab);
  }

  onFileChange(event: Event, field: keyof ArquivosAdmissao): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) (this.arquivos as any)[field] = input.files[0];
  }

  nomeArquivo(field: keyof ArquivosAdmissao): string {
    const f = (this.arquivos as any)[field] as File | null;
    return f ? f.name : '';
  }

  openView(f: Funcionario): void { this.funcionarioEmVisualizacao = f; this.viewModal.show(); }

  openEdit(f: Funcionario): void {
    this.submittedEdit = false;
    this.editandoId = f.id;
    this.formEdit = { nome: f.nome, cargo: f.cargo,
                      setor: f.setor, email: f.email, telefone: f.telefone };
    this.editModal.show();
  }

  saveEdit(): void {
    this.submittedEdit = true;
    const fe = this.formEdit;
    if (!fe.nome.trim() || !fe.cargo.trim() ||
        !fe.setor.trim() || !fe.email.trim() || !this.isValidEmail(fe.email)) return;
    const idx = this.funcionarios.findIndex(f => f.id === this.editandoId);
    if (idx > -1) Object.assign(this.funcionarios[idx], fe);
    this.editModal.hide();
    this.showSuccess('Funcionário atualizado com sucesso!');
  }

  toggleAtivo(f: Funcionario): void {
    f.ativo = !f.ativo;
    this.showSuccess(`Funcionário ${f.ativo ? 'ativado' : 'desativado'} com sucesso!`);
  }

  applyCpfMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    const d = el.value.replace(/\D/g, '').slice(0, 11);
    el.value = d.replace(/(\d{3})(\d{3})(\d{3})(\d{0,2})/, '$1.$2.$3-$4')
                .replace(/(\d{3})(\d{3})(\d{0,3})$/, '$1.$2.$3')
                .replace(/(\d{3})(\d{0,3})$/, '$1.$2');
    this.form.cpf = el.value;
  }

  applyRgMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    const d = el.value.replace(/\D/g, '').slice(0, 9);
    el.value = d.replace(/(\d{2})(\d{3})(\d{3})(\d{0,1})/, '$1.$2.$3-$4')
                .replace(/(\d{2})(\d{3})(\d{0,3})$/, '$1.$2.$3')
                .replace(/(\d{2})(\d{0,3})$/, '$1.$2');
    this.form.rg = el.value;
  }

  applyPisMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    const d = el.value.replace(/\D/g, '').slice(0, 11);
    el.value = d.replace(/(\d{3})(\d{5})(\d{2})(\d{0,1})/, '$1.$2.$3-$4')
                .replace(/(\d{3})(\d{5})(\d{0,2})$/, '$1.$2.$3')
                .replace(/(\d{3})(\d{0,5})$/, '$1.$2');
    this.form.pis = el.value;
  }

  applyCepMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    const d = el.value.replace(/\D/g, '').slice(0, 8);
    el.value = d.replace(/(\d{5})(\d{0,3})/, '$1-$2');
    this.form.cep = el.value;
  }

  applySalarioMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    const d = el.value.replace(/\D/g, '');
    if (!d) { el.value = ''; this.form.salario = ''; return; }
    el.value = (parseInt(d, 10) / 100).toLocaleString('pt-BR', { minimumFractionDigits: 2 });
    this.form.salario = el.value;
  }

  applyPhoneMask(event: Event, field: 'telefone' | 'celular'): void {
    const el = event.target as HTMLInputElement;
    const d = el.value.replace(/\D/g, '').slice(0, 11);
    if (d.length > 7)      el.value = d.replace(/(\d{2})(\d{5})(\d{0,4})/, '($1) $2-$3');
    else if (d.length > 2) el.value = d.replace(/(\d{2})(\d{0,5})/, '($1) $2');
    else if (d.length > 0) el.value = '(' + d;
    (this.form as any)[field] = el.value;
  }

  applyPhoneEditMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    const d = el.value.replace(/\D/g, '').slice(0, 11);
    if (d.length > 7)      el.value = d.replace(/(\d{2})(\d{5})(\d{0,4})/, '($1) $2-$3');
    else if (d.length > 2) el.value = d.replace(/(\d{2})(\d{0,5})/, '($1) $2');
    else if (d.length > 0) el.value = '(' + d;
    this.formEdit.telefone = el.value;
  }

  isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private emptyForm(): FormFuncionario {
    return {
      nome: '', cpf: '', rg: '', pis: '', dataNascimento: '',
      sexo: '', estadoCivil: '', nomeMae: '', nomePai: '',
      cep: '', logradouro: '', numero: '', complemento: '',
      bairro: '', cidade: '', uf: '', telefone: '', celular: '',
      dataAdmissao: '', cargo: '', tipoFuncionario: '',
      setor: '', tipoContrato: '', salario: '', jornadaHoras: '', email: '',
      banco: '', agencia: '', conta: '', tipoConta: '', pix: ''
    };
  }

  private emptyArquivos(): ArquivosAdmissao {
    return { rg: null, ctps: null, aso: null, compResidencia: null,
             foto: null, tituloEleitor: null, reservista: null,
             diplomaEscolar: null, certidaoCasamento: null };
  }

  private emptyFormEdit(): FormFuncionarioEdit {
    return { nome: '', cargo: '', setor: '', email: '', telefone: '' };
  }
}
