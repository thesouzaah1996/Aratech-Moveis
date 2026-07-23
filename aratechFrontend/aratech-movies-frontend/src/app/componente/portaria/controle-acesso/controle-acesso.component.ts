import { Component, ElementRef, AfterViewInit, OnInit, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { RegistroChegadaService } from '../../../core/services/registro-chegada.service';
import {
  RegistroChegada,
  RegistroChegadaForm,
  StatusCaminhao,
  SETOR_LABELS,
  STATUS_CAMINHAO_LABELS
} from '../../../core/models/registro-chegada.model';

declare const bootstrap: any;

@Component({
  selector: 'app-controle-acesso',
  standalone: true,
  imports: [FormsModule, DatePipe, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './controle-acesso.component.html',
  styleUrl: './controle-acesso.component.scss'
})
export class ControleAcessoComponent implements OnInit, AfterViewInit {
  @ViewChild('historicoModal') historicoModalEl!: ElementRef;
  @ViewChild('confirmEntradaModal') confirmEntradaModalEl!: ElementRef;
  @ViewChild('editModal') editModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Portaria', route: '/portaria' },
    { label: 'Controle de Acesso' }
  ];

  readonly setorLabels = SETOR_LABELS;
  readonly statusLabels = STATUS_CAMINHAO_LABELS;

  fila: RegistroChegada[] = [];
  historico: RegistroChegada[] = [];

  form: RegistroChegadaForm = this.emptyForm();
  submitted = false;
  successMessage = '';
  errorMessage = '';

  filtroNF = '';
  filtroData = '';
  notaFiscalParaConfirmar: string | null = null;

  editForm: RegistroChegadaForm = this.emptyForm();
  editSubmitted = false;

  page = 1;
  pageSize = 8;

  private historicoModal?: any;
  private confirmEntradaModal?: any;
  private editModal?: any;

  constructor(private readonly registroChegadaService: RegistroChegadaService) {}

  ngOnInit(): void {
    this.carregarFila();
  }

  ngAfterViewInit(): void {
    this.historicoModal = new bootstrap.Modal(this.historicoModalEl.nativeElement);
    this.confirmEntradaModal = new bootstrap.Modal(this.confirmEntradaModalEl.nativeElement);
    this.editModal = new bootstrap.Modal(this.editModalEl.nativeElement);
  }

  private carregarFila(): void {
    this.registroChegadaService.getFila().subscribe({
      next: fila => (this.fila = fila),
      error: () => this.showError('Erro ao carregar a fila de espera.')
    });
  }

  private carregarHistorico(): void {
    this.registroChegadaService.getHistorico().subscribe({
      next: historico => (this.historico = historico),
      error: () => this.showError('Erro ao carregar o histórico de entregas.')
    });
  }

  get filaPaginada(): RegistroChegada[] {
    const start = (this.page - 1) * this.pageSize;
    return this.fila.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.fila.length / this.pageSize);
  }

  get visiblePages(): number[] {
    const half = 5;
    const start = Math.max(1, Math.min(this.page - half, this.totalPages - 7));
    const end = Math.min(this.totalPages, start + 7);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  get paginationStart(): number {
    return this.fila.length === 0 ? 0 : (this.page - 1) * this.pageSize + 1;
  }

  get paginationEnd(): number {
    return Math.min(this.page * this.pageSize, this.fila.length);
  }

  setPage(p: number): void {
    if (p < 1 || p > this.totalPages) return;
    this.page = p;
  }

  get historicoFiltrado(): RegistroChegada[] {
    return this.historico.filter(h => {
      const nfOk = !this.filtroNF || h.notaFiscal.toLowerCase().includes(this.filtroNF.toLowerCase());
      const dataOk = !this.filtroData || h.dataChegada.startsWith(this.filtroData);
      return nfOk && dataOk;
    });
  }

  registrarChegada(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;

    const payload: RegistroChegadaForm = {
      ...this.form,
      placa: this.form.placa.toUpperCase().replace(/[^A-Z0-9]/g, '')
    };

    this.registroChegadaService.add(payload).subscribe({
      next: registro => {
        this.fila.push(registro);
        this.resetForm();
        this.showSuccess('Chegada registrada com sucesso!');
      },
      error: err => this.showError(err.error?.mensagem ?? 'Erro ao registrar chegada.')
    });
  }

  abrirHistorico(): void {
    this.carregarHistorico();
    this.historicoModal.show();
  }

  abrirConfirmacaoEntrada(notaFiscal: string): void {
    this.notaFiscalParaConfirmar = notaFiscal;
    this.confirmEntradaModal.show();
  }

  confirmarEntrada(): void {
    if (!this.notaFiscalParaConfirmar) return;

    this.registroChegadaService.finalizar(this.notaFiscalParaConfirmar).subscribe({
      next: () => {
        this.fila = this.fila.filter(i => i.notaFiscal !== this.notaFiscalParaConfirmar);
        this.notaFiscalParaConfirmar = null;
        this.confirmEntradaModal.hide();
        this.showSuccess('Entrada confirmada com sucesso!');
      },
      error: err => {
        this.confirmEntradaModal.hide();
        this.showError(err.error?.mensagem ?? 'Erro ao confirmar entrada.');
      }
    });
  }

  abrirEdicao(item: RegistroChegada): void {
    this.editSubmitted = false;
    this.editForm = {
      notaFiscal: item.notaFiscal,
      empresa: item.empresa,
      nomeMotorista: item.nomeMotorista,
      placa: item.placa,
      descricaoCarga: item.descricaoCarga,
      setorResponsavel: item.setorResponsavel
    };
    this.editModal.show();
  }

  salvarEdicao(): void {
    this.editSubmitted = true;
    if (!this.isEditFormValid()) return;

    const payload: RegistroChegadaForm = {
      ...this.editForm,
      placa: this.editForm.placa.toUpperCase().replace(/[^A-Z0-9]/g, '')
    };

    this.registroChegadaService.atualizar(payload).subscribe({
      next: registro => {
        this.fila = this.fila.map(i => (i.notaFiscal === registro.notaFiscal ? registro : i));
        this.editModal.hide();
        this.showSuccess('Registro atualizado com sucesso!');
      },
      error: err => this.showError(err.error?.mensagem ?? 'Erro ao atualizar registro.')
    });
  }

  private isEditFormValid(): boolean {
    return !!(
      this.editForm.notaFiscal.trim() &&
      this.editForm.empresa.trim() &&
      this.editForm.nomeMotorista.trim() &&
      this.editForm.placa.trim() &&
      this.editForm.descricaoCarga.trim() &&
      this.editForm.setorResponsavel
    );
  }

  badgeClass(status: StatusCaminhao): string {
    const map: Record<StatusCaminhao, string> = {
      AGUARDANDO: 'bg-warning text-dark',
      AUTORIZADO: 'bg-primary',
      FINALIZADO: 'bg-success'
    };
    return map[status] ?? 'bg-secondary';
  }

  limparFiltros(): void {
    this.filtroNF = '';
    this.filtroData = '';
  }

  private isFormValid(): boolean {
    return !!(
      this.form.notaFiscal.trim() &&
      this.form.empresa.trim() &&
      this.form.nomeMotorista.trim() &&
      this.form.placa.trim() &&
      this.form.descricaoCarga.trim() &&
      this.form.setorResponsavel
    );
  }

  private resetForm(): void {
    this.submitted = false;
    this.form = this.emptyForm();
  }

  private emptyForm(): RegistroChegadaForm {
    return { notaFiscal: '', empresa: '', nomeMotorista: '', placa: '', descricaoCarga: '', setorResponsavel: '' };
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    this.errorMessage = '';
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  private showError(msg: string): void {
    this.errorMessage = msg;
    this.successMessage = '';
  }
}
