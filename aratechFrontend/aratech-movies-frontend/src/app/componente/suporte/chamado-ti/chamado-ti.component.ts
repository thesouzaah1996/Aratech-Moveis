import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

declare const bootstrap: any;

interface FormChamadoTi {
  tipo: string;
  prioridade: string;
  moduloAfetado: string;
  titulo: string;
  solicitante: string;
  setor: string;
  contato: string;
  descricao: string;
  tentativasSolucao: string;
}

@Component({
  selector: 'app-chamado-ti',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './chamado-ti.component.html',
  styleUrl: './chamado-ti.component.scss'
})
export class ChamadoTiComponent implements AfterViewInit {
  @ViewChild('resultModal') resultModalEl!: ElementRef;

  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Suporte de TI' }
  ];

  form: FormChamadoTi = this.emptyForm();
  submitted = false;
  resultadoSucesso = false;
  numeroChamado = '';

  private resultModal?: any;

  constructor(private router: Router) {}

  ngAfterViewInit(): void {
    this.resultModal = new bootstrap.Modal(this.resultModalEl.nativeElement);
  }

  submit(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;

    this.numeroChamado = this.gerarNumeroChamado();
    this.resultadoSucesso = true;
    this.resultModal.show();
  }

  fecharResultado(): void {
    this.resultModal.hide();
    if (this.resultadoSucesso) {
      this.resetForm();
    }
  }

  voltar(): void {
    this.router.navigate(['/dashboard']);
  }

  resetForm(): void {
    this.submitted = false;
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
    this.form.contato = input.value;
  }

  private isFormValid(): boolean {
    return !!(
      this.form.tipo &&
      this.form.prioridade &&
      this.form.titulo.trim() &&
      this.form.solicitante.trim() &&
      this.form.setor.trim() &&
      this.form.descricao.trim()
    );
  }

  private gerarNumeroChamado(): string {
    const ano = new Date().getFullYear();
    const seq = Math.floor(Math.random() * 9000) + 1000;
    return `TI-${ano}-${seq}`;
  }

  private emptyForm(): FormChamadoTi {
    return {
      tipo: '',
      prioridade: '',
      moduloAfetado: '',
      titulo: '',
      solicitante: '',
      setor: '',
      contato: '',
      descricao: '',
      tentativasSolucao: ''
    };
  }
}
