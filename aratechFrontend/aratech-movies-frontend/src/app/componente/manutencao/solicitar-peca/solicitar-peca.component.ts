import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { SolicitacaoPecaService } from '../../../core/services/solicitacao-peca.service';
import { SolicitacaoPeca, SolicitacaoPecaForm } from '../../../core/models/solicitacao-peca.model';

declare const bootstrap: any;

@Component({
  selector: 'app-solicitar-peca',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './solicitar-peca.component.html',
  styleUrl: './solicitar-peca.component.scss'
})
export class SolicitarPecaComponent implements AfterViewInit {
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

  private resultModal?: any;

  constructor(private solicitacaoPecaService: SolicitacaoPecaService) {}

  ngAfterViewInit(): void {
    this.resultModal = new bootstrap.Modal(this.resultModalEl.nativeElement);
  }

  submit(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;

    this.solicitacaoPecaService.add(this.form).subscribe({
      next: solicitacao => {
        this.solicitacaoCriada = solicitacao;
        this.resultadoSucesso = true;
        this.resultModal.show();
      },
      error: (err) => {
        this.errorMessage = err.error?.message ?? '';
        this.resultadoSucesso = false;
        this.resultModal.show();
      }
    });
  }

  fecharResultado(): void {
    this.resultModal.hide();
    if (this.resultadoSucesso) {
      this.resetForm();
    }
  }

  resetForm(): void {
    this.submitted = false;
    this.solicitacaoCriada = null;
    this.errorMessage = '';
    this.form = this.emptyForm();
  }

  applyPhoneMask(event: Event): void {
    const input = event.target as HTMLInputElement;
    let digits = input.value.replace(/\D/g, '').slice(0, 11);
    if (digits.length > 7) {
      input.value = digits.replace(/^(\d{2})(\d{5})(\d{0,4})/, '($1) $2-$3');
    } else if (digits.length > 2) {
      input.value = digits.replace(/^(\d{2})(\d{0,5})/, '($1) $2');
    } else if (digits.length > 0) {
      input.value = '(' + digits;
    }
    this.form.telefone = input.value;
  }

  private isFormValid(): boolean {
    return !!(
      this.form.nomePeca.trim() &&
      this.form.quantidade &&
      this.form.quantidade > 0 &&
      this.form.unidade &&
      this.form.equipamento.trim() &&
      this.form.finalidade &&
      this.form.prioridade &&
      this.form.solicitante.trim() &&
      this.form.setor.trim() &&
      this.form.observacoes.trim()
    );
  }

  private emptyForm(): SolicitacaoPecaForm {
    return {
      nomePeca: '', codigo: '', quantidade: null, unidade: '',
      equipamento: '', finalidade: '', prioridade: '',
      solicitante: '', setor: '', telefone: '', observacoes: ''
    };
  }
}
