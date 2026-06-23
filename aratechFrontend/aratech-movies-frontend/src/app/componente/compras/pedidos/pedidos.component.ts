import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusPedido = 'Rascunho' | 'Aguardando Aprovação' | 'Aprovado' | 'Recebido' | 'Cancelado';

export interface PedidoCompra {
  id: number;
  numero: string;
  fornecedor: string;
  item: string;
  quantidade: number;
  valorUnitario: number;
  dataPedido: string;
  dataEntrega: string;
  status: StatusPedido;
}

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './pedidos.component.html',
  styleUrl: './pedidos.component.scss'
})
export class PedidosCompraComponent implements AfterViewInit {
  @ViewChild('pedidoModal') pedidoModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Compras', route: '/compras' },
    { label: 'Pedidos de Compra' }
  ];

  pedidos: PedidoCompra[] = [];
  searchTerm = '';
  filtroStatus: StatusPedido | 'Todos' = 'Todos';
  page = 1;
  pageSize = 8;
  isEditing = false;
  submitted = false;
  successMessage = '';

  form: Partial<PedidoCompra> = this.emptyForm();
  pedidoParaExcluir: PedidoCompra | null = null;

  private pedidoModal?: any;
  private deleteModal?: any;

  ngAfterViewInit(): void {
    this.pedidoModal = new bootstrap.Modal(this.pedidoModalEl.nativeElement);
    this.deleteModal = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  get filtrados(): PedidoCompra[] {
    let lista = [...this.pedidos];
    if (this.filtroStatus !== 'Todos') lista = lista.filter(p => p.status === this.filtroStatus);
    const term = this.searchTerm.trim().toLowerCase();
    if (term) lista = lista.filter(p =>
      p.numero.toLowerCase().includes(term) ||
      p.fornecedor.toLowerCase().includes(term) ||
      p.item.toLowerCase().includes(term)
    );
    return lista;
  }

  get paged(): PedidoCompra[] {
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
  get valorTotal(): number { return (this.form.quantidade ?? 0) * (this.form.valorUnitario ?? 0); }

  setPage(p: number): void { if (p >= 1 && p <= this.totalPages) this.page = p; }
  setFiltro(s: string): void { this.filtroStatus = s as StatusPedido | 'Todos'; this.page = 1; }

  openAdd(): void {
    this.isEditing = false;
    this.submitted = false;
    this.form = this.emptyForm();
    this.pedidoModal.show();
  }

  openEdit(p: PedidoCompra): void {
    this.isEditing = true;
    this.submitted = false;
    this.form = { ...p };
    this.pedidoModal.show();
  }

  openDelete(p: PedidoCompra): void {
    this.pedidoParaExcluir = p;
    this.deleteModal.show();
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;
    if (this.isEditing) {
      const idx = this.pedidos.findIndex(p => p.id === this.form.id);
      if (idx > -1) this.pedidos[idx] = { ...this.form } as PedidoCompra;
      this.showSuccess('Pedido atualizado com sucesso!');
    } else {
      const newId = this.pedidos.length ? Math.max(...this.pedidos.map(p => p.id)) + 1 : 1;
      const num = `PC-${String(newId).padStart(4, '0')}`;
      this.pedidos.push({ ...this.form, id: newId, numero: num, status: 'Rascunho' } as PedidoCompra);
      this.showSuccess('Pedido de compra criado com sucesso!');
    }
    this.pedidoModal.hide();
  }

  enviarAprovacao(p: PedidoCompra): void {
    p.status = 'Aguardando Aprovação';
    this.showSuccess(`Pedido ${p.numero} enviado para aprovação.`);
  }

  confirmDelete(): void {
    if (!this.pedidoParaExcluir) return;
    this.pedidos = this.pedidos.filter(p => p.id !== this.pedidoParaExcluir!.id);
    this.pedidoParaExcluir = null;
    this.deleteModal.hide();
    this.showSuccess('Pedido removido com sucesso!');
  }

  badgeClass(status: StatusPedido): string {
    const map: Record<StatusPedido, string> = {
      'Rascunho':              'bg-secondary',
      'Aguardando Aprovação':  'bg-warning text-dark',
      'Aprovado':              'bg-info text-dark',
      'Recebido':              'bg-success',
      'Cancelado':             'bg-danger'
    };
    return map[status] ?? 'bg-secondary';
  }

  private isFormValid(): boolean {
    return !!(this.form.fornecedor?.trim() && this.form.item?.trim() &&
              this.form.quantidade && this.form.quantidade > 0 &&
              this.form.valorUnitario && this.form.valorUnitario > 0 &&
              this.form.dataPedido && this.form.dataEntrega);
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private emptyForm(): Partial<PedidoCompra> {
    return { fornecedor: '', item: '', quantidade: 1, valorUnitario: 0, dataPedido: '', dataEntrega: '' };
  }
}
