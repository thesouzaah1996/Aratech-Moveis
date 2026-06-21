import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  email = '';
  password = '';
  showPassword = false;
  isLoading = false;
  loginState: 'idle' | 'success' | 'error' = 'idle';
  emailError = '';
  passwordError = '';

  constructor(private router: Router) {}

  ngOnInit(): void {}

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  clearState(): void {
    this.loginState = 'idle';
    this.emailError = '';
    this.passwordError = '';
  }

  async onSubmit(): Promise<void> {
    this.loginState = 'idle';
    this.emailError = '';
    this.passwordError = '';

    let valid = true;

    if (!this.email) {
      this.emailError = 'E-mail é obrigatório';
      valid = false;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email)) {
      this.emailError = 'Digite um e-mail válido';
      valid = false;
    }

    if (!this.password) {
      this.passwordError = 'Senha é obrigatória';
      valid = false;
    }

    if (!valid) return;

    this.isLoading = true;

    try {
      // TODO: inject HttpClient e chamar POST http://localhost:8090/api/auth/login
      // const res = await this.http.post<any>('/api/auth/login', { email: this.email, password: this.password }).toPromise();
      // localStorage.setItem('auth_token', res.data.token);

      this.loginState = 'success';
      setTimeout(() => this.router.navigate(['/dashboard']), 1500);

    } catch (err: any) {
      this.loginState = 'error';
      const msg = (err?.message ?? '').toLowerCase();

      if (msg.includes('invalid') || msg.includes('credenciais') || msg.includes('401')) {
        this.passwordError = 'E-mail ou senha incorretos';
      } else if (msg.includes('not found') || msg.includes('404')) {
        this.passwordError = 'Usuário não encontrado';
      } else if (msg.includes('inactive') || msg.includes('inativo')) {
        this.passwordError = 'Conta inativa';
      } else if (msg.includes('locked') || msg.includes('bloqueada')) {
        this.passwordError = 'Conta bloqueada';
      } else if (msg.includes('timeout') || msg.includes('network')) {
        this.passwordError = 'Problema de conexão';
      } else if (msg.includes('500')) {
        this.passwordError = 'Erro no servidor';
      } else {
        this.passwordError = 'E-mail ou senha incorretos';
      }
    } finally {
      this.isLoading = false;
    }
  }
}
