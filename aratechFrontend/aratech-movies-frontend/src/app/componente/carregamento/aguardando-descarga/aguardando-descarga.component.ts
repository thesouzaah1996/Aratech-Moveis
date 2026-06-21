import { Component, ElementRef, AfterViewInit, OnInit, ViewChild } from '@angular/core';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusFila = 'Aguardando' | 'Em conferência' | 'Finalizado';

export interface ItemFilaCarregamento {
  id: number;
  empresa: string;
  produto: string;
  chegada: string;
  motorista: string;
  status: StatusFila;
  observacoes?: string;
}

@Component({
  selector: 'app-carregamento-aguardando-descarga',
  standalone: true,
  imports: [NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './aguardando-descarga.component.html',
  styleUrl: './aguardando-descarga.component.scss'
})
export class CarregamentoAguardandoDescargaComponent implements OnInit, AfterViewInit {
  @ViewChild('historicoModal') historicoModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Carregamento', route: '/carregamento' },
    { label: 'Aguardando Descarga' }
  ];

  fila: ItemFilaCarregamento[] = [];
  historico: ItemFilaCarregamento[] = [];

  private historicoModal?: any;

  ngOnInit(): void {
    // TODO: inject CarregamentoDescargaService and call listarFila()
    // TODO: inject CarregamentoDescargaService and call listarHistorico()
  }

  ngAfterViewInit(): void {
    this.historicoModal = new bootstrap.Modal(this.historicoModalEl.nativeElement);
  }

  abrirHistorico(): void {
    this.historicoModal.show();
  }

  liberar(item: ItemFilaCarregamento): void {
    // TODO: carregamentoDescargaService.liberar(item.id)
    item.status = 'Em conferência';
  }

  finalizar(item: ItemFilaCarregamento): void {
    // TODO: carregamentoDescargaService.finalizar(item.id)
    item.status = 'Finalizado';
    this.historico.unshift({ ...item });
    this.fila = this.fila.filter(i => i.id !== item.id);
  }

  badgeClass(status: StatusFila): string {
    const map: Record<StatusFila, string> = {
      'Aguardando':     'bg-warning text-dark',
      'Em conferência': 'bg-primary',
      'Finalizado':     'bg-success',
    };
    return map[status] ?? 'bg-secondary';
  }
}
