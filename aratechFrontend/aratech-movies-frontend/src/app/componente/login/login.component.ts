import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { loginCorporativoValido } from '../../core/login-corporativo';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  email = '';
  password = '';
  showPassword = false;
  isLoading = false;
  loginState: 'idle' | 'success' | 'error' | 'blocked' = 'idle';
  emailError = '';
  passwordError = '';
  erroLogin = 'Usuário ou senha incorretos.';

  constructor(private router: Router, private authService: AuthService, private route: ActivatedRoute) {}

  ngOnInit(): void {}

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  clearState(): void {
    this.loginState = 'idle';
    this.emailError = '';
    this.passwordError = '';
  }

  onSubmit(): void {
    if (this.isLoading) return;
    this.loginState = 'idle';
    this.emailError = '';
    this.passwordError = '';
    this.email = this.email.trim();

    let valid = true;

    if (!this.email) {
      this.emailError = 'E-mail corporativo é obrigatório';
      valid = false;
    } else if (!loginCorporativoValido(this.email)) {
      this.emailError = 'Use nome@aratechmoveis, com até 64 caracteres antes do @';
      valid = false;
    }

    if (!this.password) {
      this.passwordError = 'Senha é obrigatória';
      valid = false;
    }

    if (!valid) return;

    this.isLoading = true;

    this.authService.login(this.email, this.password).subscribe({
      next: usuario => {
        this.loginState = 'success';
        this.password = '';
        if (usuario.trocarSenha) { void this.router.navigate(['/trocar-senha']); return; }
        const destino = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/dashboard';
        void this.router.navigateByUrl(destino.startsWith('/') && !destino.startsWith('//') ? destino : '/dashboard');
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        this.loginState = err.status === 429 ? 'blocked' : 'error';
        this.erroLogin = err.status === 0 ? 'Não foi possível conectar ao servidor. Tente novamente.' : err.error?.codigo === 'CSRF_INVALIDO' ? 'A sessão de segurança expirou. Tente novamente.' : 'Usuário ou senha incorretos.';
      }
    });
  }
}
