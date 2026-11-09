import { of, Subject } from 'rxjs';
import { EstoqueProdutosComponent } from './estoque-produtos.component';
import { EstoqueManutencaoComponent } from '../../manutencao/estoque/estoque.component';
import { ProdutoService } from '../../../core/services/produto.service';
import { PecaEstoqueService } from '../../../core/services/peca-estoque.service';
import { SessaoState } from '../../../core/services/sessao-state.service';
import { Produto } from '../../../core/models/produto.model';
import { PecaEstoque } from '../../../core/models/peca-estoque.model';

describe('Quantidade minima dos estoques', () => {
  function sessao(): SessaoState { const s=new SessaoState();s.usuario.set({id:1,nome:'Gestor',email:'gestor@example.test',administrador:true,perfis:[],permissoes:[],trocarSenha:false});return s; }
  const produto:Produto={id:1,nome:'Parafuso',sku:'P1',quantidade:5,categoriaID:1,categoriaNome:'Ferragens',fornecedorID:1,fornecedorNome:'Fornecedor',localArmazenamento:'A1'};
  it('confirma minimo apenas apos retorno do servidor e impede envio duplicado', () => {
    const service=jasmine.createSpyObj<ProdutoService>('ProdutoService',['minimo']);const retorno=new Subject<{quantidadeMinima:number|null}>();service.minimo.and.returnValue(retorno);
    const c=new EstoqueProdutosComponent({} as any,{} as any,service,sessao());const item={...produto};
    c.editarMinimo(item);c.minimoValor=5;c.salvarMinimo(item);c.salvarMinimo(item);
    expect(service.minimo).toHaveBeenCalledOnceWith(1,5);expect(item.quantidadeMinima).toBeUndefined();
    retorno.next({quantidadeMinima:5});expect(item.quantidadeMinima).toBe(5);expect(c.minimoId).toBeNull();c.ngOnDestroy();
  });
  it('produto exige inteiro e valor vazio desativa o monitoramento', () => {
    const service=jasmine.createSpyObj<ProdutoService>('ProdutoService',['minimo']);service.minimo.and.returnValue(of({quantidadeMinima:null}));
    const c=new EstoqueProdutosComponent({} as any,{} as any,service,sessao());const item={...produto};
    c.editarMinimo(item);c.minimoValor=1.5;c.salvarMinimo(item);expect(service.minimo).not.toHaveBeenCalled();
    c.minimoValor=null;c.salvarMinimo(item);expect(service.minimo).toHaveBeenCalledOnceWith(1,null);c.ngOnDestroy();
  });
  it('pecas de manutencao aceitam minimo decimal', () => {
    const service=jasmine.createSpyObj<PecaEstoqueService>('PecaEstoqueService',['minimo']);service.minimo.and.returnValue(of({quantidadeMinima:2.5}));
    const c=new EstoqueManutencaoComponent(service,sessao());const item:PecaEstoque={id:1,nome:'Peça',codigo:'M1',quantidade:3,unidade:'kg',localizacao:'Oficina'};
    c.editarMinimo(item);c.minimoValor=2.5;c.salvarMinimo(item);expect(service.minimo).toHaveBeenCalledOnceWith(1,2.5);expect(item.quantidadeMinima).toBe(2.5);c.ngOnDestroy();
  });
});
