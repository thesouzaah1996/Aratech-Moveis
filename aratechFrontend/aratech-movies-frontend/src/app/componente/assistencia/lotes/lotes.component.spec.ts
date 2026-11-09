import { of, Subject } from 'rxjs';
import { AssistenciaLotesComponent } from './lotes.component';
import { CarregamentoAssistenciaComponent } from '../../carregamento/assistencia/assistencia.component';
import { LoteAssistencia, LoteAssistenciaService } from '../../../core/services/lote-assistencia.service';

describe('Fluxo de lotes entre assistência e carregamento', () => {
  let service: jasmine.SpyObj<LoteAssistenciaService>;
  const lote = (id: number, status: LoteAssistencia['status'], dataEntrada = '2026-11-03'): LoteAssistencia =>
    ({ id, codigo: `L-${id}`, produto: 'Portas', empresa: 'Cliente', quantidade: 2, dataEntrada, responsavel: 'Ana', defeito: 'Embalar', status });
  beforeEach(() => { service = jasmine.createSpyObj('LoteAssistenciaService', ['listar', 'avancar']); });
  it('assistência prioriza o mais antigo, inclusive antes de paginar', () => {
    const c = new AssistenciaLotesComponent(service);
    c.lotes = [lote(3, 'Não iniciado'), lote(1, 'Em andamento', '2026-11-01'), lote(2, 'Finalizado', '2026-11-02')];
    c.pageSize = 2;
    expect(c.paged.map(l => l.id)).toEqual([1, 2]);
  });
  it('carregamento prioriza finalizados, em andamento e não iniciados', () => {
    const c = new CarregamentoAssistenciaComponent(service);
    c.lotes = [lote(1, 'Não iniciado'), lote(2, 'Em andamento'), lote(3, 'Finalizado'), { ...lote(4, 'Finalizado'), carregadoEm: '2026-11-03T12:00:00Z' }];
    expect(c.itens.map(l => l.id)).toEqual([3, 2, 1]);
    c.finalizarCarregamento(c.lotes[0]);
    expect(service.avancar).not.toHaveBeenCalled();
    service.avancar.and.returnValue(of({ ...c.lotes[2], carregadoEm: '2026-11-03T12:00:00Z' }));
    c.finalizarCarregamento(c.lotes[2]);
    expect(c.itens.map(l => l.id)).toEqual([2, 1]);
    expect(c.lotes.find(l => l.id === 3)?.carregadoEm).toBeTruthy();
  });
  it('impede cliques duplicados ao iniciar', () => {
    const c = new AssistenciaLotesComponent(service);
    const resposta = new Subject<LoteAssistencia>();
    service.avancar.and.returnValue(resposta);
    c.lotes = [lote(1, 'Não iniciado')];
    c.avancar(c.lotes[0]); c.avancar(c.lotes[0]);
    expect(service.avancar).toHaveBeenCalledOnceWith(1, 'iniciar');
    resposta.error(new Error('Falha'));
    expect(c.processando).toBeFalse();
    expect(c.lotes[0].status).toBe('Não iniciado');
  });
});
