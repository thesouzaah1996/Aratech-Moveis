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
  cpf: string;
  rg: string;
  rgOrgaoEmissor: string;
  rgUf: string;
  dataNascimento: string;
  sexo: string;
  estadoCivil: string;
  nacionalidade: string;
  naturalidade: string;
  nomeMae: string;
  nomePai: string;
  pis: string;
  tituloEleitor: string;
  certificadoReservista: string;
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
  email: string;
  telefone: string;
  telefoneFixo: string;
  dataAdmissao: string;
  cargo: string;
  setor: string;
  tipoContrato: string;
  salario: string;
  jornadaSemanal: string;
  horarioEntrada: string;
  horarioSaida: string;
  banco: string;
  agencia: string;
  conta: string;
  tipoConta: string;
  pix: string;
  ativo: boolean;
  documentos: DocumentosAdmissao;
}

interface DocumentosAdmissao {
  rg: string;
  cpf: string;
  ctps: string;
  compResidencia: string;
  foto: string;
  tituloEleitor: string;
  reservista: string;
  pis: string;
  certidao: string;
  aso: string;
  dadosBancarios: string;
  certidaoDependentes: string;
}

interface FormFuncionario {
  matricula: string;
  nome: string;
  cpf: string;
  rg: string;
  rgOrgaoEmissor: string;
  rgUf: string;
  dataNascimento: string;
  sexo: string;
  estadoCivil: string;
  nacionalidade: string;
  naturalidade: string;
  nomeMae: string;
  nomePai: string;
  pis: string;
  tituloEleitor: string;
  certificadoReservista: string;
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
  email: string;
  telefone: string;
  telefoneFixo: string;
  dataAdmissao: string;
  cargo: string;
  setor: string;
  tipoContrato: string;
  salario: string;
  jornadaSemanal: string;
  horarioEntrada: string;
  horarioSaida: string;
  banco: string;
  agencia: string;
  conta: string;
  tipoConta: string;
  pix: string;
}

interface FormFuncionarioEdit {
  matricula: string;
  nome: string;
  cargo: string;
  setor: string;
  email: string;
  telefone: string;
}

interface ArquivosAdmissao {
  rg: File | null;
  cpf: File | null;
  ctps: File | null;
  compResidencia: File | null;
  foto: File | null;
  tituloEleitor: File | null;
  reservista: File | null;
  pis: File | null;
  certidao: File | null;
  aso: File | null;
  dadosBancarios: File | null;
  certidaoDependentes: File | null;
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

  readonly ufs = ['AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO'];
  readonly bancos = [
    '001 - Banco do Brasil','033 - Santander','077 - Banco Inter',
    '104 - Caixa Econômica Federal','208 - BTG Pactual','237 - Bradesco',
    '260 - Nubank','341 - Itaú','422 - Banco Safra','756 - Sicoob','Outro'
  ];
  readonly estadosCivis = ['Solteiro(a)','Casado(a)','Divorciado(a)','Viúvo(a)','União Estável'];
  readonly tiposContrato = ['CLT - Efetivo','CLT - Experiência','Estágio','Aprendiz','Temporário','PJ / Autônomo'];

  funcionarios: Funcionario[] = [];

  searchTerm = '';
  filtroStatus: 'todos' | 'ativos' | 'inativos' = 'todos';
  page = 1;
  pageSize = 8;
  successMessage = '';
  activeTab = 0;
  tabSubmitted = [false, false, false, false, false];
  submittedEdit = false;

