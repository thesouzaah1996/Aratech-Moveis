import { HttpClient } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { SessaoState } from '../../core/services/sessao-state.service';
import { NavbarComponent } from '../navbar/navbar.component';
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
  imports: [NavbarComponent, BreadcrumbComponent, DatePipe, RouterLink],
  templateUrl: './meu-perfil.component.html',
  styleUrl: './meu-perfil.component.scss'
})
export class MeuPerfilComponent implements OnInit, OnDestroy {
  loading = false; erro = ""; tecnico = false; acessoAtual = "";
  private request?: Subscription;
  ngOnInit(): void { this.carregar(); }
  carregar(): void {
    this.request?.unsubscribe(); this.loading = true; this.erro = "";
    this.request = this.http.get<Partial<DadosPerfil> & { acessoAtual: string }>("/aratech/auth/meu-perfil").subscribe({ next: d => { Object.assign(this.dados, d); this.acessoAtual = d.acessoAtual; this.loading = false; }, error: e => { this.loading = false; this.erro = e.error?.mensagem ?? "Não foi possível carregar seu perfil."; } });
  }
  ngOnDestroy(): void { this.request?.unsubscribe(); }
  constructor(sessao: SessaoState, private http: HttpClient) {
    const usuario = sessao.usuario();
    this.tecnico = usuario?.id === 0;
    if (usuario) Object.assign(this.dados, { nome: usuario.nome, email: usuario.email, matricula: String(usuario.id), perfil: usuario.administrador ? 'Administrador' : usuario.perfis.join(', ') || 'Sem perfil' });
  }
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
