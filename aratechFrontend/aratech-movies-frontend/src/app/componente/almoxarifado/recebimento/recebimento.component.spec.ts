import { SessaoState } from '../../../core/services/sessao-state.service';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { fakeAsync, tick, TestBed, ComponentFixture } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { RecebimentoComponent } from './recebimento.component';
import { RecebimentoService } from '../../../core/services/recebimento.service';
import { RegistroChegada } from '../../../core/models/registro-chegada.model';
const registro: RegistroChegada = { id: 42, notaFiscal: '123', empresa: 'Empresa', nomeMotorista: 'Motorista', placa: 'ABC1234', descricaoCarga: 'Madeira', setorResponsavel: 'ALMOXARIFADO', status: 'AGUARDANDO', dataChegada: '2026-10-30T10:00:00Z', dataAutorizacao: null, dataEntrada: null, dataFinalizacao: null };
const pagina = { content: [registro], number: 0, size: 8, totalElements: 17, totalPages: 3 };
const resumo = { aguardando: 17, autorizados: 0, emRecebimento: 0, ultimaChegadaId: 42, ultimaEmpresa: 'Empresa' };
describe('Recebimento do almoxarifado', () => {
  let c: RecebimentoComponent; let service: jasmine.SpyObj<RecebimentoService>;
  beforeEach(() => {
    service = jasmine.createSpyObj('RecebimentoService', ['getTodos', 'resumo', 'autorizar', 'finalizar', 'atualizar', 'getHistoricoCompleto']); service.getTodos.and.returnValue(of(pagina)); service.resumo.and.returnValue(of(resumo));
    c = new RecebimentoComponent(service); (c as any).confirmAcaoModal = { show: jasmine.createSpy('show'), hide: jasmine.createSpy('hide'), dispose: () => {} };
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

describe('Botoes e modais reais - recebimento', () => {
  let fixture: ComponentFixture<RecebimentoComponent>;
  let service: jasmine.SpyObj<RecebimentoService>;
  beforeEach(async () => {
    service = jasmine.createSpyObj('RecebimentoService', ['getTodos', 'resumo', 'autorizar', 'finalizar', 'atualizar', 'getHistoricoCompleto']);
    service.getTodos.and.returnValue(of(pagina)); service.resumo.and.returnValue(of(resumo));
    service.getHistoricoCompleto.and.returnValue(of([{ ...registro, status: 'FINALIZADO', dataFinalizacao: '2026-10-30T11:00:00Z' }]));
    await TestBed.configureTestingModule({ imports: [RecebimentoComponent], providers: [provideRouter([]),
      { provide: RecebimentoService, useValue: service }, { provide: AuthService, useValue: { logout: () => {} } }] }).compileComponents();
    TestBed.inject(SessaoState).usuario.set({id:1,nome:'Operador autorizado',email:'teste@example.test',administrador:true,perfis:[],permissoes:[],trocarSenha:false}); fixture=TestBed.createComponent(RecebimentoComponent);
  });
  afterEach(() => {
    fixture?.destroy();
    document.querySelectorAll('.modal-backdrop').forEach(e => e.remove());
    document.body.classList.remove('modal-open'); document.body.style.removeProperty('overflow'); document.body.style.removeProperty('padding-right');
  });
  function encerrar(): void {
    fixture.nativeElement.querySelectorAll('.modal.show').forEach((el: HTMLElement) => (window as any).bootstrap.Modal.getInstance(el)?.hide());
    tick(500); fixture.destroy(); tick(100);
  }
  it('abre o historico pelo botao existente e mostra as entregas finalizadas', fakeAsync(() => {
    try {
      fixture.detectChanges(); tick();
      const botao = Array.from(fixture.nativeElement.querySelectorAll('button') as NodeListOf<HTMLButtonElement>).find(b => b.textContent?.includes('Hist'))!;
      botao.click(); fixture.detectChanges(); tick(400); fixture.detectChanges();
      const modal = fixture.componentInstance.historicoModalEl.nativeElement as HTMLElement;
      expect(modal.classList.contains('show')).toBeTrue();
      expect(modal.textContent).toContain('123'); expect(modal.textContent).toContain('Finalizado');
      expect(service.getHistoricoCompleto).toHaveBeenCalled();
      (modal.querySelector('.btn-cancelar') as HTMLButtonElement).click(); tick(400);
      expect(modal.classList.contains('show')).toBeFalse();
    } finally { encerrar(); }
  }));
  it('autoriza a entrada pelo cadeado e confirma a comunicacao com a portaria', fakeAsync(() => {
    try {
      fixture.detectChanges(); tick();
      (fixture.nativeElement.querySelector('.action-buttons button') as HTMLButtonElement).click(); fixture.detectChanges(); tick(400);
      const modal = fixture.componentInstance.confirmAcaoModalEl.nativeElement as HTMLElement;
      expect(modal.classList.contains('show')).toBeTrue();
      service.autorizar.and.returnValue(of({ ...registro, status: 'AUTORIZADO' }));
      service.getTodos.and.returnValue(of({ ...pagina, content: [{ ...registro, status: 'AUTORIZADO' }] }));
      (modal.querySelector('button.btn-success') as HTMLButtonElement).click(); tick(400); fixture.detectChanges();
      expect(service.autorizar).toHaveBeenCalledWith(42);
      expect(fixture.nativeElement.textContent).toContain('Autorizado'); expect(modal.classList.contains('show')).toBeFalse();
    } finally { encerrar(); }
  }));
  it('o botao de editar abre o formulario e salva pelo ID compartilhado', fakeAsync(() => {
    try {
      fixture.detectChanges(); tick();
      (fixture.nativeElement.querySelector('.action-buttons button[title="Editar registro"]') as HTMLButtonElement).click();
      fixture.detectChanges(); tick(400);
      const modal = fixture.componentInstance.editModalEl.nativeElement as HTMLElement;
      expect(modal.classList.contains('show')).toBeTrue();
      const descricao = modal.querySelector('textarea')!; descricao.value = 'Carga revisada'; descricao.dispatchEvent(new Event('input')); tick(); fixture.detectChanges();
      service.atualizar.and.returnValue(of({ ...registro, descricaoCarga: 'Carga revisada' }));
      (modal.querySelector('.modal-footer .btn-danger') as HTMLButtonElement).click(); tick(400); fixture.detectChanges();
      expect(service.atualizar).toHaveBeenCalledWith(42, jasmine.objectContaining({ descricaoCarga: 'Carga revisada' }));
      expect(modal.classList.contains('show')).toBeFalse();
    } finally { encerrar(); }
  }));
  it('finaliza pelo botao existente somente depois da entrada na portaria', fakeAsync(() => {
    try {
      service.getTodos.and.returnValue(of({ ...pagina, content: [{ ...registro, status: 'EM_RECEBIMENTO' }] }));
      fixture.detectChanges(); tick();
      const botao = fixture.nativeElement.querySelector('.action-buttons button') as HTMLButtonElement;
      expect(botao.disabled).toBeFalse(); botao.click(); fixture.detectChanges(); tick(400);
      service.finalizar.and.returnValue(of({ ...registro, status: 'FINALIZADO' }));
      service.getTodos.and.returnValue(of({ ...pagina, content: [], totalElements: 0, totalPages: 0 }));
      const modal = fixture.componentInstance.confirmAcaoModalEl.nativeElement as HTMLElement;
      (modal.querySelector('button.btn-success') as HTMLButtonElement).click(); tick(400); fixture.detectChanges();
      expect(service.finalizar).toHaveBeenCalledWith(42); expect(fixture.componentInstance.fila.length).toBe(0);
      expect(modal.classList.contains('show')).toBeFalse();
    } finally { encerrar(); }
  }));
});
