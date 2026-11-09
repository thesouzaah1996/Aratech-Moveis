import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { PecaEstoqueService } from './peca-estoque.service';
import { PecaEstoqueForm } from '../models/peca-estoque.model';
describe('Estoque de manutencao - contrato HTTP', () => {
  let s:PecaEstoqueService;let h:HttpTestingController;const url='/aratech/manutencao/estoque';
  const form:PecaEstoqueForm={nome:'Lubrificante',codigo:'mnt-01',quantidade:1.5,unidade:'L',localizacao:'Prateleira A',descricao:''};
  beforeEach(()=>{TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting()]});s=TestBed.inject(PecaEstoqueService);h=TestBed.inject(HttpTestingController);});
  afterEach(()=>h.verify());
  it('pagina e busca sao enviadas ao servidor com total global',()=>{s.getAll(1,8,' Lubrificante ').subscribe(p=>expect(p.total).toBe(17));const r=h.expectOne(q=>q.url===url);expect(r.request.params.get('pagina')).toBe('1');expect(r.request.params.get('busca')).toBe('Lubrificante');r.flush({pecas:[],pagina:1,tamanho:8,total:17});});
  it('cadastro aceita saldo decimal e edicao nunca envia saldo ou ID',()=>{
    s.add(form).subscribe();const c=h.expectOne(url);expect(c.request.body.codigo).toBe('MNT-01');expect(c.request.body.quantidade).toBe(1.5);c.flush({peca:{id:42,...form}});
    s.update(42,{...form,quantidade:999}).subscribe();const e=h.expectOne(url+'/42');expect(e.request.method).toBe('PUT');expect(e.request.body.quantidade).toBeUndefined();expect(e.request.body.id).toBeUndefined();expect(e.request.body.unidade).toBe('L');e.flush({peca:{id:42,...form}});
  });
  it('movimenta apenas a diferenca com chave e motivo, e consulta historico',()=>{
    s.entrada(42,0.25,'chave','Recebimento').subscribe();const e=h.expectOne(url+'/42/entradas');expect(e.request.method).toBe('POST');expect(e.request.body).toEqual({quantidade:0.25,chaveOperacao:'chave',motivo:'Recebimento'});e.flush({peca:{id:42,...form}});
    s.saida(42,0.125,'outra','Reparo').subscribe();h.expectOne(url+'/42/saidas').flush({peca:{id:42,...form}});
    s.historico(42,1,8).subscribe();const r=h.expectOne(q=>q.url===url+'/42/movimentacoes');expect(r.request.params.get('pagina')).toBe('1');r.flush({movimentacoes:[],total:0,pagina:1,tamanho:8});
  });
});
