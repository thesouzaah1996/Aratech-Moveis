import { fakeAsync, tick } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { EstoqueProdutosComponent } from './estoque-produtos.component';
import { ProdutoService } from '../../../core/services/produto.service';
import { ProdutosPagina } from '../../../core/models/produto.model';

describe('EstoqueProdutosComponent - paginacao no servidor', () => {
  let service: jasmine.SpyObj<ProdutoService>;
  let component: EstoqueProdutosComponent;
  const pagina = (numero: number): ProdutosPagina => ({status: 200, pagina: numero, tamanho: 8, total: 50,
    produtos: Array.from({length: numero === 6 ? 2 : 8}, (_, i) => ({id: numero * 8 + i + 1, nome: `Produto ${numero * 8 + i + 1}`,sku:`PRD-${i}`,quantidade:10,categoriaID:1,categoriaNome:'Ferragens',fornecedorID:2,fornecedorNome:'Fornecedor Exemplo',localArmazenamento:'A1'}))});
  beforeEach(() => {
    service = jasmine.createSpyObj('ProdutoService', ['getAll','add','update','entrada','saida','delete','historico','opcoes']);
    service.opcoes.and.returnValue(of({categorias:[{id:1,nome:'Ferragens'}],fornecedores:[{id:2,nome:'Fornecedor Exemplo'}]}));
    service.getAll.and.callFake((numero = 0) => of(pagina(numero)));
    const cat=jasmine.createSpyObj('CategoriaService',['lookup']);cat.lookup.and.returnValue(of([{id:1,nome:'Ferragens'}]));
    const forn=jasmine.createSpyObj('FornecedorService',['lookup']);forn.lookup.and.returnValue(of([{id:2,nome:'Fornecedor Exemplo'}]));
    component = new EstoqueProdutosComponent(cat,forn,service);
    (component as any).entradaModal={show:()=>{},hide:()=>{},dispose:()=>{}};
    (component as any).saidaModal={show:()=>{},hide:()=>{},dispose:()=>{}};
  });
  afterEach(() => component.ngOnDestroy());

  it('carrega a primeira pagina e usa o total do servidor', () => {
    component.ngOnInit();
    expect(service.getAll).toHaveBeenCalledWith(0, 8, '');
    expect(component.produtos.length).toBe(8);
    expect(component.totalPages).toBe(7);
    expect(component.inicio).toBe(1);
    expect(component.fim).toBe(8);
  });

  it('nao fatia novamente a pagina recebida e mostra os ultimos dois de cinquenta', () => {
    component.ngOnInit();
    component.setPage(7);
    expect(service.getAll).toHaveBeenCalledWith(6, 8, '');
    expect(component.produtos.map(c => c.id)).toEqual([49, 50]);
    expect(component.inicio).toBe(49);
    expect(component.fim).toBe(50);
    const chamadas = service.getAll.calls.count();
    component.setPage(8);
    component.setPage(0);
    expect(service.getAll.calls.count()).toBe(chamadas);
  });

  it('busca no servidor e reinicia a pagina em vez de filtrar apenas os itens carregados', fakeAsync(() => {
    component.ngOnInit();
    component.setPage(7);
    component.searchQuery = 'Dobradi';
    component.onSearch();
    tick(300);
    expect(service.getAll).toHaveBeenCalledWith(0, 8, 'Dobradi');
    expect(component.page).toBe(1);
  }));

  it('cancela a requisicao anterior para nao mostrar uma pagina antiga', () => {
    const antiga = new Subject<ProdutosPagina>();
    service.getAll.and.returnValue(antiga);
    component.ngOnInit();
    service.getAll.and.returnValue(of(pagina(1)));
    component.page = 2;
    component.carregar();
    antiga.next(pagina(0));
    expect(component.page).toBe(2);
    expect(component.produtos[0].id).toBe(9);
  });

  it('ajusta pagina quando o total diminui apos uma alteracao', () => {
    component.page = 7;
    service.getAll.and.returnValues(of({...pagina(6), produtos: [], total: 48}), of({...pagina(5), total: 48}));
    component.carregar();
    expect(service.getAll).toHaveBeenCalledWith(5, 8, '');
    expect(component.page).toBe(6);
    expect(component.fim).toBe(48);
  });

  it('nao envia cadastro vazio', () => {
    component.form.nome = ' ';
    component.save();
    expect(service.add).not.toHaveBeenCalled();
    expect(component.modalError).toBeTruthy();
  });

  it('rejeita movimentacoes fracionarias, negativas ou nulas sem chamar API',()=>{
    component.openEntrada(pagina(0).produtos[0]);
    for(const quantidade of [null,0,-1,0.5,1000000001]) {
      component.quantidadeMovimentacao=quantidade;
      component.confirmEntrada();
    }
    expect(service.entrada).not.toHaveBeenCalled();
    expect(component.movimentacaoError).toBeTruthy();
  });

  it('repete a mesma chave e quantidade em falha de rede sem recalcular o saldo',()=>{
    service.entrada.and.returnValues(throwError(()=>({status:0})),of(pagina(0).produtos[0]));
    component.openEntrada(pagina(0).produtos[0]);
    component.quantidadeMovimentacao=2;
    component.motivoMovimentacao='Conferencia de estoque';
    component.confirmEntrada();
    const primeira=service.entrada.calls.first().args;
    component.quantidadeMovimentacao=99;
    component.confirmEntrada();
    const segunda=service.entrada.calls.mostRecent().args;
    expect(primeira).toEqual(segunda);
    expect(segunda[1]).toBe(2);
    expect(segunda[2]).toBeTruthy();
    expect(service.update).not.toHaveBeenCalled();
  });

  it('saida usa quantidade solicitada e trata conflito informado pelo servidor',()=>{
    service.saida.and.returnValue(throwError(()=>({status:409,error:{mensagem:'Sem estoque'}})));
    component.openSaida(pagina(0).produtos[0]);
    component.quantidadeMovimentacao=15;
    component.motivoMovimentacao='Conferencia de estoque';
    component.confirmSaida();
    expect(service.saida).toHaveBeenCalledWith(1,15,jasmine.any(String),'Conferencia de estoque');
    expect(component.movimentacaoError).toBe('Sem estoque');
    expect(service.update).not.toHaveBeenCalled();
  });

  it('exige motivo antes de enviar uma movimentacao',()=>{
    component.openEntrada(pagina(0).produtos[0]);component.quantidadeMovimentacao=2;
    component.confirmEntrada();expect(service.entrada).not.toHaveBeenCalled();
    expect(component.movimentacaoError).toContain('motivo');
  });

  it('carrega o historico por produto e pagina sem misturar os registros',()=>{
    (component as any).historicoModal={show:()=>{},dispose:()=>{}};
    service.historico.and.returnValue(of({status:200,movimentacoes:[],pagina:0,tamanho:8,total:10}));
    component.openHistorico(pagina(0).produtos[0]);
    expect(service.historico).toHaveBeenCalledWith(1,0,8);
    expect(component.historicoPages).toBe(2);
    component.paginaHistorico(2);
    expect(service.historico).toHaveBeenCalledWith(1,1,8);
  });
});
