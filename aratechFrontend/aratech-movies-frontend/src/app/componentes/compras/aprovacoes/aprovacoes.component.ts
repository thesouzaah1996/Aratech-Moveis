import { Component } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

export interface PedidoAprovacao {
  id: number;
  numero: string;
  fornecedor: string;
  item: string;
  valor: number;
  solicitante: string;
  dataSolicitacao: string;
  status: 'Aguardando Aprovação' | 'Aprovado' | 'Reprovado';
  justificativa?: string;
}

@Component({
  selector: 'app-aprovacoes',
  standalone: true,
  imports: [CurrencyPipe, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './aprovacoes.component.html',
  styleUrl: './aprovacoes.component.scss'
})
export class AprovacoesComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Compras', route: '/compras' },
    { label: 'Aprovações' }
  ];

  successMessage = '';

  pedidos: PedidoAprovacao[] = [];

  get pendentes(): PedidoAprovacao[] {
    return this.pedidos.filter(p => p.status === 'Aguardando Aprovação');
  }

  get historico(): PedidoAprovacao[] {
    return this.pedidos.filter(p => p.status !== 'Aguardando Aprovação');
  }

  aprovar(p: PedidoAprovacao): void {
    p.status = 'Aprovado';
    this.showSuccess(`Pedido ${p.numero} aprovado.`);
  }

  reprovar(p: PedidoAprovacao): void {
    p.status = 'Reprovado';
    p.justificativa = 'Reprovado manualmente.';
    this.showSuccess(`Pedido ${p.numero} reprovado.`);
  }

  badgeClass(status: string): string {
    const map: Record<string, string> = {
      'Aguardando Aprovação': 'bg-warning text-dark',
      'Aprovado':             'bg-success',
      'Reprovado':            'bg-danger'
    };
    return map[status] ?? 'bg-secondary';
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }
}
