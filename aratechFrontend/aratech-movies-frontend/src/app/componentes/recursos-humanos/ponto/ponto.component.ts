import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

export type StatusPonto = 'Presente' | 'Incompleto' | 'Ausente';

export interface RegistroPonto {
  matricula: string;
  funcionario: string;
  cargo: string;
  setor: string;
  entrada: string | null;
  saida: string | null;
  totalHoras: string | null;
  status: StatusPonto;
}

@Component({
  selector: 'app-ponto',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './ponto.component.html',
  styleUrl: './ponto.component.scss'
})
export class PontoComponent implements OnInit {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Recursos Humanos', route: '/recursos-humanos' },
    { label: 'Controle de Ponto' }
  ];

  dataSelecionada: string = '';
  filtroStatus: StatusPonto | 'Todos' = 'Todos';
  searchTerm = '';
  page = 1;
  pageSize = 8;
  sincronizando = false;

  registros: RegistroPonto[] = [];

  ngOnInit(): void {
    this.dataSelecionada = this.hoje();
  }

  get filtrados(): RegistroPonto[] {
    let lista = [...this.registros];

    if (this.filtroStatus !== 'Todos') {
      lista = lista.filter(r => r.status === this.filtroStatus);
    }

    const term = this.searchTerm.trim().toLowerCase();
    if (term) {
      lista = lista.filter(r =>
        r.funcionario.toLowerCase().includes(term) ||
        r.matricula.toLowerCase().includes(term) ||
        r.setor.toLowerCase().includes(term)
      );
    }

    return lista;
  }

  get paged(): RegistroPonto[] {
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

  setFiltro(status: StatusPonto | 'Todos'): void {
    this.filtroStatus = status;
    this.page = 1;
  }

  onDataChange(): void {
    this.page = 1;
  }

  sincronizar(): void {
    this.sincronizando = true;
    setTimeout(() => (this.sincronizando = false), 1500);
  }

  diaAnterior(): void {
    const d = new Date(this.dataSelecionada + 'T00:00:00');
    d.setDate(d.getDate() - 1);
    this.dataSelecionada = d.toISOString().split('T')[0];
    this.onDataChange();
  }

  proximoDia(): void {
    const d = new Date(this.dataSelecionada + 'T00:00:00');
    d.setDate(d.getDate() + 1);
    this.dataSelecionada = d.toISOString().split('T')[0];
    this.onDataChange();
  }

  get isHoje(): boolean {
    return this.dataSelecionada === this.hoje();
  }

  get dataFormatada(): string {
    if (!this.dataSelecionada) return '';
    const [y, m, d] = this.dataSelecionada.split('-');
    return `${d}/${m}/${y}`;
  }

  contarPor(status: StatusPonto): number {
    return this.registros.filter(r => r.status === status).length;
  }

  statusBadge(status: StatusPonto): string {
    if (status === 'Presente')   return 'bg-success';
    if (status === 'Incompleto') return 'bg-warning text-dark';
    return 'bg-danger';
  }

  private hoje(): string {
    return new Date().toISOString().split('T')[0];
  }
}
