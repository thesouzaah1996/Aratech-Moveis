import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-solicitar-primeiro-acesso',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './solicitar-primeiro-acesso.component.html',
  styleUrl: './solicitar-primeiro-acesso.component.scss'
})
export class SolicitarPrimeiroAcessoComponent {
  emailPessoal = '';
  emailError = '';
  isLoading = false;
  state: 'idle' | 'success' | 'error' = 'idle';
  errorMessage = '';

  constructor(private authService: AuthService) {}

  onSubmit(): void {
    this.state = 'idle';
    this.emailError = '';
    this.errorMessage = '';

    if (!this.emailPessoal) {
      this.emailError = 'E-mail é obrigatório';
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.emailPessoal)) {
      this.emailError = 'Digite um e-mail válido';
      return;
    }

    this.isLoading = true;
    this.authService.solicitarPrimeiroAcesso(this.emailPessoal).subscribe({
      next: () => {
        this.isLoading = false;
        this.state = 'success';
      },
      error: (err) => {
        this.isLoading = false;
        this.state = 'error';
        this.errorMessage = err.error?.message ?? 'Não foi possível enviar o e-mail de primeiro acesso.';
      }
    });
  }
}
