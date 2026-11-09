import { LoginComponent } from './login.component';
import { of } from 'rxjs';

describe('Login corporativo', () => {
  let auth: any;
  let component: LoginComponent;
  beforeEach(() => {
    auth = { login: jasmine.createSpy('login').and.returnValue(of({ trocarSenha: false })) };
    component = new LoginComponent({ navigateByUrl: jasmine.createSpy(), navigate: jasmine.createSpy() } as any, auth,
      { snapshot: { queryParamMap: { get: () => null } } } as any);
    component.password = 'senha';
  });
  for (const login of ['admin', 'admin@example.com', 'admin@aratechmoveis.com', '.ana@aratechmoveis', 'ana..silva@aratechmoveis', 'ana silva@aratechmoveis', 'a'.repeat(65)+'@aratechmoveis']) {
    it('bloqueia antes da API: '+login, () => {
      component.email = login; component.onSubmit();
      expect(component.emailError).toContain('nome@aratechmoveis');
      expect(auth.login).not.toHaveBeenCalled();
    });
  }
  it('aceita domínio interno sem extensão', () => {
    component.email = ' admin@aratechmoveis '; component.onSubmit();
    expect(auth.login).toHaveBeenCalledOnceWith('admin@aratechmoveis', 'senha');
  });
});
