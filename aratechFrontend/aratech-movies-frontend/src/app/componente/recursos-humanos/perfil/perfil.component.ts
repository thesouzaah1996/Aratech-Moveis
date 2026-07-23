import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { PerfilService } from '../../../core/services/perfil.service';
import { Perfil } from '../../../core/models/perfil.model';

declare const bootstrap: any;

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.scss'
})
export class PerfilComponent implements OnInit {
  @ViewChild('perfilModal') perfilModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Recursos Humanos', route: '/recursos-humanos' },
    { label: 'Perfis de Acesso' }
  ];

  perfis: Perfil[] = [];
  loading = false;
  searchQuery = '';
  successMessage = '';
  errorMessage = '';
  isEditing = false;
  salvando = false;
  page = 1;
  pageSize = 8;

  form: Partial<Perfil> = { nome: '' };

  private perfilModal?: any;

  constructor(private readonly perfilService: PerfilService) {}

  ngOnInit(): void {
    this.carregar();
  }

  ngAfterViewInit(): void {
    this.perfilModal = new bootstrap.Modal(this.perfilModalEl.nativeElement);
  }

  private carregar(): void {
    this.loading = true;
    this.perfilService.getAll().subscribe({
      next: data => {
        this.perfis = data;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Não foi possível carregar os perfis.';
        this.loading = false;
      }
    });
  }

  get filtered(): Perfil[] {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) return this.perfis;
    return this.perfis.filter(p => p.nome.toLowerCase().includes(q));
  }

  get paged(): Perfil[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtered.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filtered.length / this.pageSize);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  setPage(p: number): void { this.page = p; }

  openAdd(): void {
    this.isEditing = false;
    this.form = { nome: '' };
    this.perfilModal.show();
  }

  openEdit(perfil: Perfil): void {
    this.isEditing = true;
    this.form = { ...perfil };
    this.perfilModal.show();
  }

  save(): void {
    const nome = this.form.nome?.trim();
    if (!nome) return;

    this.salvando = true;
    this.errorMessage = '';

    const request = this.isEditing
      ? this.perfilService.update(this.form.id!, nome)
      : this.perfilService.add(nome);

    request.subscribe({
      next: () => {
        this.salvando = false;
        this.successMessage = this.isEditing ? 'Perfil atualizado com sucesso.' : 'Perfil criado com sucesso.';
        this.carregar();
        this.perfilModal.hide();
      },
      error: err => {
        this.salvando = false;
        this.errorMessage = err.error?.mensagem ?? 'Não foi possível salvar o perfil.';
      }
    });
  }

  alternarStatus(perfil: Perfil): void {
    const acao = perfil.ativo ? this.perfilService.desativar(perfil.id) : this.perfilService.ativar(perfil.id);

    acao.subscribe({
      next: () => {
        this.successMessage = `Perfil ${perfil.ativo ? 'desativado' : 'ativado'} com sucesso.`;
        this.carregar();
      },
      error: err => {
        this.errorMessage = err.error?.mensagem ?? 'Não foi possível alterar o status do perfil.';
      }
    });
  }
}
