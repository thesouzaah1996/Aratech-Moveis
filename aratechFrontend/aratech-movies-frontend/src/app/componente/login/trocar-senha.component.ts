import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-trocar-senha', standalone: true, imports: [FormsModule, RouterLink],
  styleUrl: './trocar-senha.component.scss',
  template: `
    <main class="app-main"><div class="container py-5" style="max-width: 540px;"><div class="card shadow-sm"><div class="card-body p-4">
      <h4 class="fw-bold mb-3">Definir nova senha</h4>
      @if (sucesso) {
        <div class="alert alert-success" role="status">Senha alterada. Entre novamente com a nova senha.</div><a routerLink="/login" class="btn btn-danger">Ir para o login</a>
      } @else {
        @if (erro) { <div class="alert alert-danger" role="alert">{{ erro }}</div> }
        <form (ngSubmit)="salvar()">
          <label class="form-label" for="senha-atual">Senha atual</label><input id="senha-atual" name="senhaAtual" type="password" class="form-control mb-3" autocomplete="current-password" maxlength="128" [(ngModel)]="senhaAtual" [disabled]="salvando" required>
          <label class="form-label" for="nova-senha">Nova senha</label><input id="nova-senha" name="novaSenha" type="password" class="form-control mb-3" autocomplete="new-password" maxlength="128" [(ngModel)]="novaSenha" [disabled]="salvando" required>
          <label class="form-label" for="confirma-senha">Confirme a nova senha</label><input id="confirma-senha" name="confirmacao" type="password" class="form-control mb-4" autocomplete="new-password" maxlength="128" [(ngModel)]="confirmacao" [disabled]="salvando" required>
          <div class="d-flex justify-content-between gap-2"><button type="button" class="btn btn-cancelar" [disabled]="salvando" (click)="sair()">Sair</button><button type="submit" class="btn btn-danger" [disabled]="salvando">{{ salvando ? 'Salvando...' : 'Confirmar' }}</button></div>
        </form>
      }
    </div></div></div></main>`
})
export class TrocarSenhaComponent {
  senhaAtual = ''; novaSenha = ''; confirmacao = ''; erro = ''; salvando = false; sucesso = false;
  constructor(private auth: AuthService, private router: Router) {}
  salvar(): void {
    if (this.salvando) return;
    this.erro = '';
    if (!this.senhaAtual || Array.from(this.novaSenha).length < 8 || this.novaSenha.length > 128) { this.erro = 'Informe a senha atual e uma nova senha de 8 a 128 caracteres.'; return; }
    if (this.novaSenha !== this.confirmacao) { this.erro = 'As senhas não coincidem.'; return; }
    this.salvando = true;
    this.auth.alterarSenha(this.senhaAtual, this.novaSenha).subscribe({
      next: () => { this.salvando = false; this.sucesso = true; this.senhaAtual = ''; this.novaSenha = ''; this.confirmacao = ''; },
      error: e => { this.salvando = false; this.erro = e.error?.mensagem || 'Não foi possível alterar a senha. Tente novamente.'; }
    });
  }
  sair(): void {
    if (this.salvando) return;
    this.salvando = true;
    this.auth.logout().subscribe({ next: () => { void this.router.navigate(['/login']); }, error: () => { this.salvando = false; this.erro = 'Não foi possível encerrar a sessão. Tente novamente.'; } });
  }
}
