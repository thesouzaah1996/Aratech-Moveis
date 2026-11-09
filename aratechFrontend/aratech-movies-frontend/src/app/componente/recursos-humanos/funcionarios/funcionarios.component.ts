import { PermissaoDirective } from '../../../core/permissao.directive';
import { loginCorporativoValido } from '../../../core/login-corporativo';
import { SessaoState } from '../../../core/services/sessao-state.service';
import { Component, ElementRef, AfterViewInit, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs';
import { criarModal, ModalDaTela } from '../../../core/ui/modal';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { FuncionarioService } from '../../../core/services/funcionario.service';
import { Funcionario, FuncionarioForm, ArquivosFuncionario } from '../../../core/models/funcionario.model';

declare const bootstrap: any;

const TIPOS_FUNCIONARIO = ['GERENTE', 'ENCARREGADO', 'PRODUCAO', 'ADMINISTRATIVO', 'TERCEIRIZADO', 'PRESTADOR_PJ'];

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
  imports: [PermissaoDirective,FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './funcionarios.component.html',
  styleUrl: './funcionarios.component.scss'
})
export class FuncionariosComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('admissaoModal') admissaoModalEl!: ElementRef;
  @ViewChild('viewModal')     viewModalEl!: ElementRef;
  @ViewChild('editModal')     editModalEl!: ElementRef;
  @ViewChild('emailModal') emailModalEl!: ElementRef<HTMLElement>;
  funcionarioEmail: Funcionario | null = null;
  emailCorporativo = ''; senhaTemporaria = ''; erroEmail = ''; criandoEmail = false;
  private emailModal?: ModalDaTela;
  private emailRequest?: Subscription;
  @ViewChild('statusModal') statusModalEl!: ElementRef<HTMLElement>;
  funcionarioStatus: Funcionario | null = null;
  ativarStatus = false;
  alterandoStatus = false;
  erroStatus = '';
  private statusModal?: ModalDaTela;
  private statusRequest?: Subscription;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início',           route: '/dashboard' },
    { label: 'Recursos Humanos', route: '/recursos-humanos' },
    { label: 'Funcionários' }
  ];

  readonly ufs = ['AC','AL','AM','AP','BA','CE','DF','ES','GO','MA','MG','MS','MT',
                  'PA','PB','PE','PI','PR','RJ','RN','RO','RR','RS','SC','SE','SP','TO'];
  readonly bancos = ['Banco do Brasil','Bradesco','Caixa Econômica Federal','Itaú','Nubank',
                     'Santander','Sicoob','Sicredi','Inter','C6 Bank','Outro'];
  readonly estadosCivis = [
    {value: 'SOLTEIRO', label: 'Solteiro(a)'}, {value: 'CASADO', label: 'Casado(a)'},
    {value: 'DIVORCIADO', label: 'Divorciado(a)'}, {value: 'VIUVO', label: 'Viúvo(a)'},
    {value: 'SEPARADO_JUDICIALMENTE', label: 'Separado(a) judicialmente'}
  ];
  readonly tiposContrato = [
    {value: 'CLT', label: 'CLT'}, {value: 'TEMPORARIO', label: 'Temporário'},
    {value: 'APRENDIZ', label: 'Aprendiz'}, {value: 'ESTAGIO', label: 'Estágio'}, {value: 'PJ', label: 'PJ'}
  ];
  readonly tiposFuncionario = TIPOS_FUNCIONARIO;

  funcionarios: Funcionario[] = [];
  loading = false;
  searchTerm = '';
  filtroStatus: 'todos' | 'ativos' | 'inativos' = 'todos';
  page = 1;
  pageSize = 8;
  successMessage = '';
  errorMessage = '';
  funcionarioEmVisualizacao: Funcionario | null = null;

  activeTab = 0;
  tabSubmitted = [false, false, false, false, false];
  form: FormFuncionario = this.emptyForm();
  arquivos: ArquivosFuncionario = this.emptyArquivos();
  salvandoCadastro = false;
  erroCadastro = '';

  submittedEdit = false;
  formEdit: FormFuncionarioEdit = this.emptyFormEdit();
  private editandoId: number | null = null;

  private admissaoModal?: any;
  private viewModal?: any;
  private editModal?: any;

  constructor(private funcionarioService: FuncionarioService, public sessao: SessaoState = new SessaoState()) {}

  ngOnInit(): void {
    this.loading = true;
    this.funcionarioService.getAll().subscribe({
      next: funcionarios => {
        this.funcionarios = funcionarios;
        this.loading = false;
      },
      error: () => {
        this.showError('Erro ao carregar os funcionários.');
        this.loading = false;
      }
    });
  }

  ngAfterViewInit(): void {
    this.admissaoModal = new bootstrap.Modal(this.admissaoModalEl.nativeElement);
    this.viewModal     = new bootstrap.Modal(this.viewModalEl.nativeElement);
    this.editModal     = new bootstrap.Modal(this.editModalEl.nativeElement);
    this.emailModal = criarModal(this.emailModalEl.nativeElement);
    this.statusModal = criarModal(this.statusModalEl.nativeElement);
  }
  ngOnDestroy(): void { this.emailRequest?.unsubscribe(); this.emailModal?.dispose(); this.statusRequest?.unsubscribe(); this.statusModal?.dispose(); }

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
    this.erroCadastro = '';
    this.form = this.emptyForm();
    this.admissaoModalEl.nativeElement.querySelectorAll('input[type=file]').forEach((input: HTMLInputElement) => input.value = '');
    this.arquivos = this.emptyArquivos();
    this.activeTab = 0;
    this.tabSubmitted = [false, false, false, false, false];
    this.admissaoModal.show();
  }

  nextTab(): void {
    if (this.salvandoCadastro) return;
    this.tabSubmitted[this.activeTab] = true;
    if (!this.isTabValid(this.activeTab)) {
      this.erroCadastro = this.erroEtapa(this.activeTab);
      return;
    }
    this.erroCadastro = '';
    this.activeTab++;
  }

  prevTab(): void { if (!this.salvandoCadastro && this.activeTab > 0) this.activeTab--; }

  goToTab(i: number): void {
    if (this.salvandoCadastro) return;
    if (i > this.activeTab) {
      for (let tab = 0; tab < i; tab++) {
        this.tabSubmitted[tab] = true;
        if (!this.isTabValid(tab)) {
          this.activeTab = tab;
          this.erroCadastro = this.erroEtapa(tab);
          return;
        }
      }
    }
    this.erroCadastro = '';
    this.activeTab = i;
  }

  save(): void {
    if (this.salvandoCadastro) return;
    this.erroCadastro = '';
    this.tabSubmitted.fill(true);
    const invalida = this.tabSubmitted.findIndex((_, tab) => !this.isTabValid(tab));
    if (invalida !== -1) {
      this.activeTab = invalida;
      this.erroCadastro = this.erroEtapa(invalida);
      return;
    }

    this.salvandoCadastro = true;
    this.funcionarioService.add(this.montarPayloadCadastro(), this.arquivos).subscribe({
      next: novo => {
        this.funcionarios.unshift(novo);
        this.page = 1;
        this.searchTerm = '';
        this.filtroStatus = 'todos';
        this.salvandoCadastro = false;
        this.admissaoModal.hide();
        this.showSuccess('Funcionário cadastrado com sucesso!');
      },
      error: (err) => {
        this.salvandoCadastro = false;
        const campos = Object.values(err.error?.erros ?? {}).join(' ');
        this.erroCadastro = campos || err.error?.mensagem || (err.status === 0
          ? 'Não foi possível conectar ao servidor. Tente novamente.' : 'Erro ao cadastrar funcionário.');
      }
    });
  }

  private montarPayloadCadastro(): FuncionarioForm {
    const f = this.form;
    return {
      ...f,
      jornadaHoras: String(f.jornadaHoras ?? '').trim() ? Number(f.jornadaHoras) : null,
      salario: this.parseMoeda(f.salario)
    };
  }

  private parseMoeda(valor: string): number {
    return Number(valor.replace(/\./g, '').replace(',', '.')) || 0;
  }

  get errosIdentificacao(): Partial<Record<keyof FormFuncionario, string>> {
    const f = this.form;
    const erros: Partial<Record<keyof FormFuncionario, string>> = {};
    const tamanho = (v: string, min: number, max: number) => v.trim().length >= min && v.trim().length <= max;
    if (!tamanho(f.nome, 3, 150)) erros.nome = 'Nome completo: informe de 3 a 150 caracteres.';
    if (!this.cpfValido(f.cpf.replace(/\D/g, ''))) erros.cpf = 'CPF inválido. Confira os 11 dígitos, incluindo os verificadores.';
    if (f.rg.trim().length > 20) erros.rg = 'RG: informe no máximo 20 caracteres.';
    if (f.pis.trim() && f.pis.replace(/\D/g, '').length !== 11) erros.pis = 'PIS / NIT / NIS: informe os 11 dígitos ou deixe em branco.';
    if (!this.nascimentoValido(f.dataNascimento)) erros.dataNascimento = 'Data de nascimento: informe uma data válida anterior a hoje.';
    if (!['MASCULINO', 'FEMININO', 'NAO_INFORMADO'].includes(f.sexo)) erros.sexo = 'Selecione o sexo ou a opção Não informado.';
    if (f.estadoCivil && !this.estadosCivis.some(e => e.value === f.estadoCivil)) erros.estadoCivil = 'Selecione um estado civil válido ou deixe em branco.';
    if (!tamanho(f.nomeMae, 3, 150)) erros.nomeMae = 'Nome da mãe: informe de 3 a 150 caracteres.';
    if (f.nomePai.trim().length > 150) erros.nomePai = 'Nome do pai: informe no máximo 150 caracteres.';
    return erros;
  }

  private nascimentoValido(valor: string): boolean {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(valor)) return false;
    const [ano, mes, dia] = valor.split('-').map(Number);
    const data = new Date(0);
    data.setFullYear(ano, mes - 1, dia);
    data.setHours(0, 0, 0, 0);
    if (ano < 1 || data.getFullYear() !== ano || data.getMonth() !== mes - 1 || data.getDate() !== dia) return false;
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    return data.getTime() < hoje.getTime();
  }

  isTabValid(tab: number): boolean {
    const f = this.form;
    const digits = (v: string) => v.replace(/\D/g, '');
    const text = (v: string, min: number, max: number) => v.trim().length >= min && v.trim().length <= max;
    switch (tab) {
      case 0: return Object.keys(this.errosIdentificacao).length === 0;
      case 1: return digits(f.cep).length === 8 && text(f.logradouro, 3, 150)
        && text(f.numero, 1, 15) && f.complemento.trim().length <= 100
        && text(f.bairro, 2, 100) && text(f.cidade, 2, 100) && this.ufs.includes(f.uf)
        && this.isValidEmail(f.email) && f.email.trim().length <= 150
        && (!f.telefone || digits(f.telefone).length === 10)
        && (!f.celular || digits(f.celular).length === 11);
      case 2: return !!f.dataAdmissao && f.dataAdmissao > f.dataNascimento
        && this.tiposContrato.some(t => t.value === f.tipoContrato)
        && (!String(f.jornadaHoras ?? '').trim() || (Number.isInteger(Number(f.jornadaHoras))
          && Number(f.jornadaHoras) >= 1 && Number(f.jornadaHoras) <= 44))
        && text(f.cargo, 2, 100) && this.tiposFuncionario.includes(f.tipoFuncionario)
        && text(f.setor, 2, 100) && this.parseMoeda(f.salario) >= 0.01
        && this.parseMoeda(f.salario) < 10000000000;
      case 3: return text(f.banco, 1, 100) && /^[0-9]{1,5}(-[0-9Xx])?$/.test(f.agencia.trim())
        && /^[0-9]{1,12}(-[0-9Xx])?$/.test(f.conta.trim())
        && ['CORRENTE', 'POUPANCA'].includes(f.tipoConta) && f.pix.trim().length <= 150;
      case 4: return !!(this.arquivos.rg && this.arquivos.ctps && this.arquivos.aso && this.arquivos.compResidencia)
        && Object.values(this.arquivos).every(file => !file || !this.erroArquivo(file))
        && Object.values(this.arquivos).reduce((total, file) => total + (file?.size ?? 0), 0) <= 20 * 1024 * 1024;
      default: return false;
    }
  }

  private cpfValido(cpf: string): boolean {
    if (!/^\d{11}$/.test(cpf) || /^(\d)\1{10}$/.test(cpf)) return false;
    for (let pos = 9; pos <= 10; pos++) {
      let soma = 0;
      for (let i = 0; i < pos; i++) soma += Number(cpf[i]) * (pos + 1 - i);
      const resto = soma % 11;
      if (Number(cpf[pos]) !== (resto < 2 ? 0 : 11 - resto)) return false;
    }
    return true;
  }

  private erroEtapa(tab: number): string {
    if (tab === 0) return Object.values(this.errosIdentificacao).join(' ');
    return [
      '',
      'Confira o endereço, e-mail, telefone fixo (10 dígitos) e celular (11 dígitos).',
      'Informe admissão posterior ao nascimento, contrato, cargo, tipo, setor e salário maior que zero. Jornada: 1 a 44 horas.',
      'Confira banco, agência, conta e tipo de conta. Use apenas números e, se houver, o dígito após o hífen.',
      'Envie os quatro documentos obrigatórios. Limites: 5 MB por arquivo e 20 MB no total.'
    ][tab];
  }

  private erroArquivo(file: File): string {
    if (!/^[A-Za-z0-9][A-Za-z0-9 _-]{0,199}\.(pdf|png|jpe?g)$/i.test(file.name)
        || /^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]) *\./i.test(file.name)) {
      return 'Use PDF, JPG ou PNG com nome simples, sem acentos ou pontos extras. Exemplo: documento_1.pdf.';
    }
    return file.size <= 0 || file.size > 5 * 1024 * 1024 ? 'O arquivo deve ter conteúdo e no máximo 5 MB.' : '';
  }

  inv(tab: number, cond: boolean): boolean {
    return this.tabSubmitted[tab] && cond;
  }

  tabHasError(tab: number): boolean {
    return this.tabSubmitted[tab] && !this.isTabValid(tab);
  }

  onFileChange(event: Event, field: keyof ArquivosFuncionario): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const erro = this.erroArquivo(file);
    const total = Object.entries(this.arquivos).reduce((soma, [chave, valor]) =>
      soma + (chave === field ? 0 : valor?.size ?? 0), file.size);
    if (erro || total > 20 * 1024 * 1024) {
      this.erroCadastro = erro || 'Os documentos juntos devem ter no máximo 20 MB.';
      input.value = '';
      this.arquivos[field] = null;
      return;
    }
    this.arquivos[field] = file;
    this.erroCadastro = '';
  }

  nomeArquivo(field: keyof ArquivosFuncionario): string {
    const f = (this.arquivos as any)[field] as File | null;
    return f ? f.name : '';
  }

  openView(f: Funcionario): void { this.funcionarioEmVisualizacao = f; this.viewModal.show(); }

  openEdit(f: Funcionario): void {
    this.submittedEdit = false;
    this.editandoId = f.id;
    this.formEdit = { nome: f.nome, cargo: f.cargo,
                      setor: f.setor, email: f.email, telefone: f.telefone ?? '' };
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

  abrirEmail(f: Funcionario): void {
    if (this.criandoEmail || !f.ativo) return;
    this.funcionarioEmail = f; this.emailCorporativo = f.emailCorporativo ?? '';
    this.senhaTemporaria = ''; this.erroEmail = ''; this.emailModal?.show();
  }
  fecharEmail(): void {
    if (this.criandoEmail) return;
    this.emailModal?.hide(); this.senhaTemporaria = ''; this.funcionarioEmail = null;
  }
  criarEmailCorporativo(): void {
    const f = this.funcionarioEmail;
    if (!f || this.criandoEmail || this.senhaTemporaria) return;
    if (!loginCorporativoValido(this.emailCorporativo)) {
      this.erroEmail = 'Use nome@aratechmoveis, com até 64 caracteres antes do @.'; return;
    }
    this.criandoEmail = true; this.erroEmail = '';
    this.emailRequest = this.funcionarioService.atribuirEmailCorporativo(f.id, this.emailCorporativo).subscribe({
      next: r => { f.emailCorporativo = r.emailCorporativo; this.emailCorporativo = r.emailCorporativo; this.senhaTemporaria = r.senhaTemporaria; this.criandoEmail = false; },
      error: e => { this.criandoEmail = false; this.erroEmail = e.error?.mensagem ?? 'Não foi possível criar o acesso corporativo.'; }
    });
  }

  toggleAtivo(f: Funcionario): void {
    if (this.alterandoStatus) return;
    this.funcionarioStatus = f;
    this.ativarStatus = !f.ativo;
    this.erroStatus = '';
    this.statusModal?.show();
  }

  cancelarStatus(): void {
    if (this.alterandoStatus) return;
    this.statusModal?.hide();
    this.funcionarioStatus = null;
    this.erroStatus = '';
  }

  confirmarStatus(): void {
    const f = this.funcionarioStatus;
    if (!f || this.alterandoStatus) return;
    this.alterandoStatus = true;
    this.erroStatus = '';
    const acao = this.ativarStatus ? this.funcionarioService.enable(f.id) : this.funcionarioService.disable(f.id);

    this.statusRequest = acao.subscribe({
      next: atualizado => {
        f.ativo = atualizado.ativo;
        this.alterandoStatus = false;
        this.statusModal?.hide();
        this.funcionarioStatus = null;
        this.page = Math.max(1, Math.min(this.page, this.totalPages));
        this.showSuccess(`Funcionário ${f.ativo ? 'ativado' : 'desativado'} com sucesso!`);
      },
      error: (err) => {
        this.alterandoStatus = false;
        this.erroStatus = err.error?.mensagem ?? 'Não foi possível alterar o status do funcionário. Tente novamente.';
      }
    });
  }

  applyCpfMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    const d = el.value.replace(/\D/g, '').slice(0, 11);
    el.value = d.replace(/^(\d{3})(\d)/, '$1.$2').replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
      .replace(/(\d{3})\.(\d{3})\.(\d{3})(\d)/, '$1.$2.$3-$4');
    this.form.cpf = el.value;
  }

  applyRgMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    const d = el.value.replace(/\D/g, '').slice(0, 9);
    el.value = d.replace(/^(\d{2})(\d)/, '$1.$2')
      .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
      .replace(/(\d{2})\.(\d{3})\.(\d{3})(\d)/, '$1.$2.$3-$4');
    this.form.rg = el.value;
  }

  applyPisMask(event: Event): void {
    const el = event.target as HTMLInputElement;
    const d = el.value.replace(/\D/g, '').slice(0, 11);
    el.value = d.replace(/^(\d{3})(\d)/, '$1.$2')
      .replace(/^(\d{3})\.(\d{5})(\d)/, '$1.$2.$3')
      .replace(/(\d{3})\.(\d{5})\.(\d{2})(\d)/, '$1.$2.$3-$4');
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
    const d = el.value.replace(/\D/g, '').slice(0, field === 'telefone' ? 10 : 11);
    el.value = d.replace(/^(\d{2})(\d)/, '($1) $2');
    if (field === 'telefone') el.value = el.value.replace(/(\d{4})(\d)/, '$1-$2');
    else el.value = el.value.replace(/(\d{5})(\d)/, '$1-$2');
    this.form[field] = el.value;
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
    this.errorMessage = '';
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private showError(msg: string): void {
    this.errorMessage = msg;
    this.successMessage = '';
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

  private emptyArquivos(): ArquivosFuncionario {
    return { rg: null, ctps: null, aso: null, compResidencia: null,
             tituloEleitor: null, reservista: null,
             diplomaEscolar: null, certidaoCasamento: null, certidaoNascimento: null };
  }

  private emptyFormEdit(): FormFuncionarioEdit {
    return { nome: '', cargo: '', setor: '', email: '', telefone: '' };
  }
}
