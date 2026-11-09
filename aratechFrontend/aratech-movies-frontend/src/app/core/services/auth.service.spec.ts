import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from './auth.service';
import { SessaoState, SessaoUsuario } from './sessao-state.service';
import { securityInterceptor } from '../security.interceptor';
import { acessoGuard } from '../acesso.guard';
import { Observable } from 'rxjs';

describe('Login com sessão e controle de acesso', () => {
  let auth: AuthService; let http: HttpTestingController; let state: SessaoState;
  const usuario: SessaoUsuario = { id: 7, nome: 'Ana', email: 'ana@example.test', administrador: false, perfis: ['RH'], permissoes: ['RH.CONSULTAR'], trocarSenha: false };
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(withInterceptors([securityInterceptor])), provideHttpClientTesting(), provideRouter([])] });
    auth = TestBed.inject(AuthService); http = TestBed.inject(HttpTestingController); state = TestBed.inject(SessaoState);
  });
  afterEach(() => http.verify());
  it('envia senha e CSRF ao backend e não armazena token no navegador', () => {
    auth.login(' ANA@example.test ', ' Frase de senha com espaços ').subscribe();
    http.expectOne('/aratech/auth/csrf').flush({ token: 'csrf-antes-do-login' });
    const req = http.expectOne('/aratech/auth/entrar');
    expect(req.request.headers.get('X-XSRF-TOKEN')).toBe('csrf-antes-do-login');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    expect(req.request.body.get('emailCorporativo')).toBe('ana@example.test');
    expect(req.request.body.get('senha')).toBe(' Frase de senha com espaços ');
    req.flush(usuario);
    expect(state.usuario()?.id).toBe(7); expect(localStorage.getItem('token')).toBeNull(); expect(localStorage.getItem('perfis')).toBeNull();
    auth.logout().subscribe();
    http.expectOne('/aratech/auth/csrf').flush({ token: 'csrf-depois-do-login' });
    const sair = http.expectOne('/aratech/auth/sair');
    expect(sair.request.headers.get('X-XSRF-TOKEN')).toBe('csrf-depois-do-login');
    sair.flush({ mensagem: 'Sessão encerrada.' }); expect(auth.isAuthenticated()).toBeFalse();
  });
  it('recupera a sessão do servidor ao navegar e rejeita módulo não permitido', () => {
    let resultado: unknown;
    const route = { routeConfig: { path: 'financeiro' } } as ActivatedRouteSnapshot;
    const guard = TestBed.runInInjectionContext(() => acessoGuard(route, { url: '/financeiro' } as RouterStateSnapshot)) as Observable<boolean | UrlTree>;
    guard.subscribe(r => resultado = r);
    http.expectOne('/aratech/auth/sessao').flush(usuario);
    expect(state.usuario()?.nome).toBe('Ana'); expect(String(resultado)).toBe('/sem-acesso');
  });
  it('exige troca de senha antes de liberar um administrador temporário', () => {
    let resultado: unknown;
    const guard = TestBed.runInInjectionContext(() => acessoGuard({ routeConfig: { path: 'dashboard' } } as ActivatedRouteSnapshot, { url: '/dashboard' } as RouterStateSnapshot)) as Observable<boolean | UrlTree>;
    guard.subscribe(r => resultado = r);
    http.expectOne('/aratech/auth/sessao').flush({ ...usuario, administrador: true, trocarSenha: true });
    expect(String(resultado)).toBe('/trocar-senha');
    expect(state.permite('ACESSOS.GERENCIAR')).toBeFalse();
  });
  it('não aceita perfis gravados pelo cliente como autenticação', () => {
    localStorage.setItem('token', 'forjado'); localStorage.setItem('perfis', '["ADMIN"]');
    expect(auth.isAuthenticated()).toBeFalse(); expect(auth.temPermissao('ACESSOS.GERENCIAR')).toBeFalse();
    localStorage.removeItem('token'); localStorage.removeItem('perfis');
  });
  it('limpa a sessão expirada e encaminha ao login em chamadas protegidas', () => {
    state.usuario.set(usuario);
    const navegar = spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
    TestBed.inject(HttpClient).get('/aratech/rh/funcionarios').subscribe({ error: () => {} });
    http.expectOne('/aratech/rh/funcionarios').flush({ codigo: 'NAO_AUTENTICADO' }, { status: 401, statusText: 'Unauthorized' });
    expect(state.usuario()).toBeNull(); expect(navegar).toHaveBeenCalled();
  });
  it('não envia o CSRF para sites externos', () => {
    TestBed.inject(HttpClient).post('https://example.test/externo', {}).subscribe();
    const req = http.expectOne('https://example.test/externo');
    expect(req.request.headers.has('X-XSRF-TOKEN')).toBeFalse(); req.flush({});
  });
  it('remove o estado local quando o servidor informa que a sessão expirou', () => {
    state.usuario.set(usuario); auth.verificarSessao().subscribe({ error: () => {} });
    http.expectOne('/aratech/auth/sessao').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.isAuthenticated()).toBeFalse();
  });
  it('limpa a autenticação e renova o CSRF após trocar a senha autenticada', () => {
    state.usuario.set(usuario);
    auth.alterarSenha('Senha anterior', 'Uma nova frase de senha!').subscribe();
    http.expectOne('/aratech/auth/csrf').flush({ token: 'anterior' });
    http.expectOne('/aratech/auth/senha').flush({ mensagem: 'Senha alterada.' });
    expect(state.usuario()).toBeNull();
    auth.login('ana@example.test', 'Uma nova frase de senha!').subscribe();
    http.expectOne('/aratech/auth/csrf').flush({ token: 'novo' });
    const req = http.expectOne('/aratech/auth/entrar');
    expect(req.request.headers.get('X-XSRF-TOKEN')).toBe('novo'); req.flush(usuario);
  });
});
