import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { RegistroChegadaService } from './registro-chegada.service';
import { RegistroChegadaForm } from '../models/registro-chegada.model';
describe('API da portaria', () => {
  let service: RegistroChegadaService; let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] }); service = TestBed.inject(RegistroChegadaService); http = TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());
  it('consulta pagina no servidor', () => {
    service.getFila(1, 8).subscribe();
    const r = http.expectOne(r => r.url === '/aratech/portaria/chegadas' && r.params.get('pagina') === '1' && r.params.get('tamanho') === '8'); expect(r.request.method).toBe('GET'); r.flush({ content: [], totalElements: 0 });
  });
  it('envia chave para repeticoes seguras do cadastro', () => {
    const form: RegistroChegadaForm = { notaFiscal: '123', empresa: 'Empresa', nomeMotorista: 'Motorista', placa: 'ABC1234', descricaoCarga: 'Madeira', setorResponsavel: 'ALMOXARIFADO' };
    service.add(form, 'uuid').subscribe(); const r = http.expectOne('/aratech/portaria/chegadas'); expect(r.request.body).toEqual({ ...form, chaveOperacao: 'uuid' }); r.flush({ id: 1 });
  });
  it('entrada usa ID e nao confunde notas iguais de empresas diferentes', () => {
    service.confirmarEntrada(42).subscribe(); const r = http.expectOne('/aratech/portaria/chegadas/42/entrada'); expect(r.request.method).toBe('POST'); r.flush({});
  });
  it('filtra historico antes de paginar no servidor', () => {
    service.getHistorico(2, 8, '123', '2026-10-30').subscribe();
    const r = http.expectOne(r => r.url.endsWith('/historico') && r.params.get('pagina') === '2' && r.params.get('notaFiscal') === '123' && r.params.get('data') === '2026-10-30'); expect(r.request.params.get('tamanho')).toBe('8'); r.flush({});
  });
  it('carrega todas as paginas do historico sem alterar o modal original', () => {
    service.getHistoricoCompleto().subscribe(itens => expect(itens.map(i => i.id)).toEqual([1, 2]));
    const primeira = http.expectOne(r => r.url.endsWith('/historico') && r.params.get('pagina') === '0' && r.params.get('tamanho') === '100');
    primeira.flush({ content: [{ id: 1 }], number: 0, totalPages: 2 });
    const segunda = http.expectOne(r => r.url.endsWith('/historico') && r.params.get('pagina') === '1');
    segunda.flush({ content: [{ id: 2 }], number: 1, totalPages: 2 });
  });
});
