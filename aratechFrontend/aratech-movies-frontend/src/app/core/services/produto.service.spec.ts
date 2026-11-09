import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting,HttpTestingController } from '@angular/common/http/testing';
import { ProdutoService } from './produto.service';
import { ProdutoForm } from '../models/produto.model';

describe('ProdutoService - contrato do estoque',()=>{
  let service:ProdutoService;
  let http:HttpTestingController;
  const url='/aratech/almoxarifado/produtos';
  const form:ProdutoForm={id:1,nome:' Parafuso ',sku:'prd-1',quantidade:10,categoriaID:1,fornecedorID:2,
    localArmazenamento:' A1 ',descricao:' Exemplo ',vencimentoProduto:null};
  beforeEach(()=>{
    TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting()]});
    service=TestBed.inject(ProdutoService);http=TestBed.inject(HttpTestingController);
  });
  afterEach(()=>http.verify());
  it('envia pagina e busca e preserva o total',()=>{
    service.getAll(6,8,'MDF').subscribe(r=>expect(r.total).toBe(50));
    const req=http.expectOne(r=>r.url===url);
    expect(req.request.params.get('pagina')).toBe('6');
    expect(req.request.params.get('busca')).toBe('MDF');
    req.flush({produtos:[],pagina:6,tamanho:8,total:50});
  });
  it('cadastro envia saldo inicial mas nao ID',()=>{
    service.add(form).subscribe();
    const req=http.expectOne(url);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.quantidade).toBe(10);
    expect(req.request.body.sku).toBe('PRD-1');
    expect(req.request.body.id).toBeUndefined();
    req.flush({produto:{}});
  });
  it('edicao nao envia quantidade ou ID, mesmo presentes no formulario',()=>{
    service.update(1,{...form,quantidade:999}).subscribe();
    const req=http.expectOne(url+'/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.quantidade).toBeUndefined();
    expect(req.request.body.id).toBeUndefined();
    req.flush({produto:{}});
  });
  it('entrada e saida enviam somente quantidade e chave para endpoints proprios',()=>{
    const chave=crypto.randomUUID();
    service.entrada(1,5,chave,'Conferencia de estoque').subscribe();
    const entrada=http.expectOne(url+'/1/entradas');
    expect(entrada.request.body).toEqual({quantidade:5,chaveOperacao:chave,motivo:'Conferencia de estoque'});
    entrada.flush({produto:{}});
    service.saida(1,2,chave,'Conferencia de estoque').subscribe();
    const saida=http.expectOne(url+'/1/saidas');
    expect(saida.request.body).toEqual({quantidade:2,chaveOperacao:chave,motivo:'Conferencia de estoque'});
    saida.flush({produto:{}});
  });
  it('arquiva usando o endpoint de exclusao',()=>{
    service.delete(1).subscribe();
    const req=http.expectOne(url+'/1');expect(req.request.method).toBe('DELETE');req.flush({produto:{}});
  });
});
