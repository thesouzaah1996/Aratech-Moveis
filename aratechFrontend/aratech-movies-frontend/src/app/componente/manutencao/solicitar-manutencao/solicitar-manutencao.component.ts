import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { ChamadoService } from '../../../core/services/chamado.service';
import { Chamado, ChamadoForm } from '../../../core/models/chamado.model';

declare const bootstrap: any;

@Component({
  selector: 'app-solicitar-manutencao',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './solicitar-manutencao.component.html',
  styleUrl: './solicitar-manutencao.component.scss'
})
export class SolicitarManutencaoComponent implements AfterViewInit {
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

  private resultModal?: any;

  constructor(private chamadoService: ChamadoService) {}

  ngAfterViewInit(): void {
    this.resultModal = new bootstrap.Modal(this.resultModalEl.nativeElement);
  }

  submit(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;

    this.chamadoService.add(this.form).subscribe({
      next: chamado => {
        this.chamadoCriado = chamado;
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
    this.chamadoCriado = null;
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
      this.form.equipamento.trim() &&
      this.form.tipo &&
      this.form.prioridade &&
      this.form.solicitante.trim() &&
      this.form.setor.trim() &&
      this.form.descricao.trim()
    );
  }

  private emptyForm(): ChamadoForm {
    return { equipamento: '', tipo: '', prioridade: '', solicitante: '', setor: '', telefone: '', descricao: '' };
  }
}
