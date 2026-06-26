import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

export interface ProgramacaoItem {
  id: number;
  ordem: string;
  produto: string;
  quantidade: number;
  inicio: string;
  fim: string;
  linha: string;
  operador: string;
}

@Component({
  selector: 'app-programacao',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './programacao.component.html',
  styleUrl: './programacao.component.scss'
})
export class ProgramacaoComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'PCP', route: '/pcp' },
    { label: 'Programação de Produção' }
  ];

  semanaAtual = new Date().toISOString().split('T')[0];
  linhas = ['Linha 1', 'Linha 2', 'Linha 3', 'Linha 4'];
  filtroLinha = '';

  programacao: ProgramacaoItem[] = [];

  get filtrada(): ProgramacaoItem[] {
    if (!this.filtroLinha) return this.programacao;
    return this.programacao.filter(p => p.linha === this.filtroLinha);
  }

  diasSemana(): string[] {
    const base = new Date(this.semanaAtual);
    const dia = base.getDay();
    const seg = new Date(base);
    seg.setDate(base.getDate() - (dia === 0 ? 6 : dia - 1));
    return Array.from({ length: 5 }, (_, i) => {
      const d = new Date(seg);
      d.setDate(seg.getDate() + i);
      return d.toLocaleDateString('pt-BR', { weekday: 'short', day: '2-digit', month: '2-digit' });
    });
  }

  itemNoDia(item: ProgramacaoItem, diaStr: string): boolean {
    return item.inicio.split('T')[0] <= this.semanaAtual && item.fim.split('T')[0] >= this.semanaAtual;
  }
}
