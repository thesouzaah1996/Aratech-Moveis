import { Component } from '@angular/core';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

export interface LinhaCapacidade {
  nome: string;
  capacidadeTotal: number;
  capacidadeUsada: number;
  operadores: number;
  turno: string;
}

@Component({
  selector: 'app-capacidade',
  standalone: true,
  imports: [NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './capacidade.component.html',
  styleUrl: './capacidade.component.scss'
})
export class CapacidadeComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'PCP', route: '/pcp' },
    { label: 'Capacidade Produtiva' }
  ];

  linhas: LinhaCapacidade[] = [
    { nome: 'Linha 1', capacidadeTotal: 100, capacidadeUsada: 75, operadores: 4, turno: 'Manhã' },
    { nome: 'Linha 2', capacidadeTotal: 100, capacidadeUsada: 90, operadores: 5, turno: 'Manhã' },
    { nome: 'Linha 3', capacidadeTotal: 100, capacidadeUsada: 40, operadores: 3, turno: 'Tarde' },
    { nome: 'Linha 4', capacidadeTotal: 100, capacidadeUsada: 60, operadores: 4, turno: 'Tarde' },
  ];

  percentual(linha: LinhaCapacidade): number {
    return Math.round((linha.capacidadeUsada / linha.capacidadeTotal) * 100);
  }

  barClass(perc: number): string {
    if (perc >= 90) return 'bg-danger';
    if (perc >= 70) return 'bg-warning';
    return 'bg-success';
  }
}
