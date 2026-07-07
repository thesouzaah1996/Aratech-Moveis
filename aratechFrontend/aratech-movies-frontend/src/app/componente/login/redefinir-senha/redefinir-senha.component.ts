import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-redefinir-senha',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './redefinir-senha.component.html',
  styleUrl: './redefinir-senha.component.scss'
})
export class RedefinirSenhaComponent {
  email = '';
  emailError = '';
  isLoading = false;
  state: 'idle' | 'success' | 'error' = 'idle';
  errorMessage = '';

  constructor(private authService: AuthService) {}

  onSubmit(): void {
    this.state = 'idle';
    this.emailError = '';
    this.errorMessage = '';

    if (!this.email) {
      this.emailError = 'E-mail é obrigatório';
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email)) {
      this.emailError = 'Digite um e-mail válido';
      return;
    }

    this.isLoading = true;
    this.authService.solicitarRedefinicaoSenha(this.email).subscribe({
      next: () => {
        this.isLoading = false;
        this.state = 'success';
      },
      error: (err) => {
        this.isLoading = false;
        this.state = 'error';
        this.errorMessage = err.error?.message ?? 'Não foi possível enviar o e-mail de redefinição.';
      }
    });
  }
}
