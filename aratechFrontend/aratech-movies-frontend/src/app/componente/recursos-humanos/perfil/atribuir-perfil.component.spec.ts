import { fakeAsync, tick } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { AtribuirPerfilComponent } from './atribuir-perfil.component';
import { PerfilService } from '../../../core/services/perfil.service';
import { UsuarioPerfil } from '../../../core/models/perfil.model';

describe('Atribuir perfil ao usuário', () => {
  let c: AtribuirPerfilComponent;
  let service: jasmine.SpyObj<PerfilService>;
  const usuario: UsuarioPerfil = { id: 7, nome: 'Ana Silva', email: 'ana@example.test', setor: 'RH', perfis: [] };
  beforeEach(() => {
    service = jasmine.createSpyObj('PerfilService', ['buscarUsuarios', 'atribuir', 'criarAcesso']);
    service.buscarUsuarios.and.returnValue(of({ content: [usuario], number: 0, size: 8, totalElements: 1, totalPages: 1 }));
    c = new AtribuirPerfilComponent(service);
    (c as any).modal = { show: jasmine.createSpy(), hide: jasmine.createSpy(), dispose: jasmine.createSpy() };
  });
  afterEach(() => c.ngOnDestroy());
  it('busca por nome com atraso e limpa resultados ao apagar a busca', fakeAsync(() => {
    c.ngOnInit(); c.searchQuery = 'Ana'; c.onSearch();
    tick(299); expect(service.buscarUsuarios).not.toHaveBeenCalled();
    tick(1); expect(service.buscarUsuarios).toHaveBeenCalledOnceWith('Ana', 0, 8);
    expect(c.usuarios[0].nome).toBe('Ana Silva');
    c.searchQuery = ''; c.onSearch(); tick(300);
    expect(c.usuarios).toEqual([]); expect(c.pesquisou).toBeFalse();
  }));
  it('cancela resposta antiga quando o usuário muda a busca', fakeAsync(() => {
    const antiga = new Subject<any>(); service.buscarUsuarios.and.returnValue(antiga);
    c.ngOnInit(); c.searchQuery = 'Ana'; c.onSearch(); tick(300);
    c.searchQuery = 'Maria'; c.onSearch();
    antiga.next({ content: [usuario], totalElements: 1 });
    expect(c.usuarios).toEqual([]); tick(300);
  }));
  it('confirma o perfil para o usuário selecionado e atualiza a linha', () => {
    c.usuarios = [usuario]; c.abrirAtribuicao(usuario); c.nomePerfil = ' Operador ';
    service.atribuir.and.returnValue(of({ ...usuario, perfis: [{ id: 2, nome: 'OPERADOR' }] }));
    c.confirmar();
    expect(service.atribuir).toHaveBeenCalledOnceWith(7, 'Operador');
    expect(c.usuarios[0].perfis[0].nome).toBe('OPERADOR');
    expect((c as any).modal.hide).toHaveBeenCalled();
  });
  it('impede confirmação vazia e cliques duplicados', () => {
    c.abrirAtribuicao(usuario); c.confirmar();
    expect(service.atribuir).not.toHaveBeenCalled();
    service.atribuir.and.returnValue(new Subject<UsuarioPerfil>());
    c.nomePerfil = 'Operador'; c.confirmar(); c.confirmar();
    expect(service.atribuir).toHaveBeenCalledTimes(1);
  });
  it('mantém o modal aberto e mostra o erro de perfil inexistente', () => {
    c.abrirAtribuicao(usuario); c.nomePerfil = 'Inexistente';
    service.atribuir.and.returnValue(throwError(() => ({ error: { mensagem: 'Perfil não encontrado.' } })));
    c.confirmar(); expect(c.modalError).toBe('Perfil não encontrado.');
    expect(c.salvando).toBeFalse(); expect((c as any).modal.hide).not.toHaveBeenCalled();
  });
  it('não troca o destinatário enquanto o acesso está sendo criado', () => {
    const resposta = new Subject<{ senhaTemporaria: string }>(); service.criarAcesso.and.returnValue(resposta);
    c.abrirAtribuicao(usuario); c.criarAcesso();
    c.abrirAtribuicao({ ...usuario, id: 8, nome: 'Outra pessoa' });
    c.nomePerfil = 'Operador'; c.confirmar();
    expect(c.selecionado?.id).toBe(7); expect(service.atribuir).not.toHaveBeenCalled();
    resposta.next({ senhaTemporaria: 'temporaria-de-teste' });
    expect(c.senhaTemporaria).toBe('temporaria-de-teste'); expect(c.criandoAcesso).toBeFalse();
  });
});
