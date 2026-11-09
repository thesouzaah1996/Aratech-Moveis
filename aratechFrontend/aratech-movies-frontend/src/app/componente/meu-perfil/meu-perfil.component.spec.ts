import { of, throwError } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { MeuPerfilComponent } from './meu-perfil.component';
import { SessaoState } from '../../core/services/sessao-state.service';

describe('Meu perfil', () => {
  it('carrega o perfil da sessao sem solicitar lista de funcionarios', () => {
    const http = jasmine.createSpyObj<HttpClient>('HttpClient', ['get']);
    http.get.and.returnValue(of({ nome: 'Ana', email: 'ana@empresa.test', matricula: '7', cargo: 'Analista', telefone: '1133334444', dataAdmissao: '2025-01-10', perfil: 'Operador', acessoAtual: '2026-11-04T12:00:00Z' }));
    const c = new MeuPerfilComponent(new SessaoState(), http); c.ngOnInit();
    expect(http.get).toHaveBeenCalledOnceWith('/aratech/auth/meu-perfil');
    expect(c.dados.cargo).toBe('Analista'); expect(c.dados.telefone).toBe('1133334444'); expect(c.acessoAtual).toContain('2026-11-04');
    c.ngOnDestroy();
  });
  it('mostra falha de carregamento sem inventar dados', () => {
    const http = jasmine.createSpyObj<HttpClient>('HttpClient', ['get']);
    http.get.and.returnValue(throwError(() => ({ error: { mensagem: 'Falha ao consultar perfil.' } })));
    const c = new MeuPerfilComponent(new SessaoState(), http); c.ngOnInit();
    expect(c.erro).toBe('Falha ao consultar perfil.'); expect(c.loading).toBeFalse(); expect(c.dados.cargo).toBe(''); c.ngOnDestroy();
  });
});
