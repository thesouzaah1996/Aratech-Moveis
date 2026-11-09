import { PermissaoDirective } from '../../../core/permissao.directive';
import { Component, ElementRef, AfterViewInit, OnDestroy, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { SolicitacaoPecaService } from '../../../core/services/solicitacao-peca.service';
import { SolicitacaoPeca, SolicitacaoPecaForm } from '../../../core/models/solicitacao-peca.model';

import { Subscription } from 'rxjs';
import { criarModal, ModalDaTela } from '../../../core/ui/modal';
import { validarSolicitacaoPeca, normalizarSolicitacaoPeca } from '../../../core/models/solicitacao-peca-form';

@Component({
  selector: 'app-solicitar-peca',
  standalone: true,
  imports: [PermissaoDirective,FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './solicitar-peca.component.html',
  styleUrl: './solicitar-peca.component.scss'
})
export class SolicitarPecaComponent implements AfterViewInit, OnDestroy {
  @ViewChild('resultModal') resultModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Manutenção', route: '/manutencao' },
    { label: 'Solicitar Peça' }
  ];

  form: SolicitacaoPecaForm = this.emptyForm();
  submitted = false;
  resultadoSucesso = false;
  solicitacaoCriada: SolicitacaoPeca | null = null;
  errorMessage = '';

  salvando = false;
  validacaoError = '';
  cadastroPendente: SolicitacaoPecaForm | null = null;
  private chaveOperacao = crypto.randomUUID();
  private mutation?: Subscription;
  private resultModal?: ModalDaTela;

  constructor(private solicitacaoPecaService: SolicitacaoPecaService) {}

  ngAfterViewInit(): void {
    this.resultModal = criarModal(this.resultModalEl.nativeElement);
  }

  ngOnDestroy(): void { this.mutation?.unsubscribe(); this.resultModal?.dispose(); }

  submit(): void {
    if (this.salvando || this.solicitacaoCriada) return;
    this.submitted = true;
    if (!this.cadastroPendente) {
      this.validacaoError = validarSolicitacaoPeca(this.form);
      if (this.validacaoError) return;
      this.cadastroPendente = normalizarSolicitacaoPeca(this.form);
    }
    this.salvando = true; this.errorMessage = ''; this.validacaoError = '';
    this.mutation = this.solicitacaoPecaService.add(this.cadastroPendente, this.chaveOperacao).subscribe({
      next: chamado => {
        this.salvando = false;
        this.cadastroPendente = null;
        this.solicitacaoCriada = chamado;
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
    this.solicitacaoCriada = null;
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

  private emptyForm(): SolicitacaoPecaForm {
    return { nomePeca: '', codigo: '', quantidade: null, unidade: '', equipamento: '', finalidade: '', prioridade: '', solicitante: '', setor: '', telefone: '', observacoes: '' };
  }
}
