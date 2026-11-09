import { fakeAsync, tick } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { FornecedoresComponent } from './fornecedores.component';
import { FornecedorService } from '../../../core/services/fornecedor.service';
import { FornecedoresPagina } from '../../../core/models/fornecedor.model';

describe('FornecedoresComponent - paginacao no servidor', () => {
  let service: jasmine.SpyObj<FornecedorService>;
  let component: FornecedoresComponent;
  const pagina = (numero: number): FornecedoresPagina => ({status: 200, pagina: numero, tamanho: 8, total: 50,
    fornecedores: Array.from({length: numero === 6 ? 2 : 8}, (_, i) => ({id: numero * 8 + i + 1, nome: `Fornecedor ${numero * 8 + i + 1}`, email: 'contato@example.com', telefone: '4330000000', representante: {nomeRepresentante: 'Maria Exemplo', telefoneRepresentante: '43990000000', emailRepresentante: 'maria@example.com'}, ativo: true}))});
  beforeEach(() => {
    service = jasmine.createSpyObj('FornecedorService', ['getAll', 'add', 'update', 'enable', 'disable']);
    service.getAll.and.callFake((numero = 0) => of(pagina(numero)));
    component = new FornecedoresComponent(service);
  });
  afterEach(() => component.ngOnDestroy());

  it('carrega a primeira pagina e usa o total do servidor', () => {
    component.ngOnInit();
    expect(service.getAll).toHaveBeenCalledWith(0, 8, '', undefined);
    expect(component.fornecedores.length).toBe(8);
    expect(component.totalPages).toBe(7);
    expect(component.paginationStart).toBe(1);
    expect(component.paginationEnd).toBe(8);
  });

  it('nao fatia novamente a pagina recebida e mostra os ultimos dois de cinquenta', () => {
    component.ngOnInit();
    component.setPage(7);
    expect(service.getAll).toHaveBeenCalledWith(6, 8, '', undefined);
    expect(component.fornecedores.map(c => c.id)).toEqual([49, 50]);
    expect(component.paginationStart).toBe(49);
    expect(component.paginationEnd).toBe(50);
    const chamadas = service.getAll.calls.count();
    component.setPage(8);
    component.setPage(0);
    expect(service.getAll.calls.count()).toBe(chamadas);
  });

  it('busca no servidor e reinicia a pagina em vez de filtrar apenas os itens carregados', fakeAsync(() => {
    component.ngOnInit();
    component.setPage(7);
    component.searchTerm = 'Dobradi';
    component.onSearch();
    tick(300);
    expect(service.getAll).toHaveBeenCalledWith(0, 8, 'Dobradi', undefined);
    expect(component.page).toBe(1);
  }));

  it('cancela a requisicao anterior para nao mostrar uma pagina antiga', () => {
    const antiga = new Subject<FornecedoresPagina>();
    service.getAll.and.returnValue(antiga);
    component.ngOnInit();
    service.getAll.and.returnValue(of(pagina(1)));
    component.page = 2;
    component.loadFornecedores();
    antiga.next(pagina(0));
    expect(component.page).toBe(2);
    expect(component.fornecedores[0].id).toBe(9);
  });

  it('ajusta pagina quando o total diminui apos uma alteracao', () => {
    component.page = 7;
    service.getAll.and.returnValues(of({...pagina(6), fornecedores: [], total: 48}), of({...pagina(5), total: 48}));
    component.loadFornecedores();
    expect(service.getAll).toHaveBeenCalledWith(5, 8, '', undefined);
    expect(component.page).toBe(6);
    expect(component.paginationEnd).toBe(48);
  });

  it('nao envia cadastro vazio', () => {
    component.form.nome = ' ';
    component.save();
    expect(service.add).not.toHaveBeenCalled();
    expect(component.modalError).toBeTruthy();
  });

  it('filtra o status no servidor antes de paginar', () => {
    component.ngOnInit();
    component.setPage(7);
    component.setFiltroStatus('inativos');
    expect(service.getAll).toHaveBeenCalledWith(0,8,'',false);
    expect(component.page).toBe(1);
  });

  it('nao envia fornecedor sem dados do representante', () => {
    component.form = {nome: 'Fornecedor Exemplo', email: 'contato@example.com', telefone: '4330000000',
      representante: {nomeRepresentante: '',telefoneRepresentante: '',emailRepresentante: ''}};
    component.save();
    expect(service.add).not.toHaveBeenCalled();
    expect(component.modalError).toBeTruthy();
  });
});
