import { PermissaoDirective } from '../../core/permissao.directive';
import { Component, inject } from '@angular/core';
import { SessaoState } from '../../core/services/sessao-state.service';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../breadcrumb/breadcrumb.component';

@Component({
  selector: 'app-recursos-humanos',
  standalone: true,
  imports: [PermissaoDirective,RouterLink, NavbarComponent, BreadcrumbComponent],
  templateUrl: './recursos-humanos.component.html',
  styleUrl: './recursos-humanos.component.scss'
})
export class RecursosHumanosComponent {
  readonly acesso = inject(SessaoState);

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Recursos Humanos' }
  ];
}
