import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

export interface LogAuditoria {
  id: number;
  dataHora: string;
  usuario: string;
  modulo: string;
  acao: string;
  detalhe: string;
}

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './auditoria.component.html',
  styleUrl: './auditoria.component.scss'
})
export class AuditoriaComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Administração', route: '/administracao' },
    { label: 'Log de Auditoria' }
  ];

  logs: LogAuditoria[] = [];
  filtroModulo = '';
  filtroDataInicio = '';
  filtroDataFim = '';
  searchTerm = '';
  page = 1;
  pageSize = 8;

  get filtrados(): LogAuditoria[] {
    let lista = [...this.logs];
    if (this.filtroModulo) lista = lista.filter(l => l.modulo === this.filtroModulo);
    if (this.filtroDataInicio) lista = lista.filter(l => l.dataHora >= this.filtroDataInicio);
    if (this.filtroDataFim)    lista = lista.filter(l => l.dataHora <= this.filtroDataFim + 'T23:59');
    const term = this.searchTerm.trim().toLowerCase();
    if (term) lista = lista.filter(l =>
      l.usuario.toLowerCase().includes(term) ||
      l.acao.toLowerCase().includes(term) ||
      l.detalhe.toLowerCase().includes(term)
    );
    return lista;
  }

  get paged(): LogAuditoria[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtrados.slice(start, start + this.pageSize);
  }

  get totalPages(): number { return Math.ceil(this.filtrados.length / this.pageSize); }
  get visiblePages(): number[] {
    const start = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    return Array.from({ length: Math.min(this.totalPages, start + 4) - start + 1 }, (_, i) => start + i);
  }
  get paginationStart(): number { return this.filtrados.length === 0 ? 0 : (this.page - 1) * this.pageSize + 1; }
  get paginationEnd(): number { return Math.min(this.page * this.pageSize, this.filtrados.length); }

  setPage(p: number): void { if (p >= 1 && p <= this.totalPages) this.page = p; }

  limparFiltros(): void {
    this.filtroModulo = '';
    this.filtroDataInicio = '';
    this.filtroDataFim = '';
    this.searchTerm = '';
    this.page = 1;
  }

  modulos = ['Almoxarifado', 'Portaria', 'Carregamento', 'Manutenção', 'Assistência', 'RH', 'PCP', 'Compras', 'Financeiro', 'Administração'];
}
