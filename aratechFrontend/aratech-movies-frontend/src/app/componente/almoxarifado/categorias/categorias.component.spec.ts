import { fakeAsync, tick } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { CategoriasComponent } from './categorias.component';
import { CategoriaService } from '../../../core/services/categoria.service';
import { CategoriasPagina } from '../../../core/models/categoria.model';

describe('CategoriasComponent - paginacao no servidor', () => {
  let service: jasmine.SpyObj<CategoriaService>;
  let component: CategoriasComponent;
  const pagina = (numero: number): CategoriasPagina => ({status: 200, pagina: numero, tamanho: 8, total: 50,
    categorias: Array.from({length: numero === 6 ? 2 : 8}, (_, i) => ({id: numero * 8 + i + 1, nome: `Categoria ${numero * 8 + i + 1}`, ativo: true}))});
  beforeEach(() => {
    service = jasmine.createSpyObj('CategoriaService', ['getAll', 'add', 'update', 'enable', 'disable']);
    service.getAll.and.callFake((numero = 0) => of(pagina(numero)));
    component = new CategoriasComponent(service);
  });
  afterEach(() => component.ngOnDestroy());

  it('carrega a primeira pagina e usa o total do servidor', () => {
    component.ngOnInit();
    expect(service.getAll).toHaveBeenCalledWith(0, 8, '');
    expect(component.categorias.length).toBe(8);
    expect(component.totalPages).toBe(7);
    expect(component.inicio).toBe(1);
    expect(component.fim).toBe(8);
  });

  it('nao fatia novamente a pagina recebida e mostra os ultimos dois de cinquenta', () => {
    component.ngOnInit();
    component.setPage(7);
    expect(service.getAll).toHaveBeenCalledWith(6, 8, '');
    expect(component.categorias.map(c => c.id)).toEqual([49, 50]);
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
    const antiga = new Subject<CategoriasPagina>();
    service.getAll.and.returnValue(antiga);
    component.ngOnInit();
    service.getAll.and.returnValue(of(pagina(1)));
    component.page = 2;
    component.carregar();
    antiga.next(pagina(0));
    expect(component.page).toBe(2);
    expect(component.categorias[0].id).toBe(9);
  });

  it('ajusta pagina quando o total diminui apos uma alteracao', () => {
    component.page = 7;
    service.getAll.and.returnValues(of({...pagina(6), categorias: [], total: 48}), of({...pagina(5), total: 48}));
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
});
