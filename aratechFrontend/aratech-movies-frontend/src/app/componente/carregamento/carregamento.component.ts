import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { FooterComponent } from '../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../breadcrumb/breadcrumb.component';

@Component({
  selector: 'app-carregamento',
  standalone: true,
  imports: [RouterLink, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './carregamento.component.html',
  styleUrl: './carregamento.component.scss'
})
export class CarregamentoComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Carregamento' }
  ];
}