  form: FormFuncionario = this.emptyForm();
  arquivos: ArquivosAdmissao = this.emptyArquivos();
  formEdit: FormFuncionarioEdit = this.emptyFormEdit();
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
    if (term) lista = lista.filter(f =>
      f.nome.toLowerCase().includes(term) ||
      f.matricula.toLowerCase().includes(term) ||
      f.setor.toLowerCase().includes(term)
    );
    return lista;
  }

  get paged(): Funcionario[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtrados.slice(start, start + this.pageSize);
  }

  get totalPages(): number { return Math.ceil(this.filtrados.length / this.pageSize); }

  get visiblePages(): number[] {
    const start = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    const end   = Math.min(this.totalPages, start + 4);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  get paginationStart(): number { return this.filtrados.length === 0 ? 0 : (this.page - 1) * this.pageSize + 1; }
  get paginationEnd(): number   { return Math.min(this.page * this.pageSize, this.filtrados.length); }

  setPage(p: number): void { if (p >= 1 && p <= this.totalPages) this.page = p; }
  setFiltroStatus(s: 'todos' | 'ativos' | 'inativos'): void { this.filtroStatus = s; this.page = 1; }
  onSearch(): void { this.page = 1; }

  openAdd(): void {
    this.activeTab = 0;
    this.tabSubmitted = [false, false, false, false, false];
    this.form = this.emptyForm();
    this.arquivos = this.emptyArquivos();
    this.funcionarioModal.show();
  }

  goToTab(i: number): void { this.activeTab = i; }

  nextTab(): void {
    this.tabSubmitted[this.activeTab] = true;
    if (this.isTabValid(this.activeTab)) this.activeTab++;
  }

  prevTab(): void { if (this.activeTab > 0) this.activeTab--; }

  save(): void {
    this.tabSubmitted = [true, true, true, true, true];
    for (let i = 0; i < 5; i++) {
      if (!this.isTabValid(i)) { this.activeTab = i; return; }
    }
    const newId = this.funcionarios.length ? Math.max(...this.funcionarios.map(f => f.id)) + 1 : 1;
    this.funcionarios.push({
      ...this.form,
      id: newId,
      ativo: true,
      documentos: {
        rg:                  this.arquivos.rg?.name ?? '',
        cpf:                 this.arquivos.cpf?.name ?? '',
        ctps:                this.arquivos.ctps?.name ?? '',
        compResidencia:      this.arquivos.compResidencia?.name ?? '',
        foto:                this.arquivos.foto?.name ?? '',
        tituloEleitor:       this.arquivos.tituloEleitor?.name ?? '',
        reservista:          this.arquivos.reservista?.name ?? '',
        pis:                 this.arquivos.pis?.name ?? '',
        certidao:            this.arquivos.certidao?.name ?? '',
        aso:                 this.arquivos.aso?.name ?? '',
        dadosBancarios:      this.arquivos.dadosBancarios?.name ?? '',
        certidaoDependentes: this.arquivos.certidaoDependentes?.name ?? '',
      }
    });
    this.funcionarioModal.hide();
    this.showSuccess('Funcionário cadastrado com sucesso!');
  }

  openView(f: Funcionario): void { this.funcionarioEmVisualizacao = f; this.viewModal.show(); }

  openEdit(f: Funcionario): void {
    this.submittedEdit = false;
    this.funcionarioEditandoId = f.id;
    this.formEdit = { matricula: f.matricula, nome: f.nome, cargo: f.cargo, setor: f.setor, email: f.email, telefone: f.telefone };
    this.editModal.show();
  }

  saveEdit(): void {
    this.submittedEdit = true;
    if (!this.isFormEditValid() || !this.funcionarioEditandoId) return;
    const idx = this.funcionarios.findIndex(f => f.id === this.funcionarioEditandoId);
    if (idx > -1) Object.assign(this.funcionarios[idx], this.formEdit);
    this.editModal.hide();
    this.showSuccess('Funcionário atualizado com sucesso!');
  }

  toggleAtivo(f: Funcionario): void {
    f.ativo = !f.ativo;
    this.showSuccess(`Funcionário ${f.ativo ? 'ativado' : 'desativado'} com sucesso!`);
  }

  onFileChange(event: Event, field: keyof ArquivosAdmissao): void {
    const input = event.target as HTMLInputElement;
    (this.arquivos as any)[field] = input.files?.[0] ?? null;
  }

  applyCpfMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    let v = el.value.replace(/\D/g, '').slice(0, 11);
    v = v.replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    el.value = v; this.form.cpf = v;
  }

  applyRgMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    let v = el.value.replace(/\D/g, '').slice(0, 9);
    v = v.replace(/(\d{2})(\d)/, '$1.$2').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d{1})$/, '$1-$2');
    el.value = v; this.form.rg = v;
  }

  applyPisMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    let v = el.value.replace(/\D/g, '').slice(0, 11);
    v = v.replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{5})(\d)/, '$1.$2').replace(/(\d{2})(\d{1})$/, '$1-$2');
    el.value = v; this.form.pis = v;
  }

  applyCepMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    let v = el.value.replace(/\D/g, '').slice(0, 8);
    v = v.replace(/(\d{5})(\d)/, '$1-$2');
    el.value = v; this.form.cep = v;
  }

  applySalarioMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    const raw = el.value.replace(/\D/g, '');
    if (!raw) { this.form.salario = ''; return; }
    const num = parseInt(raw, 10) / 100;
    const formatted = num.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    el.value = formatted; this.form.salario = formatted;
  }

  applyPhoneMask(event: Event, isEdit = false): void {
    const el = event.target as HTMLInputElement;
    let d = el.value.replace(/\D/g, '').slice(0, 11);
    if (d.length > 7)      d = d.replace(/^(\d{2})(\d{5})(\d{0,4})/, '($1) $2-$3');
    else if (d.length > 2) d = d.replace(/^(\d{2})(\d{0,5})/, '($1) $2');
    else if (d.length > 0) d = '(' + d;
    el.value = d;
    if (isEdit) this.formEdit.telefone = d; else this.form.telefone = d;
  }

  applyPhoneFixoMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    let d = el.value.replace(/\D/g, '').slice(0, 10);
    if (d.length > 6)      d = d.replace(/^(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
    else if (d.length > 2) d = d.replace(/^(\d{2})(\d{0,4})/, '($1) $2');
    else if (d.length > 0) d = '(' + d;
    el.value = d; this.form.telefoneFixo = d;
  }

  isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
  }

  isTabValid(tab: number): boolean {
    const f = this.form;
    switch (tab) {
      case 0: return !!(f.matricula.trim() && f.nome.trim() && f.cpf.trim() && f.dataNascimento && f.sexo && f.nomeMae.trim());
      case 1: return !!(f.cep.trim() && f.logradouro.trim() && f.numero.trim() && f.bairro.trim() && f.cidade.trim() && f.uf);
      case 2: return !!(f.dataAdmissao && f.cargo.trim() && f.setor.trim() && f.tipoContrato && f.salario.trim() && f.email.trim() && this.isValidEmail(f.email));
      case 3: return !!(f.banco && f.agencia.trim() && f.conta.trim() && f.tipoConta);
      case 4: return !!(this.arquivos.rg && this.arquivos.ctps && this.arquivos.compResidencia && this.arquivos.aso);
      default: return true;
    }
  }

  inv(tab: number, cond: boolean): boolean { return this.tabSubmitted[tab] && cond; }

  tabHasError(tab: number): boolean { return this.tabSubmitted[tab] && !this.isTabValid(tab); }

  private isFormEditValid(): boolean {
    return !!(this.formEdit.matricula.trim() && this.formEdit.nome.trim() &&
              this.formEdit.cargo.trim() && this.formEdit.setor.trim() &&
              this.formEdit.email.trim() && this.isValidEmail(this.formEdit.email));
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3500);
  }

  private emptyForm(): FormFuncionario {
    return {
      matricula: '', nome: '', cpf: '', rg: '', rgOrgaoEmissor: '', rgUf: '',
      dataNascimento: '', sexo: '', estadoCivil: 'Solteiro(a)', nacionalidade: 'Brasileira',
      naturalidade: '', nomeMae: '', nomePai: '', pis: '', tituloEleitor: '', certificadoReservista: '',
      cep: '', logradouro: '', numero: '', complemento: '', bairro: '', cidade: '', uf: '',
      email: '', telefone: '', telefoneFixo: '',
      dataAdmissao: '', cargo: '', setor: '', tipoContrato: '', salario: '',
      jornadaSemanal: '44', horarioEntrada: '08:00', horarioSaida: '17:48',
      banco: '', agencia: '', conta: '', tipoConta: '', pix: '',
    };
  }

  private emptyArquivos(): ArquivosAdmissao {
    return { rg: null, cpf: null, ctps: null, compResidencia: null, foto: null,
             tituloEleitor: null, reservista: null, pis: null, certidao: null,
             aso: null, dadosBancarios: null, certidaoDependentes: null };
  }

  private emptyFormEdit(): FormFuncionarioEdit {
    return { matricula: '', nome: '', cargo: '', setor: '', email: '', telefone: '' };
  }
}
