import { of, Subject, throwError } from 'rxjs';
import { SolicitarManutencaoComponent } from './solicitar-manutencao.component';
import { ChamadoService } from '../../../core/services/chamado.service';
import { Chamado, ChamadoForm } from '../../../core/models/chamado.model';
const form: ChamadoForm = { equipamento: ' Prensa  01 ', tipo: 'CORRETIVA', prioridade: 'ALTA', solicitante: 'Ana Silva', setor: 'Producao', telefone: '(43) 99999-1234', descricao: 'Motor parado' };
const chamado: Chamado = { ...form, id: 42, tipo: 'CORRETIVA', prioridade: 'ALTA', status: 'ABERTA', dataAbertura: '2026-11-01T10:00:00Z' };
describe('Solicitar manutencao', () => {
  let c: SolicitarManutencaoComponent; let service: jasmine.SpyObj<ChamadoService>;
  beforeEach(() => {
    service = jasmine.createSpyObj('ChamadoService', ['add']); c = new SolicitarManutencaoComponent(service);
    (c as any).resultModal = { show: jasmine.createSpy('show'), hide: jasmine.createSpy('hide'), dispose: () => {} };
    c.form = { ...form };
  });
  afterEach(() => c.ngOnDestroy());
  it('bloqueia duplo clique e apresenta o numero retornado pelo servidor', () => {
    const resposta = new Subject<Chamado>(); service.add.and.returnValue(resposta);
    c.submit(); c.submit(); expect(service.add).toHaveBeenCalledTimes(1); expect(c.salvando).toBeTrue();
    resposta.next(chamado); resposta.complete(); expect(c.salvando).toBeFalse();
    expect(c.chamadoCriado?.id).toBe(42); expect(c.resultadoSucesso).toBeTrue();
    expect((c as any).resultModal.show).toHaveBeenCalled();
    c.submit(); expect(service.add).toHaveBeenCalledTimes(1);
    c.fecharResultado(); expect(c.form.equipamento).toBe(''); expect(c.chamadoCriado).toBeNull();
  });
  it('valida antes de enviar e normaliza os campos', () => {
    c.form.tipo = ''; c.submit(); expect(service.add).not.toHaveBeenCalled(); expect(c.validacaoError).toContain('tipo');
    c.form = { ...form }; service.add.and.returnValue(of(chamado)); c.submit();
    expect(service.add).toHaveBeenCalledWith(jasmine.objectContaining({ equipamento: 'Prensa 01', telefone: '43999991234' }), jasmine.any(String));
  });
  it('falha de rede preserva dados e reutiliza a mesma chave na nova tentativa', () => {
    service.add.and.returnValue(throwError(() => ({ status: 0 })));
    c.submit(); const primeira = service.add.calls.mostRecent().args;
    expect(c.cadastroPendente).not.toBeNull(); expect(c.errorMessage).toContain('reenviados');
    c.resetForm(); expect(c.form.equipamento).toBe(form.equipamento);
    service.add.and.returnValue(of(chamado)); c.submit();
    expect(service.add.calls.mostRecent().args).toEqual(primeira);
  });
  it('erro de validacao do servidor libera a correcao do formulario', () => {
    service.add.and.returnValue(throwError(() => ({ status: 400, error: { mensagem: 'Confira o telefone.' } })));
    c.submit(); expect(c.errorMessage).toBe('Confira o telefone.'); expect(c.cadastroPendente).toBeNull(); expect(c.salvando).toBeFalse();
  });
  it('formata tanto telefone fixo quanto celular', () => {
    const input = document.createElement('input'); input.value = '4333331234'; c.applyPhoneMask({ target: input } as unknown as Event);
    expect(c.form.telefone).toBe('(43) 3333-1234'); input.value = '43999991234'; c.applyPhoneMask({ target: input } as unknown as Event);
    expect(c.form.telefone).toBe('(43) 99999-1234');
  });
});
