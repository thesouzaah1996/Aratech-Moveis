import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../breadcrumb/breadcrumb.component';

@Component({
  selector: 'app-portaria',
  standalone: true,
  imports: [RouterLink, NavbarComponent, BreadcrumbComponent],
  templateUrl: './portaria.component.html',
  styleUrl: './portaria.component.scss'
})
export class PortariaComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Portaria' }
  ];
}
