import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
export type StatusPedido = 'Rascunho' | 'Aguardando Aprovação' | 'Aprovado' | 'Reprovado' | 'Recebido' | 'Cancelado';
export interface PedidoCompra {
  id: number; numero: string; origem: string; tipo: string; estoqueId?: number; solicitacaoId?: number;
  fornecedor: string | null; item: string; codigo: string | null; quantidade: number; unidade: string;
  valorUnitario: number | null; dataPedido: string; dataEntrega: string | null; dataVencimento?:string|null; status: StatusPedido;
  motivo: string | null; solicitante: string | null; decididoNome: string | null; decididoEm: string | null; versao: number;
}
@Injectable({ providedIn: 'root' })
export class PedidoCompraService {
  constructor(private http: HttpClient) {}
  listar(origem?: 'almoxarifado' | 'manutencao') { return this.http.get<PedidoCompra[]>(origem ? `/aratech/${origem}/pedidos` : '/aratech/compras/pedidos'); }
  salvar(form: Partial<PedidoCompra>, id?: number) {
    const corpo = { fornecedor: form.fornecedor, item: form.item, quantidade: form.quantidade, valorUnitario: form.valorUnitario, dataPedido: form.dataPedido, dataEntrega: form.dataEntrega, dataVencimento:form.dataVencimento, versao: form.versao ?? 0 };
    return id ? this.http.put<PedidoCompra>(`/aratech/compras/pedidos/${id}`, corpo) : this.http.post<PedidoCompra>('/aratech/compras/pedidos', corpo);
  }
  enviar(p: PedidoCompra) { return this.http.patch<PedidoCompra>(`/aratech/compras/pedidos/${p.id}/enviar`, { versao: p.versao }); }
  cancelar(p: PedidoCompra) { return this.http.delete<PedidoCompra>(`/aratech/compras/pedidos/${p.id}`, { params: { versao: p.versao } }); }
  decidir(p: PedidoCompra, acao: 'aprovar' | 'reprovar' | 'receber', motivo = '') { return this.http.post<PedidoCompra>(`/aratech/compras/pedidos/${p.id}/${acao}`, { motivo, versao: p.versao }); }
}
