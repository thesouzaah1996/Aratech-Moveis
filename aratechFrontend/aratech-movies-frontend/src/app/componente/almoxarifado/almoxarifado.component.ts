import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { FooterComponent } from '../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../breadcrumb/breadcrumb.component';
import { AuthService } from '../../core/services/auth.service';
import { PRODUTOS_ALMOXARIFADO_ROLES } from '../../core/config/role-permissions';

@Component({
  selector: 'app-almoxarifado',
  standalone: true,
  imports: [RouterLink, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './almoxarifado.component.html',
  styleUrl: './almoxarifado.component.scss'
})
export class AlmoxarifadoComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Almoxarifado' }
  ];

  constructor(private authService: AuthService) {}

  get podeVerProdutos(): boolean {
    return this.authService.hasAnyRole(PRODUTOS_ALMOXARIFADO_ROLES);
  }
}
