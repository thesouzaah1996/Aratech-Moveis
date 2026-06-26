import { Component } from '@angular/core';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

export interface KpiCard {
  titulo: string;
  valor: string;
  unidade: string;
  descricao: string;
  tendencia: 'up' | 'down' | 'neutral';
  cor: string;
}

@Component({
  selector: 'app-kpis',
  standalone: true,
  imports: [NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './kpis.component.html',
  styleUrl: './kpis.component.scss'
})
export class KpisComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'PCP', route: '/pcp' },
    { label: 'KPIs de Produção' }
  ];

  kpis: KpiCard[] = [
    {
      titulo: 'OEE',
      valor: '78',
      unidade: '%',
      descricao: 'Overall Equipment Effectiveness — eficiência global dos equipamentos.',
      tendencia: 'up',
      cor: 'text-success'
    },
    {
      titulo: 'Taxa de Retrabalho',
      valor: '3,2',
      unidade: '%',
      descricao: 'Percentual de peças que precisaram de retrabalho no mês.',
      tendencia: 'down',
      cor: 'text-danger'
    },
    {
      titulo: 'Lead Time Médio',
      valor: '4,5',
      unidade: 'dias',
      descricao: 'Tempo médio entre abertura e fechamento das ordens de produção.',
      tendencia: 'neutral',
      cor: 'text-primary'
    },
    {
      titulo: 'Ordens no Prazo',
      valor: '92',
      unidade: '%',
      descricao: 'Ordens de produção concluídas dentro da data prevista.',
      tendencia: 'up',
      cor: 'text-success'
    },
    {
      titulo: 'Produção do Mês',
      valor: '1.240',
      unidade: 'un',
      descricao: 'Total de unidades produzidas no mês corrente.',
      tendencia: 'up',
      cor: 'text-primary'
    },
    {
      titulo: 'Paradas Não Planejadas',
      valor: '6',
      unidade: 'h',
      descricao: 'Horas totais de parada não planejada na linha de produção.',
      tendencia: 'down',
      cor: 'text-warning'
    }
  ];

  tendenciaIcon(t: 'up' | 'down' | 'neutral'): string {
    return { up: 'bi-arrow-up-circle-fill text-success', down: 'bi-arrow-down-circle-fill text-danger', neutral: 'bi-dash-circle-fill text-secondary' }[t];
  }
}
