import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

export interface LinhaCaixa {
  data: string;
  descricao: string;
  tipo: 'Entrada' | 'Saída';
  valor: number;
  saldoAcumulado: number;
}

@Component({
  selector: 'app-fluxo-caixa',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './fluxo-caixa.component.html',
  styleUrl: './fluxo-caixa.component.scss'
})
export class FluxoCaixaComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Financeiro', route: '/financeiro' },
    { label: 'Fluxo de Caixa' }
  ];

  mesSelecionado = new Date().toISOString().slice(0, 7);

  linhas: LinhaCaixa[] = [];

  get totalEntradas(): number {
    return this.linhas.filter(l => l.tipo === 'Entrada').reduce((s, l) => s + l.valor, 0);
  }

  get totalSaidas(): number {
    return this.linhas.filter(l => l.tipo === 'Saída').reduce((s, l) => s + l.valor, 0);
  }

  get saldoFinal(): number {
    return this.totalEntradas - this.totalSaidas;
  }

  tipoClass(tipo: 'Entrada' | 'Saída'): string {
    return tipo === 'Entrada' ? 'text-success fw-semibold' : 'text-danger fw-semibold';
  }

  saldoClass(saldo: number): string {
    return saldo >= 0 ? 'text-success' : 'text-danger';
  }
}
