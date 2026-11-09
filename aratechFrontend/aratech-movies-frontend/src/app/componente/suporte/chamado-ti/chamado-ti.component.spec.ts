import { Subject, of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { ChamadoTiService } from '../../../core/services/chamado-ti.service';
import { ChamadoTi } from '../../../core/models/chamado-ti.model';
import { ChamadoTiComponent } from './chamado-ti.component';
describe('ChamadoTiComponent',()=>{
 let c:ChamadoTiComponent,s:jasmine.SpyObj<ChamadoTiService>;
 beforeEach(()=>{s=jasmine.createSpyObj('ChamadoTiService',['abrir']);c=new ChamadoTiComponent(jasmine.createSpyObj<Router>('Router',['navigate']),s);c.form={tipo:'Hardware',prioridade:'Alta',moduloAfetado:'',titulo:'Monitor parado',solicitante:'Ana Silva',setor:'Compras',contato:'1234',descricao:'Sem imagem',tentativasSolucao:''};});
 afterEach(()=>c.ngOnDestroy());
 it('impede clique duplicado e mostra somente numero confirmado',()=>{const resposta=new Subject<ChamadoTi>();s.abrir.and.returnValue(resposta);c.submit();c.submit();expect(s.abrir).toHaveBeenCalledTimes(1);expect(c.numeroChamado).toBe('');resposta.next({numero:'TI-2026-0042'} as ChamadoTi);expect(c.numeroChamado).toBe('TI-2026-0042');expect(c.resultadoSucesso).toBeTrue();c.submit();expect(s.abrir).toHaveBeenCalledTimes(1);});
 it('reenvia a mesma chave e os mesmos dados apos falha de rede',()=>{s.abrir.and.returnValue(throwError(()=>({status:0})));c.submit();const original=s.abrir.calls.mostRecent().args;c.form.titulo='Alterado';s.abrir.and.returnValue(of({numero:'TI-2026-0043'} as ChamadoTi));c.submit();expect(s.abrir.calls.mostRecent().args).toEqual(original);});
 it('libera correcao quando backend rejeita dados',()=>{s.abrir.and.returnValue(throwError(()=>({status:400,error:{mensagem:'Confira os dados'}})));c.submit();expect(c.cadastroPendente).toBeNull();expect(c.errorMessage).toContain('Confira os dados');});
 it('nao envia formulario invalido',()=>{c.form.tipo='';c.submit();expect(s.abrir).not.toHaveBeenCalled();expect(c.validacaoError).not.toBe('');});
});
