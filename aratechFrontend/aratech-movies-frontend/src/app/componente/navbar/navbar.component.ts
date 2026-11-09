import { PermissaoDirective } from '../../core/permissao.directive';
import { Component, ElementRef, ViewChild, inject } from '@angular/core';
import { SessaoState } from '../../core/services/sessao-state.service';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

declare const bootstrap: any;

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [PermissaoDirective,RouterLink],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  readonly sessao = inject(SessaoState);
  get nomeExibido(): string {
    const partes = this.sessao.usuario()?.nome?.trim().split(/\s+/).filter(Boolean) ?? [];
    return partes.length > 1 ? `${partes[0]} ${partes[partes.length - 1]}` : partes[0] || 'Usuário';
  }
  saindo = false;
  erroLogout = '';
  @ViewChild('logoutModal') logoutModalEl!: ElementRef;

  constructor(private router: Router, private authService: AuthService) {}

  performLogout(): void {
    if (this.saindo) return;
    this.saindo = true; this.erroLogout = '';
    this.authService.logout().subscribe({
      next: () => { this.saindo = false; bootstrap.Modal.getInstance(this.logoutModalEl.nativeElement)?.hide(); void this.router.navigate(['/login']); },
      error: () => { this.saindo = false; this.erroLogout = 'Não foi possível encerrar a sessão. Tente novamente.'; }
    });
  }
}
