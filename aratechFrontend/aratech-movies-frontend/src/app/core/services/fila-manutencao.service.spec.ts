import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { FilaManutencaoService } from './fila-manutencao.service';
describe('FilaManutencaoService', () => {
  let s: FilaManutencaoService; let h: HttpTestingController;
  const url='/aratech/manutencao/fila';
  beforeEach(() => { TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] }); s=TestBed.inject(FilaManutencaoService); h=TestBed.inject(HttpTestingController); });
  afterEach(() => h.verify());
  it('envia paginacao, status e busca ao servidor', () => {
    s.getPagina(2,8,'ABERTA',' Rolamento ').subscribe(p => expect(p.total).toBe(17));
    const r=h.expectOne(q => q.url===url);expect(r.request.params.get('pagina')).toBe('2');expect(r.request.params.get('status')).toBe('ABERTA');expect(r.request.params.get('busca')).toBe('Rolamento');
    r.flush({ status:200,itens:[],pagina:2,tamanho:8,total:17 });
  });
  it('detalha, atribui e conclui pelo mesmo ID independente da origem', () => {
    s.getById(42).subscribe(c => expect(c.origem).toBe('PECA'));h.expectOne(url+'/42').flush({ item:{id:42,origem:'PECA'} });
    s.atribuirMecanico(42,7).subscribe();const r=h.expectOne(url+'/42/mecanico');expect(r.request.method).toBe('POST');expect(r.request.body).toEqual({mecanicoId:7});r.flush({item:{id:42}});
    s.concluir(42).subscribe();const fim=h.expectOne(url+'/42/concluir');expect(fim.request.method).toBe('POST');fim.flush({item:{id:42}});
  });
});
