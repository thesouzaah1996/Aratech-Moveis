import { AfterViewInit, Component, ElementRef, Input, OnDestroy, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoteAssistencia } from '../../../core/services/lote-assistencia.service';
import { criarModal, ModalDaTela } from '../../../core/ui/modal';

@Component({
  selector: 'app-historico-lotes', standalone: true, imports: [DatePipe, FormsModule],
  template: `
    <div class="modal fade" #modal tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false" aria-label="Histórico de lotes">
      <div class="modal-dialog modal-xl modal-dialog-scrollable"><div class="modal-content">
        <div class="modal-header bg-danger text-white"><h5 class="modal-title fw-bold"><i class="bi bi-clock-history me-2"></i>Histórico de Lotes</h5></div>
        <div class="modal-body">
          <div class="row g-3 mb-4">
            <div class="col-md-5"><label class="form-label fw-semibold">Buscar por Código ou Produto<input class="form-control" [(ngModel)]="busca" placeholder="Digite o código ou produto"></label></div>
            <div class="col-md-5"><label class="form-label fw-semibold">Filtrar por Data de Finalização<input type="date" class="form-control" [(ngModel)]="data"></label></div>
            <div class="col-md-2 d-flex align-items-end"><button type="button" class="btn btn-outline-secondary w-100" (click)="busca = ''; data = ''"><i class="bi bi-eraser me-1"></i>Limpar</button></div>
          </div>
          <div class="table-responsive"><table class="table table-striped table-hover align-middle">
            <thead class="table-dark"><tr><th>Código</th><th>Produto / Embalagem</th><th>Empresa</th><th>Quantidade</th><th>Responsável</th><th>Iniciado em</th><th>Finalizado em</th><th>Carregado em</th><th>Status</th></tr></thead>
            <tbody>
              @for (lote of filtrados; track lote.id) {
                <tr><td class="text-nowrap"><code>{{ lote.codigo }}</code></td><td>{{ lote.produto }} {{ lote.defeito }}</td><td>{{ lote.empresa || '—' }}</td><td>{{ lote.quantidade }}</td><td>{{ lote.responsavel }}</td><td>{{ lote.iniciadoEm | date:'dd/MM/yyyy HH:mm' }}</td><td>{{ lote.finalizadoEm | date:'dd/MM/yyyy HH:mm' }}</td><td>{{ (lote.carregadoEm | date:'dd/MM/yyyy HH:mm') || '—' }}</td><td><span class="badge bg-success">{{ lote.carregadoEm ? 'Carregado' : 'Finalizado' }}</span></td></tr>
              } @empty { <tr><td colspan="9"><div class="empty-state"><i class="bi bi-inbox"></i><p>Nenhum registro encontrado.</p></div></td></tr> }
            </tbody>
          </table></div>
        </div>
        <div class="modal-footer border-0 bg-light"><button type="button" class="btn btn-cancelar" (click)="instancia?.hide()">Fechar</button></div>
      </div></div>
    </div>`
})
export class HistoricoLotesComponent implements AfterViewInit, OnDestroy {
  @Input() lotes: LoteAssistencia[] = [];
  @ViewChild('modal') modal!: ElementRef<HTMLElement>;
  instancia?: ModalDaTela;
  busca = ''; data = '';
  get filtrados(): LoteAssistencia[] {
    const termo = this.busca.trim().toLocaleLowerCase();
    return this.lotes.filter(l => l.status === 'Finalizado' &&
      (!termo || `${l.codigo} ${l.produto}`.toLocaleLowerCase().includes(termo)) &&
      (!this.data || (l.finalizadoEm && new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Sao_Paulo' }).format(new Date(l.finalizadoEm)) === this.data)))
      .sort((a, b) => (b.finalizadoEm ?? '').localeCompare(a.finalizadoEm ?? '') || b.id - a.id);
  }
  ngAfterViewInit(): void { this.instancia = criarModal(this.modal.nativeElement); }
  abrir(): void { this.instancia?.show(); }
  ngOnDestroy(): void { this.instancia?.dispose(); }
}
