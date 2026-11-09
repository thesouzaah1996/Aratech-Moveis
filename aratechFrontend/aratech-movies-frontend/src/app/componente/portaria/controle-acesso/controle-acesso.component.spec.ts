import { SessaoState } from '../../../core/services/sessao-state.service';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { fakeAsync, tick, TestBed, ComponentFixture } from '@angular/core/testing';
import { of, throwError, Subject } from 'rxjs';
import { ControleAcessoComponent } from './controle-acesso.component';
import { RegistroChegadaService } from '../../../core/services/registro-chegada.service';
import { RegistroChegada } from '../../../core/models/registro-chegada.model';
const registro: RegistroChegada = { id: 42, notaFiscal: '123', empresa: 'Empresa', nomeMotorista: 'Motorista', placa: 'ABC1234', descricaoCarga: 'Madeira', setorResponsavel: 'ALMOXARIFADO', status: 'AGUARDANDO', dataChegada: '2026-10-30T10:00:00Z', dataAutorizacao: null, dataEntrada: null, dataFinalizacao: null };
const pagina = { content: [registro], number: 0, size: 8, totalElements: 17, totalPages: 3 };
const resumo = { aguardando: 17, autorizados: 0, emRecebimento: 0, ultimaChegadaId: 42, ultimaEmpresa: 'Empresa' };
describe('Controle de acesso', () => {
  let c: ControleAcessoComponent; let service: jasmine.SpyObj<RegistroChegadaService>;
  beforeEach(() => {
    service = jasmine.createSpyObj('RegistroChegadaService', ['getFila', 'resumo', 'add', 'atualizar', 'confirmarEntrada', 'finalizar', 'getHistoricoCompleto']); service.getFila.and.returnValue(of(pagina)); service.resumo.and.returnValue(of(resumo));
    c = new ControleAcessoComponent(service); (c as any).confirmEntradaModal = { show: jasmine.createSpy('show'), hide: jasmine.createSpy('hide'), dispose: () => {} };
    service.getHistoricoCompleto.and.returnValue(of([]));
    (c as any).editModal = { show: jasmine.createSpy('show'), hide: jasmine.createSpy('hide'), dispose: () => {} };
    (c as any).historicoModal = { show: jasmine.createSpy('show'), hide: jasmine.createSpy('hide'), dispose: () => {} };
  });
  it('rejeita dados invalidos antes de registrar', () => { c.registrarChegada(); expect(service.add).not.toHaveBeenCalled(); expect(c.errorMessage).toBeTruthy(); });
  it('usa a mesma chave e os mesmos dados ao repetir apos falha de rede', () => {
    c.form = { ...registro }; service.add.and.returnValue(throwError(() => ({ status: 0 })));
    c.registrarChegada(); c.registrarChegada();
    expect(service.add).toHaveBeenCalledTimes(2); expect(service.add.calls.argsFor(0)).toEqual(service.add.calls.argsFor(1)); expect(c.cadastroPendente).not.toBeNull();
  });
  it('impede entrada antes da autorizacao', () => { c.abrirConfirmacaoEntrada(registro); c.confirmarEntrada(); expect(service.confirmarEntrada).not.toHaveBeenCalled(); expect(c.errorMessage).toContain('setor'); });
  it('reflete autorizacoes vindas do almoxarifado e encerra polling ao sair', fakeAsync(() => {
    c.ngOnInit(); expect(service.getFila).toHaveBeenCalledTimes(1); tick(5000); expect(service.getFila).toHaveBeenCalledTimes(2); c.ngOnDestroy(); tick(10000); expect(service.getFila).toHaveBeenCalledTimes(2);
  }));
  it('pagina no backend sem cortar novamente os resultados', () => { c.carregarFila(); c.setPage(2); expect(service.getFila).toHaveBeenCalledWith(1, 8); expect(c.fila.length).toBe(1); expect(c.totalPages).toBe(3); });
  it('avisa a portaria e atualiza o estado quando o almoxarifado autoriza', () => {
    c.carregarFila(); service.getFila.and.returnValue(of({ ...pagina, content: [{ ...registro, status: 'AUTORIZADO' }] }));
    service.resumo.and.returnValue(of({ ...resumo, autorizados: 1 })); c.carregarFila();
    expect(c.fila[0].status).toBe('AUTORIZADO'); expect(c.successMessage).toContain('setor responsavel autorizou');
  });
  it('libera a entrada somente depois da autorizacao recebida', () => {
    service.confirmarEntrada.and.returnValue(of({ ...registro, status: 'EM_RECEBIMENTO' }));
    c.abrirConfirmacaoEntrada({ ...registro, status: 'AUTORIZADO' }); c.confirmarEntrada();
    expect(service.confirmarEntrada).toHaveBeenCalledWith(42); expect(c.successMessage).toContain('Entrada liberada');
  });
  it('finaliza somente depois da entrada e envia ao historico', () => {
    c.fila = [{ ...registro, status: 'EM_RECEBIMENTO' }]; c.total = 1;
    service.finalizar.and.returnValue(of({ ...registro, status: 'FINALIZADO' }));
    service.getFila.and.returnValue(of({ ...pagina, content: [], totalElements: 0, totalPages: 0 }));
    c.abrirConfirmacaoEntrada(c.fila[0]); c.confirmarEntrada();
    expect(service.finalizar).toHaveBeenCalledWith(42); expect(c.fila).toEqual([]);
    expect(service.confirmarEntrada).not.toHaveBeenCalled();
  });
  it('salva a edicao sem trocar o identificador compartilhado', () => {
    service.atualizar.and.returnValue(of({ ...registro, descricaoCarga: 'Carga corrigida' }));
    c.abrirEdicao(registro); c.editForm.descricaoCarga = 'Carga corrigida'; c.salvarEdicao();
    expect(service.atualizar).toHaveBeenCalledWith(42, jasmine.objectContaining({ descricaoCarga: 'Carga corrigida' }));
  });
  it('atualiza o historico aberto quando o almoxarifado finaliza', fakeAsync(() => {
    const modal = document.createElement('div'); c.historicoModalEl = { nativeElement: modal };
    c.ngOnInit(); modal.classList.add('show');
    service.getHistoricoCompleto.and.returnValue(of([{ ...registro, status: 'FINALIZADO' }])); tick(5000);
    expect(c.historico[0].status).toBe('FINALIZADO'); c.ngOnDestroy();
  }));
  it('mantem o caminhao na fila apos liberar, ate finalizar', () => {
    c.fila = [{ ...registro, status: 'AUTORIZADO' }]; c.total = 1;
    const proximaPagina = new Subject<typeof pagina>();
    service.getFila.and.returnValue(proximaPagina);
    service.confirmarEntrada.and.returnValue(of({ ...registro, status: 'EM_RECEBIMENTO' }));
    c.abrirConfirmacaoEntrada({ ...registro, status: 'AUTORIZADO' }); c.confirmarEntrada();
    expect(c.fila[0].status).toBe('EM_RECEBIMENTO'); expect(c.total).toBe(1); expect(c.loading).toBeTrue();
    proximaPagina.next({ ...pagina, content: [], totalElements: 0, totalPages: 0 }); proximaPagina.complete();
    expect(c.loading).toBeFalse();
  });
  it('nao desconta o total duas vezes quando outra portaria ja liberou a entrada', () => {
    c.fila = []; c.total = 3;
    const proximaPagina = new Subject<typeof pagina>(); service.getFila.and.returnValue(proximaPagina);
    service.confirmarEntrada.and.returnValue(of({ ...registro, status: 'EM_RECEBIMENTO' }));
    c.abrirConfirmacaoEntrada({ ...registro, status: 'AUTORIZADO' }); c.confirmarEntrada();
    expect(c.total).toBe(3);
    proximaPagina.next({ ...pagina, content: [], totalElements: 3, totalPages: 1 }); proximaPagina.complete();
  });
});

