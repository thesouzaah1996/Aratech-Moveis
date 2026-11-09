import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { SolicitacaoPecaService } from './solicitacao-peca.service';
describe('SolicitacaoPecaService', () => {
  let service: SolicitacaoPecaService; let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] }); service=TestBed.inject(SolicitacaoPecaService); http=TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());
  it('envia dados e chave e devolve o numero do servidor', () => {
    const form = { nomePeca: 'Rolamento', codigo: '', quantidade: 1.5, unidade: 'kg', equipamento: 'Prensa', finalidade: 'MELHORIA' as const, prioridade: 'ALTA' as const, solicitante: 'Ana Silva', setor: 'Producao', telefone: '', observacoes: 'Trocar rolamento' };
    service.add(form,'chave').subscribe(r => expect(r.id).toBe(42));
    const req=http.expectOne('/aratech/manutencao/solicitacoes-pecas'); expect(req.request.method).toBe('POST');expect(req.request.body.quantidade).toBe(1.5);expect(req.request.body.chaveOperacao).toBe('chave');expect(req.request.body.id).toBeUndefined();
    req.flush({ solicitacao: { ...form, id: 42, status: 'ABERTA', criadoEm: '2026-11-01T10:00:00Z' } });
  });
});
