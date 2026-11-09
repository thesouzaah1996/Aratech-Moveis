import { PermissaoDirective } from '../../../core/permissao.directive';
﻿import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AfterViewInit, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { criarModal, ModalDaTela } from '../../../core/ui/modal';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { HistoricoLotesComponent } from '../../assistencia/lotes/historico-lotes.component';
import { LoteAssistencia, LoteAssistenciaService, StatusLoteAssistencia } from '../../../core/services/lote-assistencia.service';

@Component({
  selector: 'app-carregamento-assistencia', standalone: true,
  imports: [PermissaoDirective,DatePipe, NavbarComponent, BreadcrumbComponent, HistoricoLotesComponent],
  templateUrl: './assistencia.component.html', styleUrl: './assistencia.component.scss'
})
export class CarregamentoAssistenciaComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('viewModal') viewModalEl!: ElementRef<HTMLElement>;
  private viewModal?: ModalDaTela;
  loteSelecionado: LoteAssistencia | null = null;
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' }, { label: 'Carregamento', route: '/carregamento' }, { label: 'Assistência' }
  ];
  lotes: LoteAssistencia[] = [];
  carregando = false;
  processando = false;
  errorMessage = '';
  successMessage = '';
  constructor(private service: LoteAssistenciaService) {}
  ngOnInit(): void { this.carregar(); }
  ngAfterViewInit(): void { this.viewModal = criarModal(this.viewModalEl.nativeElement); }
  ngOnDestroy(): void { this.viewModal?.dispose(); }
  openView(lote: LoteAssistencia): void { this.loteSelecionado = lote; this.viewModal?.show(); }
  fecharVisualizacao(): void { this.viewModal?.hide(); }
  carregar(): void {
    this.carregando = true;
    this.service.listar().subscribe({
      next: lotes => { this.lotes = lotes; this.carregando = false; this.errorMessage = ''; },
      error: () => { this.carregando = false; this.errorMessage = 'Não foi possível carregar os lotes. Tente novamente.'; }
    });
  }
  get itens(): LoteAssistencia[] {
    const ordem: Record<StatusLoteAssistencia, number> = { 'Finalizado': 0, 'Em andamento': 1, 'Não iniciado': 2 };
    return this.lotes.filter(l => !l.carregadoEm).sort((a, b) => ordem[a.status] - ordem[b.status] || a.dataEntrada.localeCompare(b.dataEntrada) || a.id - b.id);
  }
  finalizarCarregamento(lote: LoteAssistencia): void {
    if (this.processando || lote.status !== 'Finalizado' || lote.carregadoEm) return;
    this.processando = true;
    this.service.avancar(lote.id, 'carregar').subscribe({
      next: atualizado => { this.lotes = this.lotes.map(l => l.id === atualizado.id ? atualizado : l); this.processando = false; this.errorMessage = ''; this.successMessage = `Carregamento do lote ${lote.codigo} finalizado.`; },
      error: () => { this.processando = false; this.errorMessage = 'Não foi possível finalizar o carregamento. Atualize a lista e tente novamente.'; }
    });
  }
  badgeClass(status: StatusLoteAssistencia): string {
    return status === 'Finalizado' ? 'bg-success' : status === 'Em andamento' ? 'bg-info text-dark' : 'bg-secondary';
  }
}
