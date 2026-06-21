import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

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
  isEditing = false;
  page = 1;
  pageSize = 8;

  form: Partial<Categoria> = { nome: '' };
  categoriaParaExcluir: Categoria | null = null;

  private categoryModal?: any;
  private deleteModal?: any;

  ngOnInit(): void {
    // TODO: injetar CategoriaService e chamar this.categoriaService.listar()
  }

  ngAfterViewInit(): void {
    this.categoryModal = new bootstrap.Modal(this.categoryModalEl.nativeElement);
    this.deleteModal   = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  get paged(): Categoria[] {
    const start = (this.page - 1) * this.pageSize;
    return this.categorias.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.categorias.length / this.pageSize);
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
      // TODO: categoriaService.atualizar(this.form)
      const idx = this.categorias.findIndex(c => c.id === this.form.id);
      if (idx > -1) this.categorias[idx] = { ...this.form } as Categoria;
    } else {
      // TODO: categoriaService.criar(this.form)
      const newId = this.categorias.length ? Math.max(...this.categorias.map(c => c.id)) + 1 : 1;
      this.categorias.push({ ...this.form, id: newId } as Categoria);
    }
    this.categoryModal.hide();
  }

  confirmDelete(): void {
    if (!this.categoriaParaExcluir) return;
    // TODO: categoriaService.excluir(this.categoriaParaExcluir.id)
    this.categorias = this.categorias.filter(c => c.id !== this.categoriaParaExcluir!.id);
    this.categoriaParaExcluir = null;
    this.deleteModal.hide();
  }

  setPage(p: number): void { this.page = p; }
}
