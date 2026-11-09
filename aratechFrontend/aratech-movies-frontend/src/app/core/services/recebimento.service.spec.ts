import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { RecebimentoService } from './recebimento.service';
describe('API do recebimento', () => {
  let service: RecebimentoService; let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] }); service = TestBed.inject(RecebimentoService); http = TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());
  it('consulta apenas o recebimento do almoxarifado com pagina do servidor', () => {
    service.getTodos(1, 8).subscribe(); const r = http.expectOne(r => r.url === '/aratech/almoxarifado/recebimentos' && r.params.get('pagina') === '1'); expect(r.request.params.get('tamanho')).toBe('8'); r.flush({ content: [], totalElements: 0 });
  });
  it('autoriza e finaliza por identificador', () => {
    service.autorizar(42).subscribe(); const a = http.expectOne('/aratech/almoxarifado/recebimentos/42/autorizar'); expect(a.request.method).toBe('POST'); a.flush({});
    service.finalizar(42).subscribe(); const f = http.expectOne('/aratech/almoxarifado/recebimentos/42/finalizar'); expect(f.request.method).toBe('POST'); f.flush({});
  });
  it('resumo global notifica novas chegadas fora da pagina atual', () => {
    service.resumo().subscribe(r => expect(r.aguardando).toBe(20)); http.expectOne('/aratech/almoxarifado/recebimentos/resumo').flush({ aguardando: 20, ultimaChegadaId: 42 });
  });
  it('carrega todas as paginas do historico sem alterar o modal original', () => {
    service.getHistoricoCompleto().subscribe(itens => expect(itens.map(i => i.id)).toEqual([1, 2]));
    const primeira = http.expectOne(r => r.url.endsWith('/historico') && r.params.get('pagina') === '0' && r.params.get('tamanho') === '100');
    primeira.flush({ content: [{ id: 1 }], number: 0, totalPages: 2 });
    const segunda = http.expectOne(r => r.url.endsWith('/historico') && r.params.get('pagina') === '1');
    segunda.flush({ content: [{ id: 2 }], number: 1, totalPages: 2 });
  });
  it('edita pelo ID do registro compartilhado com a portaria', () => {
    const form = { notaFiscal: '123', empresa: 'Empresa', nomeMotorista: 'Motorista', placa: 'ABC1234', descricaoCarga: 'Carga corrigida', setorResponsavel: 'ALMOXARIFADO' as const };
    service.atualizar(42, form).subscribe();
    const r = http.expectOne('/aratech/almoxarifado/recebimentos/42');
    expect(r.request.method).toBe('PUT'); expect(r.request.body).toEqual(form); r.flush({ id: 42, ...form });
  });
});
