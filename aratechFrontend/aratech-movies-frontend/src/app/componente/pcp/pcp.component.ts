import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../breadcrumb/breadcrumb.component';

@Component({
  selector: 'app-pcp',
  standalone: true,
  imports: [RouterLink, NavbarComponent, BreadcrumbComponent],
  templateUrl: './pcp.component.html',
  styleUrl: './pcp.component.scss'
})
export class PcpComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'PCP' }
  ];
}
