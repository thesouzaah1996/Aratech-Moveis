import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { CategoriaService } from '../../../core/services/categoria.service';
import { ProdutoService } from '../../../core/services/produto.service';
import { Produto, ProdutoForm } from '../../../core/models/produto.model';
import { LookupItem } from '../../../core/models/lookup.model';

declare const bootstrap: any;

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
  categorias: LookupItem[] = [];

  searchQuery = '';
  isEditing = false;
  page = 1;
  pageSize = 8;

  form: ProdutoForm = this.emptyForm();
  produtoParaExcluir: Produto | null = null;

  private productModal?: any;
  private deleteModal?: any;

  constructor(
    private categoriaService: CategoriaService,
    private produtoService: ProdutoService
  ) {}

  ngOnInit(): void {
    this.categoriaService.lookup().subscribe(cats => this.categorias = cats);
    this.produtoService.getAll().subscribe(produtos => this.produtos = produtos);
  }

  ngAfterViewInit(): void {
    this.productModal = new bootstrap.Modal(this.productModalEl.nativeElement);
    this.deleteModal  = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  categoriaNome(id: number | null): string {
    return this.categorias.find(c => c.id === id)?.nome ?? '—';
  }

  get filtered(): Produto[] {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) return this.produtos;
    return this.produtos.filter(p =>
      p.nome.toLowerCase().includes(q) ||
      p.sku.toLowerCase().includes(q) ||
      (p.descricao ?? '').toLowerCase().includes(q)
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
    this.form = {
      id: produto.id,
      categoriaID: produto.categoriaID,
      nome: produto.nome,
      sku: produto.sku,
      quantidade: produto.quantidade,
      descricao: produto.descricao ?? '',
      vencimentoProduto: produto.vencimentoProduto ?? null
    };
    this.productModal.show();
  }

  openDelete(produto: Produto): void {
    this.produtoParaExcluir = produto;
    this.deleteModal.show();
  }

  save(): void {
    if (this.isEditing && this.form.id) {
      this.produtoService.update(this.form.id, this.form).subscribe(updated => {
        const idx = this.produtos.findIndex(p => p.id === updated.id);
        if (idx > -1) this.produtos[idx] = updated;
        this.productModal.hide();
      });
    } else {
      this.produtoService.add(this.form).subscribe(novo => {
        this.produtos.unshift(novo);
        this.productModal.hide();
      });
    }
  }

  confirmDelete(): void {
    if (!this.produtoParaExcluir) return;
    this.produtoService.delete(this.produtoParaExcluir.id).subscribe(() => {
      this.produtos = this.produtos.filter(p => p.id !== this.produtoParaExcluir!.id);
      this.produtoParaExcluir = null;
      this.deleteModal.hide();
    });
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.page = 1;
  }

  setPage(p: number): void {
    this.page = p;
  }

  private emptyForm(): ProdutoForm {
    return { categoriaID: null, nome: '', sku: '', quantidade: 0, descricao: '', vencimentoProduto: null };
  }
}
