import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../breadcrumb/breadcrumb.component';

@Component({
  selector: 'app-assistencia',
  standalone: true,
  imports: [RouterLink, NavbarComponent, BreadcrumbComponent],
  templateUrl: './assistencia.component.html',
  styleUrl: './assistencia.component.scss'
})
export class AssistenciaTecnicaComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Assistência Técnica' }
  ];
}
