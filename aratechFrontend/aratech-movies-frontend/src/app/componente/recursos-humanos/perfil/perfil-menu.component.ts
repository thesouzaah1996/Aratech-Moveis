import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

@Component({
  selector: 'app-perfil-menu', standalone: true, imports: [RouterLink, NavbarComponent, BreadcrumbComponent],
  template: `
    <app-navbar />
    <main class="app-main">
      <app-breadcrumb [items]="breadcrumb" />
      <div class="container py-5"><div class="row g-4 justify-content-center">
        <div class="col-md-6 col-lg-5">
          <a routerLink="/recursos-humanos/perfil/criar" class="card card-menu h-100">
            <div class="d-flex flex-column align-items-center justify-content-center h-100">
              <i class="bi bi-shield-lock icon-size"></i><h4 class="fw-bold mb-2">Criar Perfis de Acesso</h4>
              <p class="text-muted text-center mb-0">Cadastre e gerencie os perfis de acesso</p>
            </div>
          </a>
        </div>
        <div class="col-md-6 col-lg-5">
          <a routerLink="/recursos-humanos/perfil/atribuir" class="card card-menu h-100">
            <div class="d-flex flex-column align-items-center justify-content-center h-100">
              <i class="bi bi-person-check icon-size"></i><h4 class="fw-bold mb-2">Atribuir Perfil</h4>
              <p class="text-muted text-center mb-0">Busque o usuário e atribua um perfil de acesso</p>
            </div>
          </a>
        </div>
      </div></div>
    </main>`
})
export class PerfilMenuComponent {
  breadcrumb: BreadcrumbItem[] = [{ label: 'Início', route: '/dashboard' }, { label: 'Recursos Humanos', route: '/recursos-humanos' }, { label: 'Perfis de Acesso' }];
}
