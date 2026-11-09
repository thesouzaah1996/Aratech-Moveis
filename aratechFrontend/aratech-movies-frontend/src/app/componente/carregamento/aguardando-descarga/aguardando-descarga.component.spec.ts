import { provideRouter } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { fakeAsync, tick, TestBed, ComponentFixture } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { CarregamentoAguardandoDescargaComponent } from './aguardando-descarga.component';
import { CarregamentoService } from '../../../core/services/carregamento.service';
import { RegistroChegada } from '../../../core/models/registro-chegada.model';
const registro: RegistroChegada = { id: 42, notaFiscal: '123', empresa: 'Empresa', nomeMotorista: 'Motorista', placa: 'ABC1234', descricaoCarga: 'Madeira', setorResponsavel: 'CARREGAMENTO', tipoOperacao: 'DESCARREGAR', status: 'AGUARDANDO', dataChegada: '2026-10-30T10:00:00Z', dataAutorizacao: null, dataEntrada: null, dataFinalizacao: null };
const pagina = { content: [registro], number: 0, size: 8, totalElements: 17, totalPages: 3 };
const resumo = { aguardando: 17, autorizados: 0, emRecebimento: 0, ultimaChegadaId: 42, ultimaEmpresa: 'Empresa' };
describe('Descarga do carregamento', () => {
  let c: CarregamentoAguardandoDescargaComponent; let service: jasmine.SpyObj<CarregamentoService>;
  beforeEach(() => {
    service = jasmine.createSpyObj('CarregamentoService', ['getTodos', 'resumo', 'autorizar', 'finalizar', 'atualizar', 'getHistoricoCompleto']); service.getTodos.and.returnValue(of(pagina)); service.resumo.and.returnValue(of(resumo));
    c = new CarregamentoAguardandoDescargaComponent(service); (c as any).confirmAcaoModal = { show: jasmine.createSpy('show'), hide: jasmine.createSpy('hide'), dispose: () => {} };
    service.getHistoricoCompleto.and.returnValue(of([]));
    (c as any).editModal = { show: jasmine.createSpy('show'), hide: jasmine.createSpy('hide'), dispose: () => {} };
    (c as any).historicoModal = { show: jasmine.createSpy('show'), hide: jasmine.createSpy('hide'), dispose: () => {} };
  });
  it('notifica automaticamente nova chegada mesmo fora da pagina atual', fakeAsync(() => {
    c.ngOnInit(); expect(c.novaChegada).toBe(''); service.resumo.and.returnValue(of({ ...resumo, ultimaChegadaId: 43, ultimaEmpresa: 'Nova Empresa' })); tick(5000);
    expect(c.novaChegada).toContain('Nova Empresa'); c.ngOnDestroy(); tick(10000); expect(service.getTodos).toHaveBeenCalledTimes(2);
  }));
  it('nao finaliza enquanto a portaria nao confirmar a entrada', () => { c.abrirConfirmacao({ ...registro, status: 'AUTORIZADO' }, 'finalizar'); c.confirmarAcao(); expect(service.finalizar).not.toHaveBeenCalled(); });
  it('autoriza por ID e informa a proxima etapa', () => {
    service.autorizar.and.returnValue(of({ ...registro, status: 'AUTORIZADO' })); c.abrirConfirmacao(registro, 'autorizar'); c.confirmarAcao(); expect(service.autorizar).toHaveBeenCalledWith(42); expect(c.successMessage).toContain('portaria');
  });
  it('mantem modal aberto e exibe conflito retornado pelo servidor', () => {
    service.autorizar.and.returnValue(throwError(() => ({ error: { mensagem: 'Etapa invalida.' } }))); c.abrirConfirmacao(registro, 'autorizar'); c.confirmarAcao(); expect(c.modalError).toBe('Etapa invalida.'); expect((c as any).confirmAcaoModal.hide).not.toHaveBeenCalled();
  });
  it('usa total global e pagina do servidor', () => { c.carregarTodos(); c.setPage(2); expect(service.getTodos).toHaveBeenCalledWith(1, 8); expect(c.fila.length).toBe(1); expect(c.totalPages).toBe(3); });
  it('edita o recebimento aguardando e compartilha a alteracao com a portaria', () => {
    service.atualizar.and.returnValue(of({ ...registro, descricaoCarga: 'Carga corrigida' }));
    c.abrirEdicao(registro); c.editForm.descricaoCarga = 'Carga corrigida'; c.salvarEdicao();
    expect(service.atualizar).toHaveBeenCalledWith(42, jasmine.objectContaining({ descricaoCarga: 'Carga corrigida' }));
    expect(c.successMessage).toContain('portaria');
  });
  it('nao abre a edicao de um recebimento autorizado', () => {
    c.abrirEdicao({ ...registro, status: 'AUTORIZADO' });
    expect((c as any).editModal.show).not.toHaveBeenCalled();
  });
  it('mantem o historico aberto atualizado depois de finalizacoes em outra tela', fakeAsync(() => {
    const modal = document.createElement('div'); c.historicoModalEl = { nativeElement: modal };
    c.ngOnInit(); modal.classList.add('show');
    service.getHistoricoCompleto.and.returnValue(of([{ ...registro, status: 'FINALIZADO' }])); tick(5000);
    expect(c.historico[0].status).toBe('FINALIZADO'); c.ngOnDestroy();
  }));
});

