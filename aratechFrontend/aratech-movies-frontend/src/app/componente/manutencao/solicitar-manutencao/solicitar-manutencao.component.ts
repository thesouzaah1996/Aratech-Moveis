import { PermissaoDirective } from '../../../core/permissao.directive';
import { Component, ElementRef, AfterViewInit, OnDestroy, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { ChamadoService } from '../../../core/services/chamado.service';
import { Chamado, ChamadoForm } from '../../../core/models/chamado.model';

import { Subscription } from 'rxjs';
import { criarModal, ModalDaTela } from '../../../core/ui/modal';
import { validarChamado, normalizarChamado } from '../../../core/models/chamado-form';

@Component({
  selector: 'app-solicitar-manutencao',
  standalone: true,
  imports: [PermissaoDirective,FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './solicitar-manutencao.component.html',
  styleUrl: './solicitar-manutencao.component.scss'
})
export class SolicitarManutencaoComponent implements AfterViewInit, OnDestroy {
  @ViewChild('resultModal') resultModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Manutenção', route: '/manutencao' },
    { label: 'Solicitar Manutenção' }
  ];

  form: ChamadoForm = this.emptyForm();
  submitted = false;
  resultadoSucesso = false;
  chamadoCriado: Chamado | null = null;
  errorMessage = '';

  salvando = false;
  validacaoError = '';
  cadastroPendente: ChamadoForm | null = null;
  private chaveOperacao = crypto.randomUUID();
  private mutation?: Subscription;
  private resultModal?: ModalDaTela;

  constructor(private chamadoService: ChamadoService) {}

  ngAfterViewInit(): void {
    this.resultModal = criarModal(this.resultModalEl.nativeElement);
  }

  ngOnDestroy(): void { this.mutation?.unsubscribe(); this.resultModal?.dispose(); }

  submit(): void {
    if (this.salvando || this.chamadoCriado) return;
    this.submitted = true;
    if (!this.cadastroPendente) {
      this.validacaoError = validarChamado(this.form);
      if (this.validacaoError) return;
      this.cadastroPendente = normalizarChamado(this.form);
    }
    this.salvando = true; this.errorMessage = ''; this.validacaoError = '';
    this.mutation = this.chamadoService.add(this.cadastroPendente, this.chaveOperacao).subscribe({
      next: chamado => {
        this.salvando = false;
        this.cadastroPendente = null;
        this.chamadoCriado = chamado;
        this.resultadoSucesso = true;
        this.resultModal?.show();
      },
      error: err => {
        this.salvando = false;
        if (err.status >= 400 && err.status < 500 && err.status !== 409) {
          this.cadastroPendente = null;
          this.chaveOperacao = crypto.randomUUID();
        }
        this.errorMessage = err.error?.mensagem || err.error?.message || 'Nao foi possivel enviar a solicitacao.';
        if (this.cadastroPendente) this.errorMessage += ' Tente enviar novamente: os mesmos dados serao reenviados para evitar duplicidade.';
        this.resultadoSucesso = false;
        this.resultModal?.show();
      }
    });
  }

  fecharResultado(): void {
    this.resultModal?.hide();
    if (this.resultadoSucesso) {
      this.resetForm();
    }
  }

  resetForm(): void {
    if (this.salvando || this.cadastroPendente) return;
    this.chaveOperacao = crypto.randomUUID();
    this.validacaoError = '';
    this.submitted = false;
    this.chamadoCriado = null;
    this.errorMessage = '';
    this.form = this.emptyForm();
  }

  applyPhoneMask(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/[^0-9]/g, '').slice(0, 11);
    if (digits.length > 6) {
      const corte = digits.length > 10 ? 7 : 6;
      input.value = '(' + digits.slice(0, 2) + ') ' + digits.slice(2, corte) + '-' + digits.slice(corte);
    } else if (digits.length > 2) input.value = '(' + digits.slice(0, 2) + ') ' + digits.slice(2);
    else input.value = digits ? '(' + digits : '';
    this.form.telefone = input.value;
  }

  private emptyForm(): ChamadoForm {
    return { equipamento: '', tipo: '', prioridade: '', solicitante: '', setor: '', telefone: '', descricao: '' };
  }
}
