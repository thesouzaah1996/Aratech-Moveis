import { Component } from '@angular/core';
import { NavbarComponent } from '../navbar/navbar.component';
import { FooterComponent } from '../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../breadcrumb/breadcrumb.component';

interface DadosPerfil {
  nome: string;
  email: string;
  telefone: string;
  celular: string;
  matricula: string;
  cargo: string;
  setor: string;
  gestor: string;
  dataAdmissao: string;
  tipoContrato: string;
  perfil: string;
  ultimoAcesso: string;
  ativo: boolean;
}

@Component({
  selector: 'app-meu-perfil',
  standalone: true,
  imports: [NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './meu-perfil.component.html',
  styleUrl: './meu-perfil.component.scss'
})
export class MeuPerfilComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Meu Perfil' }
  ];

  dados: DadosPerfil = {
    nome: '',
    email: '',
    telefone: '',
    celular: '',
    matricula: '',
    cargo: '',
    setor: '',
    gestor: '',
    dataAdmissao: '',
    tipoContrato: '',
    perfil: '',
    ultimoAcesso: '',
    ativo: true
  };

  get inicial(): string {
    return this.dados.nome ? this.dados.nome.charAt(0).toUpperCase() : '?';
  }

  get perfilBadgeClass(): string {
    const map: Record<string, string> = {
      Administrador: 'badge-perfil-admin',
      Gerente: 'badge-perfil-gerente',
      Operador: 'badge-perfil-operador',
      Visualizador: 'badge-perfil-visualizador'
    };
    return map[this.dados.perfil] ?? 'badge-perfil-visualizador';
  }
}
