import { fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { PedidosCompraComponent } from './pedidos.component';
import { PedidoCompra, PedidoCompraService } from '../../../core/services/pedido-compra.service';
import { SessaoState } from '../../../core/services/sessao-state.service';

describe('Pedidos de compra persistidos', () => {
  let c: PedidosCompraComponent;
  let service: jasmine.SpyObj<PedidoCompraService>;
  const pedido: PedidoCompra = { id: 7, numero: 'PC-0007', origem: 'ALMOXARIFADO', tipo: 'ESTOQUE_MINIMO', estoqueId: 1, item: 'Parafuso', codigo: 'P1', quantidade: 3, unidade: 'un', fornecedor: null, valorUnitario: null, dataPedido: '2026-11-04', dataEntrega: null, status: 'Aguardando Aprovação', motivo: null, solicitante: 'Reposição automática', decididoNome: null, decididoEm: null, versao: 2 };
  beforeEach(() => {
    service = jasmine.createSpyObj('PedidoCompraService', ['listar', 'salvar', 'decidir', 'enviar', 'cancelar']);
    service.listar.and.returnValue(of([{ ...pedido }]));
    c = new PedidosCompraComponent(service, { snapshot: { data: {} } } as ActivatedRoute, new SessaoState());
    (c as any).reprovarModal = { show: jasmine.createSpy(), hide: jasmine.createSpy(), dispose: jasmine.createSpy() };
  });
  afterEach(() => c.ngOnDestroy());
  it('exige motivo antes de reprovar e envia somente uma decisao enquanto aguarda', () => {
    const resposta = new Subject<PedidoCompra>(); service.decidir.and.returnValue(resposta);
    c.pedidos = [{ ...pedido }]; c.reprovar(c.pedidos[0]); c.confirmarReprovacao();
    expect(service.decidir).not.toHaveBeenCalled(); expect(c.reprovarErro).toContain('motivo');
    c.motivo = 'Fornecedor indisponível'; c.confirmarReprovacao(); c.confirmarReprovacao();
    expect(service.decidir).toHaveBeenCalledOnceWith(pedido, 'reprovar', 'Fornecedor indisponível');
    expect(c.pedidos[0].status).toBe('Aguardando Aprovação');
    resposta.next({ ...pedido, status: 'Reprovado', motivo: 'Fornecedor indisponível', versao: 3 });
    expect(c.pedidos[0].status).toBe('Reprovado'); expect(c.pedidos[0].motivo).toBe('Fornecedor indisponível'); expect(c.pending).toBeFalse();
  });
  it('nao aprova localmente quando o servidor rejeita a decisao', () => {
    service.decidir.and.returnValue(throwError(() => ({ error: { mensagem: 'Pedido atualizado por outra pessoa.' } })));
    c.pedidos = [{ ...pedido }]; c.aprovar(c.pedidos[0]);
    expect(c.pedidos[0].status).toBe('Aguardando Aprovação'); expect(c.errorMessage).toContain('outra pessoa'); expect(c.pending).toBeFalse();
  });
  it('consulta o modulo de origem e atualiza a lista periodicamente', fakeAsync(() => {
    const origem = new PedidosCompraComponent(service, { snapshot: { data: { origem: 'almoxarifado' } } } as unknown as ActivatedRoute, new SessaoState());
    origem.ngOnInit(); tick(0); expect(service.listar).toHaveBeenCalledWith('almoxarifado');
    service.listar.and.returnValue(of([{ ...pedido, status: 'Reprovado', motivo: 'Justificativa de compras' }]));
    tick(15000); expect(origem.pedidos[0].motivo).toBe('Justificativa de compras'); origem.ngOnDestroy();
  }));
});
