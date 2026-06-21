import { Component, ElementRef, AfterViewInit, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { FornecedorService } from '../../../core/services/fornecedor.service';
import { Fornecedor, FornecedorForm } from '../../../core/models/fornecedor.model';

declare const bootstrap: any;

@Component({
  selector: 'app-fornecedores',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './fornecedores.component.html',
  styleUrl: './fornecedores.component.scss'
})
export class FornecedoresComponent implements OnInit, AfterViewInit {
  @ViewChild('supplierModal') supplierModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Almoxarifado', route: '/almoxarifado' },
    { label: 'Fornecedores' }
  ];

  fornecedores: Fornecedor[] = [];
  page = 1;
  pageSize = 10;
  loading = false;
  successMessage = '';
  errorMessage = '';

  form: FornecedorForm = this.emptyForm();
  fornecedorParaDesativar: Fornecedor | null = null;

  private supplierModal?: any;
  private deleteModal?: any;

  constructor(private fornecedorService: FornecedorService) {}

  ngOnInit(): void {
    this.loadFornecedores();
  }

  ngAfterViewInit(): void {
    this.supplierModal = new bootstrap.Modal(this.supplierModalEl.nativeElement);
    this.deleteModal   = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  loadFornecedores(): void {
    this.loading = true;
    this.fornecedorService.getAll().subscribe({
      next: (lista) => {
        this.fornecedores = lista;
        this.loading = false;
      },
      error: () => {
        this.showError('Erro ao carregar fornecedores.');
        this.loading = false;
      }
    });
  }

  get paged(): Fornecedor[] {
    const start = (this.page - 1) * this.pageSize;
    return this.fornecedores.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.fornecedores.length / this.pageSize);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  openAdd(): void {
    this.form = this.emptyForm();
    this.supplierModal.show();
  }

  save(): void {
    this.fornecedorService.add(this.form).subscribe({
      next: () => {
        this.supplierModal.hide();
        this.loadFornecedores();
        this.showSuccess('Fornecedor adicionado com sucesso!');
      },
      error: (err) => {
        this.showError(err.error?.message ?? 'Erro ao salvar fornecedor.');
      }
    });
  }

  openDesativar(f: Fornecedor): void {
    this.fornecedorParaDesativar = f;
    this.deleteModal.show();
  }

  confirmDesativar(): void {
    if (!this.fornecedorParaDesativar) return;

    this.fornecedorService.disable(this.fornecedorParaDesativar.id).subscribe({
      next: () => {
        this.deleteModal.hide();
        this.fornecedorParaDesativar = null;
        this.loadFornecedores();
        this.showSuccess('Fornecedor desativado com sucesso!');
      },
      error: (err) => {
        this.showError(err.error?.message ?? 'Erro ao desativar fornecedor.');
      }
    });
  }

  setPage(p: number): void {
    if (p < 1 || p > this.totalPages) return;
    this.page = p;
  }

  applyPhoneMask(event: Event, maxDigits: number): void {
    const input = event.target as HTMLInputElement;
    let digits = input.value.replace(/\D/g, '').slice(0, maxDigits);
    const isMobile = maxDigits === 11;

    if (digits.length > (isMobile ? 7 : 6)) {
      const mid = isMobile ? 5 : 4;
      input.value = digits.replace(new RegExp(`^(\\d{2})(\\d{${mid}})(\\d{0,4})`), '($1) $2-$3');
    } else if (digits.length > 2) {
      input.value = digits.replace(/^(\d{2})(\d{0,5})/, '($1) $2');
    } else if (digits.length > 0) {
      input.value = '(' + digits;
    }

    if (maxDigits === 10) {
      this.form.telefone = input.value;
    } else {
      this.form.representante.telefoneRepresentante = input.value;
    }
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

  private emptyForm(): FornecedorForm {
    return {
      nome: '',
      email: '',
      telefone: '',
      representante: { nomeRepresentante: '', telefoneRepresentante: '', emailRepresentante: '' }
    };
  }
}
