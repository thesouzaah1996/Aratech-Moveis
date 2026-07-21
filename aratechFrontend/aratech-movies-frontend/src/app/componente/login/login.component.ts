import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

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

  constructor(private router: Router, private authService: AuthService) {}

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

    this.authService.login(this.email, this.password).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.loginState = 'success';
        this.authService.salvarSessao(res.dados.token, res.dados.perfis);
        setTimeout(() => this.router.navigate(['/dashboard']), 1500);
      },
      error: () => {
        this.isLoading = false;
        this.loginState = 'error';
      }
    });
  }
}
