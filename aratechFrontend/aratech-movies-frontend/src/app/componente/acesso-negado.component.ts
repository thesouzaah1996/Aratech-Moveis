import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from './navbar/navbar.component';

@Component({ selector: 'app-acesso-negado', standalone: true, imports: [NavbarComponent, RouterLink], template: `<app-navbar /><main class="app-main"><div class="container py-5 text-center"><i class="bi bi-shield-lock display-4 text-danger"></i><h4 class="mt-3">Acesso não permitido</h4><p class="text-muted">Seu perfil não possui permissão para acessar esta área.</p><a routerLink="/dashboard" class="btn btn-danger">Voltar ao início</a></div></main>` })
export class AcessoNegadoComponent {}

@Component({ selector: 'app-falha-conexao', standalone: true, imports: [RouterLink], template: `<main class="app-main"><div class="container py-5 text-center"><h4>Não foi possível verificar sua sessão</h4><p>Tente novamente quando a conexão for restabelecida.</p><a routerLink="/dashboard" class="btn btn-danger">Tentar novamente</a></div></main>` })
export class FalhaConexaoComponent {}
