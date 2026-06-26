import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type PerfilUsuario = 'Administrador' | 'Gerente' | 'Operador' | 'Visualizador';

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: PerfilUsuario;
  modulos: string;
  ativo: boolean;
  ultimoAcesso: string;
}

interface FormUsuario {
  nome: string;
  email: string;
  perfil: PerfilUsuario | '';
  modulos: string;
}

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.scss'
})
export class UsuariosComponent implements AfterViewInit {
  @ViewChild('usuarioModal') usuarioModalEl!: ElementRef;
  @ViewChild('deleteModal') deleteModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Administração', route: '/administracao' },
    { label: 'Usuários e Permissões' }
  ];

  usuarios: Usuario[] = [];
  searchTerm = '';
  filtroStatus = 'todos';
  page = 1;
  pageSize = 8;
  isEditing = false;
  submitted = false;
  successMessage = '';

  form: FormUsuario = this.emptyForm();
  editandoId: number | null = null;
  usuarioParaExcluir: Usuario | null = null;

  private usuarioModal?: any;
  private deleteModal?: any;

  ngAfterViewInit(): void {
    this.usuarioModal = new bootstrap.Modal(this.usuarioModalEl.nativeElement);
    this.deleteModal  = new bootstrap.Modal(this.deleteModalEl.nativeElement);
  }

  get filtrados(): Usuario[] {
    let lista = [...this.usuarios];
    if (this.filtroStatus === 'ativos')   lista = lista.filter(u => u.ativo);
    if (this.filtroStatus === 'inativos') lista = lista.filter(u => !u.ativo);
    const term = this.searchTerm.trim().toLowerCase();
    if (term) lista = lista.filter(u =>
      u.nome.toLowerCase().includes(term) ||
      u.email.toLowerCase().includes(term) ||
      u.perfil.toLowerCase().includes(term)
    );
    return lista;
  }

  get paged(): Usuario[] {
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

  openAdd(): void {
    this.isEditing = false;
    this.submitted = false;
    this.form = this.emptyForm();
    this.usuarioModal.show();
  }

  openEdit(u: Usuario): void {
    this.isEditing = true;
    this.submitted = false;
    this.editandoId = u.id;
    this.form = { nome: u.nome, email: u.email, perfil: u.perfil, modulos: u.modulos };
    this.usuarioModal.show();
  }

  openDelete(u: Usuario): void {
    this.usuarioParaExcluir = u;
    this.deleteModal.show();
  }

  save(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;
    if (this.isEditing && this.editandoId) {
      const idx = this.usuarios.findIndex(u => u.id === this.editandoId);
      if (idx > -1) Object.assign(this.usuarios[idx], this.form);
      this.showSuccess('Usuário atualizado com sucesso!');
    } else {
      const newId = this.usuarios.length ? Math.max(...this.usuarios.map(u => u.id)) + 1 : 1;
      this.usuarios.push({ ...this.form, id: newId, ativo: true, ultimoAcesso: '—' } as Usuario);
      this.showSuccess('Usuário cadastrado com sucesso!');
    }
    this.usuarioModal.hide();
  }

  toggleAtivo(u: Usuario): void {
    u.ativo = !u.ativo;
    this.showSuccess(`Usuário ${u.ativo ? 'ativado' : 'desativado'} com sucesso!`);
  }

  confirmDelete(): void {
    if (!this.usuarioParaExcluir) return;
    this.usuarios = this.usuarios.filter(u => u.id !== this.usuarioParaExcluir!.id);
    this.usuarioParaExcluir = null;
    this.deleteModal.hide();
    this.showSuccess('Usuário removido com sucesso!');
  }

  perfilBadge(perfil: PerfilUsuario): string {
    const map: Record<PerfilUsuario, string> = {
      'Administrador': 'bg-danger',
      'Gerente':       'bg-warning text-dark',
      'Operador':      'bg-primary',
      'Visualizador':  'bg-secondary'
    };
    return map[perfil] ?? 'bg-secondary';
  }

  private isFormValid(): boolean {
    return !!(this.form.nome.trim() && this.form.email.trim() && this.form.perfil &&
              /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.form.email));
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private emptyForm(): FormUsuario {
    return { nome: '', email: '', perfil: '', modulos: '' };
  }
}
