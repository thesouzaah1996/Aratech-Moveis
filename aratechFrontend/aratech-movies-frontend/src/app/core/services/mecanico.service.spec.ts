import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { MecanicoService } from './mecanico.service';
describe('MecanicoService', () => {
  let s: MecanicoService;let h: HttpTestingController;const url='/aratech/manutencao/mecanicos';
  beforeEach(() => { TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting()]});s=TestBed.inject(MecanicoService);h=TestBed.inject(HttpTestingController); });
  afterEach(() => h.verify());
  it('seletor percorre todas as paginas e pede somente ativos', () => {
    let ids:number[]=[];s.lookup().subscribe(itens => ids=itens.map(i=>i.id));
    const a=h.expectOne(r=>r.url===url&&r.params.get('pagina')==='0');expect(a.request.params.get('ativo')).toBe('true');a.flush({mecanicos:[{id:7,nome:'Ana',ativo:true}],pagina:0,tamanho:100,total:101});
    h.expectOne(r=>r.url===url&&r.params.get('pagina')==='1'&&r.params.get('ativo')==='true').flush({mecanicos:[{id:8,nome:'Carlos',ativo:true}],pagina:1,tamanho:100,total:101});expect(ids).toEqual([7,8]);
  });
  it('cadastra e altera status pelos endpoints reais', () => {
    s.add({nome:'Ana Silva',especialidades:['PRENSA'],turno:'MANHA'}).subscribe(m=>expect(m.id).toBe(7));const r=h.expectOne(url);expect(r.request.method).toBe('POST');r.flush({mecanico:{id:7}});
    s.disable(7).subscribe();const d=h.expectOne(url+'/7/desativar');expect(d.request.method).toBe('PATCH');d.flush({status:200});
  });
});
