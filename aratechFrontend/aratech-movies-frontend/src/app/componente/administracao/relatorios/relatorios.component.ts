import { Component } from '@angular/core';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

@Component({
  selector: 'app-relatorios',
  standalone: true,
  imports: [NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './relatorios.component.html',
  styleUrl: './relatorios.component.scss'
})
export class RelatoriosComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Administração', route: '/administracao' },
    { label: 'Relatórios Gerenciais' }
  ];

  exportar(tipo: string): void {
    alert(`Exportação de "${tipo}" será implementada com o backend.`);
  }
}
