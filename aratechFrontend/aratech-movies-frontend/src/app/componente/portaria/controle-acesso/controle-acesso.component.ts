import { Component, ElementRef, AfterViewInit, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

export type StatusCaminhao = 'AGUARDANDO' | 'AUTORIZADO' | 'FINALIZADO';
export type SetorPortaria = 'Almoxarifado' | 'Carregamento';

export interface RegistroPortaria {
  id: number;
  notaFiscal: string;
  empresa: string;
  motorista: string;
  placa: string;
  setor: SetorPortaria;
  status: StatusCaminhao;
  dataEntrada: string;
}

@Component({
  selector: 'app-controle-acesso',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './controle-acesso.component.html',
  styleUrl: './controle-acesso.component.scss'
})
export class ControleAcessoComponent implements OnInit, AfterViewInit {
  @ViewChild('historicoModal') historicoModalEl!: ElementRef;
  @ViewChild('removeModal') removeModalEl!: ElementRef;
  @ViewChild('confirmEntradaModal') confirmEntradaModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Portaria', route: '/portaria' },
    { label: 'Controle de Acesso' }
  ];

  fila: RegistroPortaria[] = [];
  historico: RegistroPortaria[] = [];

  form = {
    notaFiscal: '',
    empresa: '',
    motorista: '',
    placa: '',
    setor: '' as SetorPortaria | ''
  };

  filtroNF = '';
  filtroData = '';
  idParaRemover: number | null = null;
  idParaConfirmar: number | null = null;

  private historicoModal?: any;
  private removeModal?: any;
  private confirmEntradaModal?: any;

  ngOnInit(): void {
    // TODO: inject PortariaService and call portariaService.listarFila()
    // TODO: inject PortariaService and call portariaService.listarHistorico()
  }

  ngAfterViewInit(): void {
    this.historicoModal = new bootstrap.Modal(this.historicoModalEl.nativeElement);
    this.removeModal = new bootstrap.Modal(this.removeModalEl.nativeElement);
    this.confirmEntradaModal = new bootstrap.Modal(this.confirmEntradaModalEl.nativeElement);
  }

  get historicoFiltrado(): RegistroPortaria[] {
    return this.historico.filter(h => {
      const nfOk = !this.filtroNF || h.notaFiscal.toLowerCase().includes(this.filtroNF.toLowerCase());
      const dataOk = !this.filtroData || h.dataEntrada.startsWith(this.filtroData);
      return nfOk && dataOk;
    });
  }

  registrarChegada(): void {
    if (!this.form.notaFiscal || !this.form.empresa || !this.form.motorista || !this.form.placa || !this.form.setor) return;
    // TODO: portariaService.registrar(this.form).subscribe(() => { this.resetForm(); this.carregarFila(); })
    this.resetForm();
  }

  abrirHistorico(): void {
    this.historicoModal.show();
  }

  abrirRemocao(id: number): void {
    this.idParaRemover = id;
    this.removeModal.show();
  }

  confirmarRemocao(): void {
    // TODO: portariaService.remover(this.idParaRemover).subscribe(...)
    this.fila = this.fila.filter(i => i.id !== this.idParaRemover);
    this.idParaRemover = null;
    this.removeModal.hide();
  }

  abrirConfirmacaoEntrada(id: number): void {
    this.idParaConfirmar = id;
    this.confirmEntradaModal.show();
  }

  confirmarEntrada(): void {
    // TODO: portariaService.confirmarEntrada(this.idParaConfirmar).subscribe(...)
    const item = this.fila.find(i => i.id === this.idParaConfirmar);
    if (item) {
      item.status = 'FINALIZADO';
      this.historico.unshift({ ...item });
      this.fila = this.fila.filter(i => i.id !== this.idParaConfirmar);
    }
    this.idParaConfirmar = null;
    this.confirmEntradaModal.hide();
  }

  formatWaitTime(dataEntrada: string): string {
    const diff = Date.now() - new Date(dataEntrada).getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(minutes / 60);
    const rest = minutes % 60;
    return hours > 0 ? `${hours}h ${rest}min` : `${minutes}min`;
  }

  badgeClass(status: StatusCaminhao): string {
    const map: Record<StatusCaminhao, string> = {
      AGUARDANDO: 'bg-warning text-dark',
      AUTORIZADO:  'bg-primary',
      FINALIZADO:  'bg-success',
    };
    return map[status] ?? 'bg-secondary';
  }

  limparFiltros(): void {
    this.filtroNF = '';
    this.filtroData = '';
  }

  private resetForm(): void {
    this.form = { notaFiscal: '', empresa: '', motorista: '', placa: '', setor: '' };
  }
}
