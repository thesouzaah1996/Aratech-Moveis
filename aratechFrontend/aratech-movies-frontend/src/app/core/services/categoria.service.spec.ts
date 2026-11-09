import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { CategoriaService } from './categoria.service';

describe('CategoriaService - contrato paginado', () => {
  let service: CategoriaService;
  let http: HttpTestingController;
  const url = '/aratech/almoxarifado/categorias';
  beforeEach(() => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    service = TestBed.inject(CategoriaService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('envia pagina e busca ao servidor e preserva o total da resposta', () => {
    let total = 0;
    service.getAll(6, 8, '  Ferragens  ').subscribe(pagina => total = pagina.total);
    const req = http.expectOne(r => r.url === url);
    expect(req.request.params.get('pagina')).toBe('6');
    expect(req.request.params.get('tamanho')).toBe('8');
    expect(req.request.params.get('nome')).toBe('Ferragens');
    req.flush({status: 200, categorias: [{id: 49, nome: 'Ferragens', ativo: true}], pagina: 6, tamanho: 8, total: 50});
    expect(total).toBe(50);
  });

  it('busca todas as paginas ativas para o seletor de produtos', () => {
    let resultado: any[] = [];
    service.lookup().subscribe(itens => resultado = itens);
    const primeira = http.expectOne(r => r.url === url && r.params.get('pagina') === '0');
    expect(primeira.request.params.get('ativo')).toBe('true');
    primeira.flush({status: 200, categorias: Array.from({length: 100}, (_, i) => ({id: i + 1, nome: `Categoria ${i}`, ativo: true})), pagina: 0, tamanho: 100, total: 101});
    const segunda = http.expectOne(r => r.url === url && r.params.get('pagina') === '1');
    expect(segunda.request.params.get('ativo')).toBe('true');
    segunda.flush({status: 200, categorias: [{id: 101, nome: 'Ultima', ativo: true}], pagina: 1, tamanho: 100, total: 101});
    expect(resultado.length).toBe(101);
    expect(resultado[100]).toEqual({id: 101, nome: 'Ultima'});
  });

  it('cadastra, edita e altera status usando os contratos do backend', () => {
    const categoria = {id: 1, nome: 'Ferragens', ativo: true};
    service.add('Ferragens').subscribe(c => expect(c).toEqual(categoria));
    const post = http.expectOne(url);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual({nome: 'Ferragens'});
    post.flush({categoria});
    service.update(1, 'Madeiras').subscribe();
    const put = http.expectOne(url + '/1');
    expect(put.request.method).toBe('PUT');
    put.flush({categoria});
    service.disable(1).subscribe();
    const desativar = http.expectOne(url + '/1/desativar');
    expect(desativar.request.method).toBe('PATCH');
    desativar.flush({categoria: {...categoria, ativo: false}});
    service.enable(1).subscribe();
    const ativar = http.expectOne(url + '/1/ativar');
    expect(ativar.request.method).toBe('PATCH');
    ativar.flush({categoria});
  });
});
