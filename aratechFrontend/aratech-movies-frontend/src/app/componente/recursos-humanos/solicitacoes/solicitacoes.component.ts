import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusSolicitacao = 'Pendente' | 'Em Análise' | 'Aprovada' | 'Recusada';
export type TipoSolicitacao =
  | 'Declaração de Vínculo'
  | 'Atestado de Trabalho'
  | 'Holerite'
  | 'Informe de Rendimentos'
  | 'Alteração de Dados Cadastrais'
  | 'Outros';

export interface Solicitacao {
  id: number;
  funcionario: string;
  matricula: string;
  tipo: TipoSolicitacao;
  descricao: string;
  data: string;
  status: StatusSolicitacao;
}

@Component({
  selector: 'app-solicitacoes',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './solicitacoes.component.html',
  styleUrl: './solicitacoes.component.scss'
})
export class SolicitacoesRhComponent implements AfterViewInit {
  @ViewChild('solicitacaoModal') solicitacaoModalEl!: ElementRef;
  @ViewChild('viewModal')        viewModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Recursos Humanos', route: '/recursos-humanos' },
    { label: 'Solicitações' }
  ];

  solicitacoes: Solicitacao[] = [];

  searchTerm = '';
  filtroStatus: StatusSolicitacao | 'Todos' = 'Todos';
  page = 1;
  pageSize = 8;
  submitted = false;
  successMessage = '';

  form: Partial<Solicitacao> = this.emptyForm();
  solicitacaoEmVisualizacao: Solicitacao | null = null;

  private solicitacaoModal?: any;
  private viewModal?: any;

  ngAfterViewInit(): void {
    this.solicitacaoModal = new bootstrap.Modal(this.solicitacaoModalEl.nativeElement);
    this.viewModal        = new bootstrap.Modal(this.viewModalEl.nativeElement);
  }

  get filtradas(): Solicitacao[] {
    let lista = [...this.solicitacoes];

    if (this.filtroStatus !== 'Todos') lista = lista.filter(s => s.status === this.filtroStatus);

    const term = this.searchTerm.trim().toLowerCase();
    if (term) {
      lista = lista.filter(s =>
        s.funcionario.toLowerCase().includes(term) ||
        s.matricula.toLowerCase().includes(term) ||
        s.id.toString().includes(term)
      );
    }

    return lista;
  }

  get paged(): Solicitacao[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtradas.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filtradas.length / this.pageSize);
  }

  get visiblePages(): number[] {
    const start = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    const end   = Math.min(this.totalPages, start + 4);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  get paginationStart(): number {
    return this.filtradas.length === 0 ? 0 : (this.page - 1) * this.pageSize + 1;
  }

  get paginationEnd(): number {
    return Math.min(this.page * this.pageSize, this.filtradas.length);
  }

  setPage(p: number): void {
    if (p < 1 || p > this.totalPages) return;
    this.page = p;
  }

  setFiltro(s: StatusSolicitacao | 'Todos'): void {
    this.filtroStatus = s;
    this.page = 1;
  }

  openAdd(): void {
    this.submitted = false;
    this.form = this.emptyForm();
    this.solicitacaoModal.show();
  }

  openView(s: Solicitacao): void {
    this.solicitacaoEmVisualizacao = s;
    this.viewModal.show();
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;
    const newId = this.solicitacoes.length ? Math.max(...this.solicitacoes.map(s => s.id)) + 1 : 1;
    this.solicitacoes.push({
      ...this.form,
      id: newId,
      status: 'Pendente',
      data: new Date().toLocaleDateString('pt-BR')
    } as Solicitacao);
    this.solicitacaoModal.hide();
    this.showSuccess('Solicitação registrada com sucesso!');
  }

  statusBadge(status: StatusSolicitacao): string {
    const map: Record<StatusSolicitacao, string> = {
      'Pendente':   'bg-secondary',
      'Em Análise': 'bg-warning text-dark',
      'Aprovada':   'bg-success',
      'Recusada':   'bg-danger'
    };
    return map[status];
  }

  private isFormValid(): boolean {
    return !!(this.form.funcionario?.trim() && this.form.matricula?.trim() && this.form.tipo);
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private emptyForm(): Partial<Solicitacao> {
    return { funcionario: '', matricula: '', tipo: undefined, descricao: '' };
  }
}
