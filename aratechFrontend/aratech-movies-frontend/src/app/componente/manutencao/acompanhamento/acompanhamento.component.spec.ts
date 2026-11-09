import { fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ChamadoService } from '../../../core/services/chamado.service';
import { Chamado } from '../../../core/models/chamado.model';
import { AcompanhamentoComponent } from './acompanhamento.component';
describe('Acompanhamento da manutenção', () => {
  const ordem: Chamado = { id: 1, equipamento: 'Serra', tipo: 'CORRETIVA', prioridade: 'ALTA', solicitante: 'Maria', setor: 'Produção', descricao: 'Falha', status: 'ABERTA', dataAbertura: '2026-11-05T10:00:00' };
  let service: jasmine.SpyObj<ChamadoService>;
  let component: AcompanhamentoComponent;
  beforeEach(() => {
    service = jasmine.createSpyObj<ChamadoService>('ChamadoService', ['getAll']);
    service.getAll.and.returnValue(of([ordem]));
    component = new AcompanhamentoComponent(service);
  });
  afterEach(() => component.ngOnDestroy());
  it('carrega, atualiza periodicamente e encerra ao sair', fakeAsync(() => {
    component.ngOnInit();
    expect(component.ordens).toEqual([ordem]);
    tick(5000);
    expect(service.getAll).toHaveBeenCalledTimes(2);
    component.ngOnDestroy();
    tick(5000);
    expect(service.getAll).toHaveBeenCalledTimes(2);
  }));
  it('preserva dados na falha e permite tentar novamente', () => {
    component.carregar();
    service.getAll.and.returnValue(throwError(() => ({ error: { mensagem: 'Falha de conexão' } })));
    component.carregar();
    expect(component.ordens).toEqual([ordem]);
    expect(component.errorMessage).toBe('Falha de conexão');
    expect(component.loading).toBeFalse();
    service.getAll.and.returnValue(of([]));
    component.carregar();
    expect(component.errorMessage).toBe('');
    expect(component.ordens).toEqual([]);
  });
  it('filtra por busca e status e corrige a página após atualização', () => {
    component.carregar();
    component.searchTerm = 'maria';
    expect(component.paged).toEqual([ordem]);
    component.setFiltro('CONCLUIDA');
    expect(component.paged).toEqual([]);
    component.setFiltro('Todos');
    component.page = 3;
    component.carregar();
    expect(component.page).toBe(1);
  });
});
