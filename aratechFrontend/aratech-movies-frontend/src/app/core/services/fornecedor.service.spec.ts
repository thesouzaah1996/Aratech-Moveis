import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { FornecedorService } from './fornecedor.service';

describe('FornecedorService - contrato paginado', () => {
  let service: FornecedorService;
  let http: HttpTestingController;
  const url = '/aratech/almoxarifado/fornecedores';
  beforeEach(() => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    service = TestBed.inject(FornecedorService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('envia pagina e busca ao servidor e preserva o total da resposta', () => {
    let total = 0;
    service.getAll(6, 8, '  Ferragens  ').subscribe(pagina => total = pagina.total);
    const req = http.expectOne(r => r.url === url);
    expect(req.request.params.get('pagina')).toBe('6');
    expect(req.request.params.get('tamanho')).toBe('8');
    expect(req.request.params.get('busca')).toBe('Ferragens');
    req.flush({status: 200, fornecedores: [{id: 49, nome: 'Ferragens', ativo: true}], pagina: 6, tamanho: 8, total: 50});
    expect(total).toBe(50);
  });

  it('busca todas as paginas ativas para o seletor de produtos', () => {
    let resultado: any[] = [];
    service.lookup().subscribe(itens => resultado = itens);
    const primeira = http.expectOne(r => r.url === url && r.params.get('pagina') === '0');
    expect(primeira.request.params.get('ativo')).toBe('true');
    primeira.flush({status: 200, fornecedores: Array.from({length: 100}, (_, i) => ({id: i + 1, nome: `Fornecedor ${i}`, ativo: true})), pagina: 0, tamanho: 100, total: 101});
    const segunda = http.expectOne(r => r.url === url && r.params.get('pagina') === '1');
    expect(segunda.request.params.get('ativo')).toBe('true');
    segunda.flush({status: 200, fornecedores: [{id: 101, nome: 'Ultima', ativo: true}], pagina: 1, tamanho: 100, total: 101});
    expect(resultado.length).toBe(101);
    expect(resultado[100]).toEqual({id: 101, nome: 'Ultima'});
  });

  it('cadastra, edita e altera status usando os contratos do backend', () => {
    const fornecedor = {id: 1, nome: 'Ferragens', ativo: true, email: 'contato@example.com', telefone: '4330000000', representante: {nomeRepresentante: 'Maria Exemplo', telefoneRepresentante: '43990000000', emailRepresentante: 'maria@example.com'}};
    const form = {nome: 'Ferragens', email: 'contato@example.com', telefone: '(43) 3000-0000', representante: {nomeRepresentante: 'Maria Exemplo', telefoneRepresentante: '(43) 99000-0000', emailRepresentante: 'MARIA@EXAMPLE.COM'}};
    service.add(form).subscribe(c => expect(c).toEqual(fornecedor));
    const post = http.expectOne(url);
    expect(post.request.method).toBe('POST');
    expect(post.request.body.nome).toBe('Ferragens');
    expect(post.request.body.telefone).toBe('4330000000');
    expect(post.request.body.representante.emailRepresentante).toBe('maria@example.com');
    expect(post.request.body.id).toBeUndefined();
    expect(post.request.body.ativo).toBeUndefined();
    post.flush({fornecedor});
    service.update(1, {...form, nome: 'Madeiras'}).subscribe();
    const put = http.expectOne(url + '/1');
    expect(put.request.method).toBe('PUT');
    put.flush({fornecedor});
    service.disable(1).subscribe();
    const desativar = http.expectOne(url + '/1/desativar');
    expect(desativar.request.method).toBe('PATCH');
    desativar.flush({fornecedor: {...fornecedor, ativo: false}});
    service.enable(1).subscribe();
    const ativar = http.expectOne(url + '/1/ativar');
    expect(ativar.request.method).toBe('PATCH');
    ativar.flush({fornecedor});
  });
});
