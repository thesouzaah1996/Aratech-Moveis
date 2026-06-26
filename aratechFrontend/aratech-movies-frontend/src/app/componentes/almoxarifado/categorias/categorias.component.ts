import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { CategoriaService } from '../../../core/services/categoria.service';

declare const bootstrap: any;

export interface Categoria {
  id: number;
  nome: string;
}

@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './categorias.component.html',
  styleUrl: './categorias.component.scss'
})
export class CategoriasComponent implements OnInit {
  @ViewChild('categoryModal') categoryModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Almoxarifado', route: '/almoxarifado' },
    { label: 'Categorias' }
  ];

  categorias: Categoria[] = [];
  loading = false;
  searchQuery = '';
  successMessage = '';
  errorMessage = '';
  isEditing = false;
  page = 1;
  pageSize = 8;

  form: Partial<Categoria> = { nome: '' };
  categoriaParaExcluir: Categoria | null = null;

  private categoryModal?: any;
  private deleteModal?: any;

  constructor(private categoriaService: CategoriaService) {}

  ngOnInit(): void {
    this.carregar();
  }

  ngAfterViewInit(): void {
    this.categoryModal = new bootstrap.Modal(this.categoryModalEl.nativeElement);
    this.deleteModal   = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  private carregar(): void {
    this.loading = true;
    this.categoriaService.getAll().subscribe({
      next: data => {
        this.categorias = data;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Erro ao carregar categorias.';
        this.loading = false;
      }
    });
  }

  get filtered(): Categoria[] {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) return this.categorias;
    return this.categorias.filter(c => c.nome.toLowerCase().includes(q));
  }

  get paged(): Categoria[] {
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
    this.form = { nome: '' };
    this.categoryModal.show();
  }

  openEdit(cat: Categoria): void {
    this.isEditing = true;
    this.form = { ...cat };
    this.categoryModal.show();
  }

  openDelete(cat: Categoria): void {
    this.categoriaParaExcluir = cat;
    this.deleteModal.show();
  }

  save(): void {
    if (this.isEditing) {
      this.categoriaService.update(this.form.id!, this.form.nome!).subscribe({
        next: () => {
          this.successMessage = 'Categoria atualizada com sucesso.';
          this.carregar();
          this.categoryModal.hide();
        },
        error: () => { this.errorMessage = 'Erro ao atualizar categoria.'; }
      });
    } else {
      this.categoriaService.add(this.form.nome!).subscribe({
        next: () => {
          this.successMessage = 'Categoria criada com sucesso.';
          this.carregar();
          this.categoryModal.hide();
        },
        error: () => { this.errorMessage = 'Erro ao criar categoria.'; }
      });
    }
  }

  confirmDelete(): void {
    if (!this.categoriaParaExcluir) return;
    this.categoriaService.delete(this.categoriaParaExcluir.id).subscribe({
      next: () => {
        this.successMessage = 'Categoria excluída com sucesso.';
        this.carregar();
        this.categoriaParaExcluir = null;
        this.deleteModal.hide();
      },
      error: () => { this.errorMessage = 'Erro ao excluir categoria.'; }
    });
  }

  setPage(p: number): void { this.page = p; }
}
