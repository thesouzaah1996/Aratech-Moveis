import { PermissaoDirective } from '../../../core/permissao.directive';
import { Component, ElementRef, AfterViewInit, OnDestroy, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { ChamadoTiService } from '../../../core/services/chamado-ti.service';
import { ChamadoTi, ChamadoTiForm } from '../../../core/models/chamado-ti.model';

import { Subscription } from 'rxjs';
import { criarModal, ModalDaTela } from '../../../core/ui/modal';
import { validarChamadoTi, normalizarChamadoTi } from '../../../core/models/chamado-ti-form';

@Component({
  selector: 'app-chamado-ti',
  standalone: true,
  imports: [PermissaoDirective,FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './chamado-ti.component.html',
  styleUrl: './chamado-ti.component.scss'
})
export class ChamadoTiComponent implements AfterViewInit, OnDestroy {
  @ViewChild('resultModal') resultModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Suporte TI', route: '/suporte' },
    { label: 'Abrir Chamado' }
  ];

  form: ChamadoTiForm = this.emptyForm();
  submitted = false;
  resultadoSucesso = false;
  chamadoCriado: ChamadoTi | null = null;
  errorMessage = '';

  salvando = false;
  validacaoError = '';
  cadastroPendente: ChamadoTiForm | null = null;
  private chaveOperacao = crypto.randomUUID();
  private mutation?: Subscription;
  private resultModal?: ModalDaTela;

  constructor(private router: Router, private chamadoTiService: ChamadoTiService) {}

  ngAfterViewInit(): void {
    this.resultModal = criarModal(this.resultModalEl.nativeElement);
  }

  ngOnDestroy(): void { this.mutation?.unsubscribe(); this.resultModal?.dispose(); }

  submit(): void {
    if (this.salvando || this.chamadoCriado) return;
    this.submitted = true;
    if (!this.cadastroPendente) {
      this.validacaoError = validarChamadoTi(this.form);
      if (this.validacaoError) return;
      this.cadastroPendente = normalizarChamadoTi(this.form);
    }
    this.salvando = true; this.errorMessage = ''; this.validacaoError = '';
    this.mutation = this.chamadoTiService.abrir(this.cadastroPendente, this.chaveOperacao).subscribe({
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

  get numeroChamado(): string { return this.chamadoCriado?.numero ?? ''; }
  voltar(): void { if (!this.salvando) this.router.navigate(['/dashboard']); }
  applyPhoneMask(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/[^0-9]/g,'').slice(0,11);
    if (digits.length <= 6) input.value = digits;
    else { const corte = digits.length > 10 ? 7 : 6; input.value = '('+digits.slice(0,2)+') '+digits.slice(2,corte)+'-'+digits.slice(corte); }
    this.form.contato = input.value;
  }

  private emptyForm(): ChamadoTiForm {
    return { tipo:'', prioridade:'', moduloAfetado:'', titulo:'', solicitante:'', setor:'', contato:'', descricao:'', tentativasSolucao:'' };
  }
}
