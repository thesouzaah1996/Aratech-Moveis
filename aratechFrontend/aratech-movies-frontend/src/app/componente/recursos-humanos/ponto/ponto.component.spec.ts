import { fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { PontoComponent } from './ponto.component';
import { PontoService, DiaPonto } from '../../../core/services/ponto.service';
import { FuncionarioService } from '../../../core/services/funcionario.service';
import { SessaoState } from '../../../core/services/sessao-state.service';

describe('Controle de ponto', () => {
  let c: PontoComponent;
  let ponto: jasmine.SpyObj<PontoService>;
  let fs: jasmine.SpyObj<FuncionarioService>;
  let sessao: SessaoState;
  const dia: DiaPonto = { data: '2025-01-10', horarios: ['08:00', '12:00', '13:00', '17:00', '', ''], versao: 1, ajustado: true };
  beforeEach(() => {
    ponto = jasmine.createSpyObj('PontoService', ['listar', 'funcionario', 'historico', 'salvar','funcionarios']);
    fs = jasmine.createSpyObj('FuncionarioService', ['getAll']);
    fs.getAll.and.returnValue(of([{ id: 7, nome: 'Ana Silva', email: 'ana@example.test', cargo: 'Analista', setor: 'RH', ativo: true, perfis: [] }]));
    ponto.funcionarios.and.returnValue(of([{id:7,nome:'Ana Silva',cargo:'Analista',setor:'RH'}]));
    ponto.historico.and.returnValue(of([]));
    sessao = new SessaoState();
    sessao.usuario.set({ id: 7, nome: 'Ana', email: 'ana@example.test', administrador: false, perfis: ['#'], permissoes: ['RH.CONSULTAR', 'RH.ALTERAR'], trocarSenha: false });
    c = new PontoComponent(fs, ponto, { paramMap: of(convertToParamMap({})), snapshot: { routeConfig: { path: 'recursos-humanos/ponto' } } } as unknown as ActivatedRoute, sessao);
    (c as any).modal = { show: jasmine.createSpy(), hide: jasmine.createSpy(), dispose: jasmine.createSpy() };
  });
  afterEach(() => c.ngOnDestroy());
  it('mostra resultados somente ao digitar nome ou ID', fakeAsync(() => {
    c.ngOnInit(); expect(c.resultados).toEqual([]);
    c.buscaNome = 'Ana'; c.buscar(); tick(300); expect(c.resultados[0].id).toBe(7);
    c.buscaNome = '7'; c.buscar(); tick(300); expect(c.resultados.length).toBe(1);
    c.buscaNome = ''; c.buscar(); tick(300); expect(c.resultados).toEqual([]);
  }));
  it('soma somente pares completos e identifica marcacao incompleta', () => {
    expect(c.horas(c.total(dia))).toBe('08:00');
    const incompleto = { ...dia, horarios: ['08:00', '12:00', '13:00', '', '', ''] };
    expect(c.horas(c.total(incompleto))).toBe('04:00'); expect(c.incompleto(incompleto)).toBeTrue();
  });
  it('edita uma copia e exige motivo antes de enviar', () => {
    c.abrir(dia); c.selecionado!.horarios[0] = '08:15'; c.salvar();
    expect(dia.horarios[0]).toBe('08:00'); expect(ponto.salvar).not.toHaveBeenCalled();
    expect(c.erroModal).toContain('motivo');
  });
  it('impede envio duplicado e preserva formulario quando ha conflito', () => {
    const resposta = new Subject<DiaPonto>(); ponto.salvar.and.returnValue(resposta);
    c.id = 7; c.abrir(dia); c.motivo = 'Correção solicitada'; c.salvar(); c.salvar();
    expect(ponto.salvar).toHaveBeenCalledTimes(1);
    resposta.error({ error: { mensagem: 'Este dia foi alterado por outra pessoa.' } });
    expect(c.salvando).toBeFalse(); expect(c.selecionado).not.toBeNull(); expect(c.erroModal).toContain('outra pessoa');
  });
  it('nao permite gravar sem permissao de alterar', () => {
    sessao.usuario.update(u => ({ ...u!, permissoes: ['RH.CONSULTAR'] }));
    c.abrir(dia); c.motivo = 'Correção solicitada'; c.salvar();
    expect(ponto.salvar).not.toHaveBeenCalled();
  });
});
