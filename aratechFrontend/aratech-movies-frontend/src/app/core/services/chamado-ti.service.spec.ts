import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ChamadoTiService } from './chamado-ti.service';
describe('ChamadoTiService',()=>{
 let s:ChamadoTiService,h:HttpTestingController;const url='/aratech/suporte/chamados';
 beforeEach(()=>{TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting()]});s=TestBed.inject(ChamadoTiService);h=TestBed.inject(HttpTestingController);});
 afterEach(()=>h.verify());
 it('envia dados e chave e utiliza o numero do servidor',()=>{
  const f={tipo:'Hardware' as const,prioridade:'Alta' as const,moduloAfetado:'',titulo:'Monitor parado',solicitante:'Ana Silva',setor:'Compras',contato:'',descricao:'Sem imagem',tentativasSolucao:''};
  s.abrir(f,'chave').subscribe(c=>expect(c.numero).toBe('TI-2026-0042'));
  const r=h.expectOne(url);expect(r.request.method).toBe('POST');expect(r.request.body).toEqual({...f,chaveOperacao:'chave'});r.flush({chamado:{id:42,numero:'TI-2026-0042'}});
 });
 it('consulta filtros e paginacao no servidor',()=>{s.getPagina(1,8,'Conclu\u00eddo',' Monitor ').subscribe();const r=h.expectOne(r=>r.url===url);expect(r.request.params.get('pagina')).toBe('1');expect(r.request.params.get('status')).toBe('Conclu\u00eddo');expect(r.request.params.get('busca')).toBe('Monitor');r.flush({chamados:[],total:0});});
 it('propaga falha de consulta sem simular lista vazia',()=>{let erro=false;s.getPagina().subscribe({error:()=>erro=true});h.expectOne(r=>r.url===url).flush({}, {status:500,statusText:'Erro'});expect(erro).toBeTrue();});
});
