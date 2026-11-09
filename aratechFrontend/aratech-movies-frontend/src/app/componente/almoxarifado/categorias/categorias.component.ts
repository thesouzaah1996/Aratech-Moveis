import { PermissaoDirective } from '../../../core/permissao.directive';
import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription, debounceTime } from 'rxjs';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { CategoriaService } from '../../../core/services/categoria.service';
import { Categoria } from '../../../core/models/categoria.model';

declare const bootstrap: any;

@Component({
  selector: 'app-categorias', standalone: true,
  imports: [PermissaoDirective,FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './categorias.component.html', styleUrl: './categorias.component.scss'
})
export class CategoriasComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('categoryModal') categoryModalEl!: ElementRef;
  @ViewChild('statusModal') statusModalEl!: ElementRef;
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' }, { label: 'Almoxarifado', route: '/almoxarifado' }, { label: 'Categorias' }
  ];
  categorias: Categoria[] = [];
  loading = false;
  searchQuery = '';
  successMessage = '';
  errorMessage = '';
  modalError = '';
  statusError = '';
  isEditing = false;
  saving = false;
  changingStatus = false;
  page = 1;
  pageSize = 8;
  total = 0;
  form: Partial<Categoria> = { nome: '' };
  categoriaParaStatus: Categoria | null = null;
  private categoryModal?: any;
  private statusModal?: any;
  private request?: Subscription;
  private mutation?: Subscription;
  private search = new Subject<void>();
  private searchSubscription?: Subscription;

  constructor(private categoriaService: CategoriaService) {}

  ngOnInit(): void {
    this.searchSubscription = this.search.pipe(debounceTime(300)).subscribe(() => this.carregar());
    this.carregar();
  }

  ngAfterViewInit(): void {
    this.categoryModal = new bootstrap.Modal(this.categoryModalEl.nativeElement);
    this.statusModal = new bootstrap.Modal(this.statusModalEl.nativeElement);
  }

  ngOnDestroy(): void {
    this.request?.unsubscribe();
    this.mutation?.unsubscribe();
    this.searchSubscription?.unsubscribe();
    this.search.complete();
    this.categoryModal?.dispose();
    this.statusModal?.dispose();
  }

  carregar(): void {
    this.request?.unsubscribe();
    this.loading = true;
    this.errorMessage = '';
    this.request = this.categoriaService.getAll(this.page - 1, this.pageSize, this.searchQuery).subscribe({
      next: data => {
        this.total = data.total;
        const ultima = Math.max(1, Math.ceil(data.total / data.tamanho));
        if (this.page > ultima) {
          this.page = ultima;
          this.carregar();
          return;
        }
        this.categorias = data.categorias;
        this.page = data.pagina + 1;
        this.pageSize = data.tamanho;
        this.loading = false;
      },
      error: err => {
        this.categorias = [];
        this.total = 0;
        this.errorMessage = this.mensagemErro(err, 'Erro ao carregar categorias.');
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.request?.unsubscribe();
    this.page = 1;
    this.loading = true;
    this.search.next();
  }

  get totalPages(): number { return Math.ceil(this.total / this.pageSize); }
  get inicio(): number { return this.total === 0 ? 0 : (this.page - 1) * this.pageSize + 1; }
  get fim(): number { return Math.min(this.page * this.pageSize, this.total); }
  get pages(): number[] {
    const inicio = Math.max(1, Math.min(this.page - 2, this.totalPages - 4));
    return Array.from({length: Math.max(0, Math.min(5, this.totalPages))}, (_, i) => inicio + i);
  }

  setPage(page: number): void {
    if (this.loading || page < 1 || page > this.totalPages || page === this.page) return;
    this.page = page;
    this.carregar();
  }

  openAdd(): void {
    this.isEditing = false;
    this.modalError = '';
    this.form = { nome: '' };
    this.categoryModal.show();
  }

  openEdit(categoria: Categoria): void {
    this.isEditing = true;
    this.modalError = '';
    this.form = { id: categoria.id, nome: categoria.nome };
    this.categoryModal.show();
  }

  save(): void {
    if (this.saving) return;
    const nome = (this.form.nome ?? '').trim().replace(/ +/g, ' ');
    if (nome.length < 2 || nome.length > 100 || /[\p{Cc}\p{Cf}]/u.test(nome)) {
      this.modalError = 'Informe um nome de 2 a 100 caracteres, sem caracteres de controle.';
      return;
    }
    this.saving = true;
    this.modalError = '';
    const operacao = this.isEditing ? this.categoriaService.update(this.form.id!, nome) : this.categoriaService.add(nome);
    this.mutation = operacao.subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = this.isEditing ? 'Categoria atualizada com sucesso.' : 'Categoria criada com sucesso.';
        this.categoryModal.hide();
        this.carregar();
      },
      error: err => {
        this.saving = false;
        this.modalError = this.mensagemErro(err, 'Erro ao salvar categoria.');
      }
    });
  }

  openStatus(categoria: Categoria): void {
    this.categoriaParaStatus = categoria;
    this.statusError = '';
    this.statusModal.show();
  }

  confirmStatus(): void {
    if (!this.categoriaParaStatus || this.changingStatus) return;
    const categoria = this.categoriaParaStatus;
    this.changingStatus = true;
    const operacao = categoria.ativo ? this.categoriaService.disable(categoria.id) : this.categoriaService.enable(categoria.id);
    this.mutation = operacao.subscribe({
      next: () => {
        this.changingStatus = false;
        this.successMessage = categoria.ativo ? 'Categoria desativada com sucesso.' : 'Categoria ativada com sucesso.';
        this.statusModal.hide();
        this.categoriaParaStatus = null;
        this.carregar();
      },
      error: err => {
        this.changingStatus = false;
        this.statusError = this.mensagemErro(err, 'Erro ao alterar o status da categoria.');
      }
    });
  }

  private mensagemErro(err: any, fallback: string): string {
    return Object.values(err.error?.erros ?? {}).join(' ') || err.error?.mensagem
      || (err.status === 0 ? 'Não foi possível conectar ao servidor.' : fallback);
  }
}
