import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ChamadoService } from './chamado.service';
import { Chamado, ChamadoForm } from '../models/chamado.model';
const form: ChamadoForm = { equipamento: 'Prensa', tipo: 'CORRETIVA', prioridade: 'ALTA', solicitante: 'Ana Silva', setor: 'Producao', telefone: '', descricao: 'Motor parado' };
const chamado: Chamado = { ...form, id: 42, tipo: 'CORRETIVA', prioridade: 'ALTA', status: 'ABERTA', dataAbertura: '2026-11-01T10:00:00Z' };
describe('ChamadoService - API de manutencao', () => {
  let service: ChamadoService; let http: HttpTestingController;
  const url = '/aratech/manutencao/chamados';
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ChamadoService); http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('envia os dados e a chave, sem inventar status ou horario no cliente', () => {
    service.add(form, '11111111-2222-4333-8444-555555555555').subscribe(c => expect(c).toEqual(chamado));
    const req = http.expectOne(url); expect(req.request.method).toBe('POST');
    expect(req.request.body.chaveOperacao).toBe('11111111-2222-4333-8444-555555555555');
    expect(req.request.body.status).toBeUndefined(); expect(req.request.body.dataAbertura).toBeUndefined();
    req.flush({ status: 201, chamado });
  });
  it('percorre todas as paginas mantendo o filtro de status', () => {
    let lista: Chamado[] = [];
    service.getAll('ABERTA').subscribe(itens => lista = itens);
    const primeira = http.expectOne(r => r.url === url && r.params.get('pagina') === '0');
    expect(primeira.request.params.get('status')).toBe('ABERTA');
    primeira.flush({ chamados: [chamado], pagina: 0, tamanho: 100, total: 101 });
    const segunda = http.expectOne(r => r.url === url && r.params.get('pagina') === '1');
    expect(segunda.request.params.get('status')).toBe('ABERTA');
    segunda.flush({ chamados: [{ ...chamado, id: 43 }], pagina: 1, tamanho: 100, total: 101 });
    expect(lista.map(c => c.id)).toEqual([42, 43]);
  });
  it('consulta pelo ID e propaga indisponibilidade sem devolver lista vazia', () => {
    service.getById(42).subscribe(c => expect(c.id).toBe(42)); http.expectOne(url + '/42').flush({ chamado });
    let falhou = false;
    service.getAll().subscribe({ next: () => fail('Nao deve ocultar o erro'), error: () => falhou = true });
    http.expectOne(r => r.url === url).flush({}, { status: 503, statusText: 'Service Unavailable' });
    expect(falhou).toBeTrue();
  });
});
