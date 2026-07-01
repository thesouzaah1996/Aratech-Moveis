import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

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

interface Permissao {
  id: string;
  modulo: string;
  acao: string;
}

const PERMISSOES_DISPONIVEIS: Permissao[] = [
  { id: 'alm.view',   modulo: 'Almoxarifado',     acao: 'Visualizar'   },
  { id: 'alm.create', modulo: 'Almoxarifado',     acao: 'Criar'        },
  { id: 'alm.edit',   modulo: 'Almoxarifado',     acao: 'Editar'       },
  { id: 'alm.delete', modulo: 'Almoxarifado',     acao: 'Excluir'      },
  { id: 'rh.view',    modulo: 'Recursos Humanos', acao: 'Visualizar'   },
  { id: 'rh.create',  modulo: 'Recursos Humanos', acao: 'Criar'        },
  { id: 'rh.edit',    modulo: 'Recursos Humanos', acao: 'Editar'       },
  { id: 'rh.delete',  modulo: 'Recursos Humanos', acao: 'Excluir'      },
  { id: 'fin.view',   modulo: 'Financeiro',       acao: 'Visualizar'   },
  { id: 'fin.create', modulo: 'Financeiro',       acao: 'Criar'        },
  { id: 'fin.edit',   modulo: 'Financeiro',       acao: 'Editar'       },
  { id: 'fin.delete', modulo: 'Financeiro',       acao: 'Excluir'      },
  { id: 'pcp.view',   modulo: 'PCP',              acao: 'Visualizar'   },
  { id: 'pcp.edit',   modulo: 'PCP',              acao: 'Editar'       },
  { id: 'comp.view',  modulo: 'Compras',          acao: 'Visualizar'   },
  { id: 'comp.edit',  modulo: 'Compras',          acao: 'Editar'       },
  { id: 'comp.aprov', modulo: 'Compras',          acao: 'Aprovar'      },
  { id: 'mnt.view',   modulo: 'Manutenção',       acao: 'Visualizar'   },
  { id: 'mnt.edit',   modulo: 'Manutenção',       acao: 'Editar'       },
  { id: 'port.view',  modulo: 'Portaria',         acao: 'Visualizar'   },
  { id: 'port.edit',  modulo: 'Portaria',         acao: 'Editar'       },
  { id: 'car.view',   modulo: 'Carregamento',     acao: 'Visualizar'   },
  { id: 'car.edit',   modulo: 'Carregamento',     acao: 'Editar'       },
  { id: 'ast.view',   modulo: 'Assistência',      acao: 'Visualizar'   },
  { id: 'ast.edit',   modulo: 'Assistência',      acao: 'Editar'       },
  { id: 'adm.view',   modulo: 'Administração',    acao: 'Visualizar'   },
  { id: 'adm.full',   modulo: 'Administração',    acao: 'Acesso Total' },
];

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.scss'
})
export class UsuariosComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Recursos Humanos', route: '/recursos-humanos' },
    { label: 'Permissões de Usuário' }
  ];

  usuarios: Usuario[] = [];

  searchQuery = '';
  buscando = false;
  naoEncontrado = false;
  usuarioEncontrado: Usuario | null = null;

  permissoesDisponiveis: Permissao[] = PERMISSOES_DISPONIVEIS;
  permissoesSelecionadas = new Set<string>();
  dropdownAberto = false;
  salvando = false;

  successMessage = '';

  buscar(): void {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) return;
    this.buscando = true;
    this.naoEncontrado = false;
    this.usuarioEncontrado = null;
    this.dropdownAberto = false;

    setTimeout(() => {
      const found = this.usuarios.find(u =>
        u.email.toLowerCase().includes(q) ||
        u.nome.toLowerCase().includes(q)
      ) ?? null;
      this.usuarioEncontrado = found;
      this.naoEncontrado = !found;
      this.buscando = false;
      if (found) this.permissoesSelecionadas = new Set();
    }, 400);
  }

  limparBusca(): void {
    this.searchQuery = '';
    this.usuarioEncontrado = null;
    this.naoEncontrado = false;
    this.dropdownAberto = false;
  }

  get modulosUnicos(): string[] {
    return [...new Set(this.permissoesDisponiveis.map(p => p.modulo))];
  }

  permissoesPorModulo(modulo: string): Permissao[] {
    return this.permissoesDisponiveis.filter(p => p.modulo === modulo);
  }

  toggleDropdown(): void { this.dropdownAberto = !this.dropdownAberto; }

  temPermissao(id: string): boolean { return this.permissoesSelecionadas.has(id); }

  togglePermissao(id: string): void {
    const s = new Set(this.permissoesSelecionadas);
    s.has(id) ? s.delete(id) : s.add(id);
    this.permissoesSelecionadas = s;
  }

  toggleModulo(modulo: string): void {
    const perms = this.permissoesPorModulo(modulo);
    const todas = perms.every(p => this.permissoesSelecionadas.has(p.id));
    const s = new Set(this.permissoesSelecionadas);
    perms.forEach(p => todas ? s.delete(p.id) : s.add(p.id));
    this.permissoesSelecionadas = s;
  }

  moduloSelecionado(modulo: string): boolean {
    return this.permissoesPorModulo(modulo).every(p => this.permissoesSelecionadas.has(p.id));
  }

  moduloParcial(modulo: string): boolean {
    const perms = this.permissoesPorModulo(modulo);
    const count = perms.filter(p => this.permissoesSelecionadas.has(p.id)).length;
    return count > 0 && count < perms.length;
  }

  salvarPermissoes(): void {
    this.salvando = true;
    setTimeout(() => {
      this.salvando = false;
      this.dropdownAberto = false;
      this.showSuccess(`Permissões de ${this.usuarioEncontrado?.nome} atualizadas com sucesso!`);
    }, 600);
  }

  perfilBadge(perfil: PerfilUsuario): string {
    return ({ Administrador: 'bg-danger', Gerente: 'bg-warning text-dark', Operador: 'bg-primary', Visualizador: 'bg-secondary' } as Record<string,string>)[perfil] ?? 'bg-secondary';
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3500);
  }
}
