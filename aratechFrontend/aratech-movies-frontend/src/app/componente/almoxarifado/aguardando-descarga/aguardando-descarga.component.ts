import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusFila = 'Aguardando' | 'Em conferência' | 'Finalizado';

export interface ItemFila {
  id: number;
  empresa: string;
  produto: string;
  chegada: string;
  motorista: string;
  status: StatusFila;
  observacoes?: string;
}

@Component({
  selector: 'app-aguardando-descarga',
  standalone: true,
  imports: [NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './aguardando-descarga.component.html',
  styleUrl: './aguardando-descarga.component.scss'
})
export class AguardandoDescargaComponent implements OnInit {
  @ViewChild('historicoModal') historicoModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Almoxarifado', route: '/almoxarifado' },
    { label: 'Aguardando Descarga' }
  ];

  fila: ItemFila[] = [];
  historico: ItemFila[] = [];

  private historicoModal?: any;

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.historicoModal = new bootstrap.Modal(this.historicoModalEl.nativeElement);
  }

  abrirHistorico(): void {
    this.historicoModal.show();
  }

  liberar(item: ItemFila): void {
    item.status = 'Em conferência';
  }

  finalizar(item: ItemFila): void {
    item.status = 'Finalizado';
    this.historico.unshift({ ...item });
    this.fila = this.fila.filter(i => i.id !== item.id);
  }

  badgeClass(status: StatusFila): string {
    return {
      'Aguardando':    'bg-warning text-dark',
      'Em conferência':'bg-primary',
      'Finalizado':    'bg-success',
    }[status] ?? 'bg-secondary';
  }
}
