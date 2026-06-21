import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export interface Produto {
  id: number;
  nome: string;
  sku: string;
  estoque: number;
  categoria: string;
  fornecedor: string;
  descricao: string;
}

@Component({
  selector: 'app-estoque-produtos',
  standalone: true,
  imports: [FormsModule, RouterLink, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './estoque-produtos.component.html',
  styleUrl: './estoque-produtos.component.scss'
})
export class EstoqueProdutosComponent implements OnInit {
  @ViewChild('productModal') productModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Almoxarifado', route: '/almoxarifado' },
    { label: 'Estoque de Produtos' }
  ];

  produtos: Produto[] = [];
  searchQuery = '';
  isEditing = false;
  page = 1;
  pageSize = 8;

  form: Partial<Produto> = this.emptyForm();
  produtoParaExcluir: Produto | null = null;

  private productModal?: any;
  private deleteModal?: any;

  ngOnInit(): void {
    // TODO: injetar ProdutoService e chamar this.produtoService.listar()
  }

  ngAfterViewInit(): void {
    this.productModal = new bootstrap.Modal(this.productModalEl.nativeElement);
    this.deleteModal  = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  get filtered(): Produto[] {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) return this.produtos;
    return this.produtos.filter(p =>
      p.nome.toLowerCase().includes(q) ||
      p.sku.toLowerCase().includes(q) ||
      p.descricao.toLowerCase().includes(q)
    );
  }

  get paged(): Produto[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtered.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filtered.length / this.pageSize);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  openAdd(): void {
    this.isEditing = false;
    this.form = this.emptyForm();
    this.productModal.show();
  }

  openEdit(produto: Produto): void {
    this.isEditing = true;
    this.form = { ...produto };
    this.productModal.show();
  }

  openDelete(produto: Produto): void {
    this.produtoParaExcluir = produto;
    this.deleteModal.show();
  }

  save(): void {
    if (this.isEditing) {
      // TODO: produtoService.atualizar(this.form)
      const idx = this.produtos.findIndex(p => p.id === this.form.id);
      if (idx > -1) this.produtos[idx] = { ...this.form } as Produto;
    } else {
      // TODO: produtoService.criar(this.form)
      const newId = this.produtos.length ? Math.max(...this.produtos.map(p => p.id)) + 1 : 1;
      this.produtos.push({ ...this.form, id: newId } as Produto);
    }
    this.productModal.hide();
  }

  confirmDelete(): void {
    if (!this.produtoParaExcluir) return;
    // TODO: produtoService.excluir(this.produtoParaExcluir.id)
    this.produtos = this.produtos.filter(p => p.id !== this.produtoParaExcluir!.id);
    this.produtoParaExcluir = null;
    this.deleteModal.hide();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.page = 1;
  }

  setPage(p: number): void {
    this.page = p;
  }

  private emptyForm(): Partial<Produto> {
    return { nome: '', sku: '', estoque: 0, categoria: '', fornecedor: '', descricao: '' };
  }
}