describe('Botoes e modais reais - controle-acesso', () => {
  let fixture: ComponentFixture<ControleAcessoComponent>;
  let service: jasmine.SpyObj<RegistroChegadaService>;
  beforeEach(async () => {
    service = jasmine.createSpyObj('RegistroChegadaService', ['getFila', 'resumo', 'add', 'atualizar', 'confirmarEntrada', 'finalizar', 'getHistoricoCompleto']);
    service.getFila.and.returnValue(of(pagina)); service.resumo.and.returnValue(of(resumo));
    service.getHistoricoCompleto.and.returnValue(of([{ ...registro, status: 'FINALIZADO', dataFinalizacao: '2026-10-30T11:00:00Z' }]));
    await TestBed.configureTestingModule({ imports: [ControleAcessoComponent], providers: [provideRouter([]),
      { provide: RegistroChegadaService, useValue: service }, { provide: AuthService, useValue: { logout: () => {} } }] }).compileComponents();
    TestBed.inject(SessaoState).usuario.set({id:1,nome:'Operador autorizado',email:'teste@example.test',administrador:true,perfis:[],permissoes:[],trocarSenha:false}); fixture=TestBed.createComponent(ControleAcessoComponent);
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
      expect(modal.textContent).toContain('123'); expect(modal.textContent).toContain('Entrada autorizada');
      expect(service.getHistoricoCompleto).toHaveBeenCalled();
      (modal.querySelector('.btn-cancelar') as HTMLButtonElement).click(); tick(400);
      expect(modal.classList.contains('show')).toBeFalse();
    } finally { encerrar(); }
  }));
  it('habilita o cadeado apos autorizacao e libera a entrada pelo modal', fakeAsync(() => {
    try {
      fixture.detectChanges(); tick();
      let botao = fixture.nativeElement.querySelector('.action-buttons button') as HTMLButtonElement;
      expect(botao.disabled).toBeFalse(); botao.click(); fixture.detectChanges(); expect(fixture.componentInstance.errorMessage).toContain('setor');
      service.getFila.and.returnValue(of({ ...pagina, content: [{ ...registro, status: 'AUTORIZADO' }] }));
      service.resumo.and.returnValue(of({ ...resumo, autorizados: 1 })); tick(5000); fixture.detectChanges();
      botao = fixture.nativeElement.querySelector('.action-buttons button') as HTMLButtonElement;
      expect(botao.disabled).toBeFalse(); botao.click(); fixture.detectChanges(); tick(400);
      const modal = fixture.componentInstance.confirmEntradaModalEl.nativeElement as HTMLElement;
      expect(modal.classList.contains('show')).toBeTrue();
      service.confirmarEntrada.and.returnValue(of({ ...registro, status: 'EM_RECEBIMENTO' }));
      service.getFila.and.returnValue(of({ ...pagina, content: [], totalElements: 0, totalPages: 0 }));
      (modal.querySelector('button.btn-success') as HTMLButtonElement).click(); tick(400); fixture.detectChanges();
      expect(service.confirmarEntrada).toHaveBeenCalledWith(42);
      expect(fixture.componentInstance.fila.length).toBe(0); expect(fixture.componentInstance.successMessage).toContain('Entrada liberada'); expect(modal.classList.contains('show')).toBeFalse();
    } finally { encerrar(); }
  }));
});
