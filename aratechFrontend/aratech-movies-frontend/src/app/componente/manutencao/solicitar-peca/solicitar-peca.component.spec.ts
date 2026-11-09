import { of, Subject, throwError } from 'rxjs';
import { SolicitarPecaComponent } from './solicitar-peca.component';
import { SolicitacaoPecaService } from '../../../core/services/solicitacao-peca.service';
import { SolicitacaoPeca, SolicitacaoPecaForm } from '../../../core/models/solicitacao-peca.model';
const form: SolicitacaoPecaForm = { nomePeca: 'Rolamento', codigo: '', quantidade: 1.5, unidade: 'kg', finalidade: 'MELHORIA', equipamento: ' Prensa  01 ', prioridade: 'ALTA', solicitante: 'Ana Silva', setor: 'Producao', telefone: '(43) 99999-1234', observacoes: 'Motor parado' };
const chamado: SolicitacaoPeca = { ...form, quantidade: 1.5, id: 42, finalidade: 'MELHORIA', prioridade: 'ALTA', status: 'ABERTA', criadoEm: '2026-11-01T10:00:00Z' };
describe('Solicitar peca', () => {
  let c: SolicitarPecaComponent; let service: jasmine.SpyObj<SolicitacaoPecaService>;
  beforeEach(() => {
    service = jasmine.createSpyObj('SolicitacaoPecaService', ['add']); c = new SolicitarPecaComponent(service);
    (c as any).resultModal = { show: jasmine.createSpy('show'), hide: jasmine.createSpy('hide'), dispose: () => {} };
    c.form = { ...form };
  });
  afterEach(() => c.ngOnDestroy());
  it('bloqueia duplo clique e apresenta o numero retornado pelo servidor', () => {
    const resposta = new Subject<SolicitacaoPeca>(); service.add.and.returnValue(resposta);
    c.submit(); c.submit(); expect(service.add).toHaveBeenCalledTimes(1); expect(c.salvando).toBeTrue();
    resposta.next(chamado); resposta.complete(); expect(c.salvando).toBeFalse();
    expect(c.solicitacaoCriada?.id).toBe(42); expect(c.resultadoSucesso).toBeTrue();
    expect((c as any).resultModal.show).toHaveBeenCalled();
    c.submit(); expect(service.add).toHaveBeenCalledTimes(1);
    c.fecharResultado(); expect(c.form.equipamento).toBe(''); expect(c.solicitacaoCriada).toBeNull();
  });
  it('valida antes de enviar e normaliza os campos', () => {
    c.form.finalidade = ''; c.submit(); expect(service.add).not.toHaveBeenCalled(); expect(c.validacaoError).toContain('finalidade');
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
