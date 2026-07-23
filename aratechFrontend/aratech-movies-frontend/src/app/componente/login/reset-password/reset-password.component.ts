import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent implements OnInit {
  codigo = '';
  senha = '';
  confirmarSenha = '';
  senhaError = '';
  confirmarError = '';
  isLoading = false;
  state: 'idle' | 'success' | 'error' | 'sem-codigo' = 'idle';
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.codigo = this.route.snapshot.queryParamMap.get('code') ?? '';
    if (!this.codigo) {
      this.state = 'sem-codigo';
      return;
    }
    this.router.navigate([], { relativeTo: this.route, queryParams: {}, replaceUrl: true });
  }

  clearState(): void {
    this.state = 'idle';
    this.senhaError = '';
    this.confirmarError = '';
  }

  onSubmit(): void {
    this.senhaError = '';
    this.confirmarError = '';
    this.errorMessage = '';

    let valid = true;

    if (!this.senha || this.senha.length < 8) {
      this.senhaError = 'A senha deve ter ao menos 8 caracteres';
      valid = false;
    }

    if (this.confirmarSenha !== this.senha) {
      this.confirmarError = 'As senhas não coincidem';
      valid = false;
    }

    if (!valid) return;

    this.isLoading = true;
    this.authService.confirmarRedefinicaoSenha(this.codigo, this.senha, this.confirmarSenha).subscribe({
      next: () => {
        this.isLoading = false;
        this.state = 'success';
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: () => {
        this.isLoading = false;
        this.state = 'error';
        this.errorMessage = 'Não foi possível redefinir a senha. O link pode ter expirado.';
      }
    });
  }
}
