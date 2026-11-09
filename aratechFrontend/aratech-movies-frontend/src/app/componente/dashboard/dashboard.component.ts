import { PermissaoDirective } from '../../core/permissao.directive';
import { Component, inject } from '@angular/core';
import { SessaoState } from '../../core/services/sessao-state.service';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { SerraIconComponent } from '../fabricacao/serra-icon.component';
import { FuradeiraIconComponent } from '../fabricacao/furadeira-icon.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [PermissaoDirective,RouterLink, NavbarComponent,SerraIconComponent,FuradeiraIconComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent { readonly acesso = inject(SessaoState); }
