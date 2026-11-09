import { fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ChamadoTiService } from '../../../core/services/chamado-ti.service';
import { ChamadoTi } from '../../../core/models/chamado-ti.model';
import { AcompanhamentoTiComponent } from './acompanhamento-ti.component';
describe('AcompanhamentoTiComponent',()=>{
 let c:AcompanhamentoTiComponent,s:jasmine.SpyObj<ChamadoTiService>;
 beforeEach(()=>{s=jasmine.createSpyObj('ChamadoTiService',['getPagina']);s.getPagina.and.returnValue(of({status:200,chamados:[{numero:'TI-2026-0042'} as ChamadoTi],pagina:0,tamanho:8,total:20}));c=new AcompanhamentoTiComponent(s);});
 afterEach(()=>c.ngOnDestroy());
 it('usa total e pagina do servidor sem paginar os dados duas vezes',()=>{c.carregar();c.setPage(2);expect(s.getPagina).toHaveBeenCalledWith(1,8,undefined,'');expect(c.totalPages).toBe(3);expect(c.paged.length).toBe(1);expect(c.paginationStart).toBe(9);});
 it('consulta filtro preservando os nomes',()=>{c.setFiltro('Em Atendimento');expect(s.getPagina).toHaveBeenCalledWith(0,8,'Em Atendimento','');});
 it('preserva dados quando atualizacao falha',()=>{c.carregar();s.getPagina.and.returnValue(throwError(()=>({status:500})));c.carregar();expect(c.paged.length).toBe(1);expect(c.errorMessage).not.toBe('');expect(c.loading).toBeFalse();});
 it('busca com debounce e atualiza automaticamente ate sair',fakeAsync(()=>{c.ngOnInit();c.searchTerm='Monitor';c.onSearch();tick(299);expect(s.getPagina).toHaveBeenCalledTimes(1);tick(1);expect(s.getPagina).toHaveBeenCalledWith(0,8,undefined,'Monitor');tick(4700);expect(s.getPagina).toHaveBeenCalledTimes(3);c.ngOnDestroy();tick(5000);expect(s.getPagina).toHaveBeenCalledTimes(3);}));
});
