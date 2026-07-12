import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-confirmar-primeiro-acesso',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './confirmar-primeiro-acesso.component.html',
  styleUrl: './confirmar-primeiro-acesso.component.scss'
})
export class ConfirmarPrimeiroAcessoComponent implements OnInit {
  codigo = '';
  emailCorporativo = '';
  novaSenha = '';
  confirmarSenha = '';
  emailError = '';
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
    this.emailError = '';
    this.senhaError = '';
    this.confirmarError = '';
  }

  onSubmit(): void {
    this.emailError = '';
    this.senhaError = '';
    this.confirmarError = '';
    this.errorMessage = '';

    let valid = true;

    if (!this.emailCorporativo) {
      this.emailError = 'E-mail é obrigatório';
      valid = false;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.emailCorporativo)) {
      this.emailError = 'Digite um e-mail válido';
      valid = false;
    }

    if (!this.novaSenha || this.novaSenha.length < 8) {
      this.senhaError = 'A senha deve ter ao menos 8 caracteres';
      valid = false;
    }

    if (this.confirmarSenha !== this.novaSenha) {
      this.confirmarError = 'As senhas não coincidem';
      valid = false;
    }

    if (!valid) return;

    this.isLoading = true;
    this.authService.confirmarPrimeiroAcesso(this.emailCorporativo, this.codigo, this.novaSenha).subscribe({
      next: () => {
        this.isLoading = false;
        this.state = 'success';
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.isLoading = false;
        this.state = 'error';
        this.errorMessage = err.error?.message ?? 'Não foi possível concluir o primeiro acesso. O link pode ter expirado.';
      }
    });
  }
}
