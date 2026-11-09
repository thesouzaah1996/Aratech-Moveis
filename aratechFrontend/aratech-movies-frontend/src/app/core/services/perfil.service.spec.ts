import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PerfilService } from './perfil.service';

describe('API de atribuição de perfis', () => {
  let service: PerfilService; let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(PerfilService); http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('envia busca e paginação para o servidor', () => {
    service.buscarUsuarios(' Ana ', 1, 8).subscribe();
    const req = http.expectOne(r => r.url === '/aratech/rh/perfis/usuarios');
    expect(req.request.params.get('busca')).toBe('Ana'); expect(req.request.params.get('pagina')).toBe('1');
    req.flush({ content: [], totalElements: 0 });
  });
  it('envia apenas o nome do perfil para o usuário escolhido', () => {
    service.atribuir(7, ' Operador ').subscribe();
    const req = http.expectOne('/aratech/rh/perfis/usuarios/7/atribuicoes');
    expect(req.request.method).toBe('POST'); expect(req.request.body).toEqual({ nome: 'Operador' });
    req.flush({ id: 7, nome: 'Ana', perfis: [] });
  });
});
