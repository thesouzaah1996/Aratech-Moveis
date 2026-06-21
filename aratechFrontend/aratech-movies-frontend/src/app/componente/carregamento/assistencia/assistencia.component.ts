import { Component, ElementRef, AfterViewInit, OnInit, ViewChild } from '@angular/core';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusAssistencia = 'Em análise' | 'Em reparo' | 'Aguardando peças' | 'Concluído';

export interface ItemAssistencia {
  id: number;
  cliente: string;
  produto: string;
  descricao: string;
  responsavel: string;
  dataEntrada: string;
  status: StatusAssistencia;
  observacoes?: string;
}

@Component({
  selector: 'app-carregamento-assistencia',
  standalone: true,
  imports: [NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './assistencia.component.html',
  styleUrl: './assistencia.component.scss'
})
export class CarregamentoAssistenciaComponent implements OnInit, AfterViewInit {
  @ViewChild('historicoModal') historicoModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Carregamento', route: '/carregamento' },
    { label: 'Assistência' }
  ];

  itens: ItemAssistencia[] = [];
  historico: ItemAssistencia[] = [];

  private historicoModal?: any;

  ngOnInit(): void {
    // TODO: inject AssistenciaCarregamentoService and call listarAtivos()
    // TODO: inject AssistenciaCarregamentoService and call listarHistorico()
  }

  ngAfterViewInit(): void {
    this.historicoModal = new bootstrap.Modal(this.historicoModalEl.nativeElement);
  }

  abrirHistorico(): void {
    this.historicoModal.show();
  }

  avancarStatus(item: ItemAssistencia): void {
    const fluxo: StatusAssistencia[] = ['Em análise', 'Em reparo', 'Aguardando peças', 'Concluído'];
    const idx = fluxo.indexOf(item.status);
    if (idx < fluxo.length - 1) {
      // TODO: assistenciaService.atualizarStatus(item.id, fluxo[idx + 1])
      item.status = fluxo[idx + 1];
    }
    if (item.status === 'Concluído') {
      this.historico.unshift({ ...item });
      this.itens = this.itens.filter(i => i.id !== item.id);
    }
  }

  badgeClass(status: StatusAssistencia): string {
    const map: Record<StatusAssistencia, string> = {
      'Em análise':      'bg-warning text-dark',
      'Em reparo':       'bg-primary',
      'Aguardando peças':'bg-info text-dark',
      'Concluído':       'bg-success',
    };
    return map[status] ?? 'bg-secondary';
  }
}
