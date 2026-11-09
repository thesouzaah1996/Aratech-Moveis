import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { TrocarSenhaComponent } from './trocar-senha.component';
import { AuthService } from '../../core/services/auth.service';

describe('Troca de senha com mínimo de oito caracteres', () => {
  let component: TrocarSenhaComponent;
  let auth: jasmine.SpyObj<AuthService>;
  beforeEach(() => {
    auth = jasmine.createSpyObj('AuthService', ['alterarSenha', 'logout']);
    component = new TrocarSenhaComponent(auth, jasmine.createSpyObj<Router>('Router', ['navigate']));
    component.senhaAtual = 'temporaria';
  });
  it('permite uma senha forte de oito caracteres e limpa os campos após salvar', () => {
    component.novaSenha = 'k9!Z7@wR'; component.confirmacao = component.novaSenha;
    auth.alterarSenha.and.returnValue(of({ mensagem: 'Senha alterada.' }));
    component.salvar();
    expect(auth.alterarSenha).toHaveBeenCalledOnceWith('temporaria', 'k9!Z7@wR');
    expect(component.sucesso).toBeTrue(); expect(component.novaSenha).toBe(''); expect(component.senhaAtual).toBe('');
  });
  it('bloqueia senhas curtas e confirmação diferente sem enviar ao servidor', () => {
    component.novaSenha = 'Ab!23'; component.confirmacao = component.novaSenha; component.salvar();
    expect(auth.alterarSenha).not.toHaveBeenCalled();
    component.novaSenha = 'k9!Z7@wR'; component.confirmacao = 'diferente'; component.salvar();
    expect(auth.alterarSenha).not.toHaveBeenCalled();
  });
  it('mostra a rejeição de força do backend e permite corrigir a senha', () => {
    component.novaSenha = 'Senha123!'; component.confirmacao = component.novaSenha;
    auth.alterarSenha.and.returnValue(throwError(() => ({ error: { mensagem: 'Senha muito previsível.' } })));
    component.salvar();
    expect(component.erro).toBe('Senha muito previsível.'); expect(component.sucesso).toBeFalse(); expect(component.salvando).toBeFalse();
  });
});
