import { of, throwError } from 'rxjs';
import { PerfilComponent } from './perfil.component';
import { PerfilService } from '../../../core/services/perfil.service';

describe('Cadastro de nomes de perfis', () => {
  let component: PerfilComponent;
  let service: jasmine.SpyObj<PerfilService>;
  beforeEach(() => {
    service = jasmine.createSpyObj<PerfilService>('PerfilService', ['listar', 'add', 'update', 'catalogoPermissoes', 'permissoes', 'salvarPermissoes']);
    service.listar.and.returnValue(of({ content: [], number: 0, size: 8, totalElements: 0, totalPages: 0 }));
    component = new PerfilComponent(service);
    (component as any).modal = { hide: jasmine.createSpy('hide') };
  });
  it('envia apenas o nome ao cadastrar', () => {
    service.add.and.returnValue(of({ id: 1, nome: 'Operador' }));
    component.form.nome = ' Operador ';
    component.save();
    expect(service.add).toHaveBeenCalledOnceWith('Operador');
  });
  it('impede nomes vazios ou invalidos', () => {
    for (const nome of ['', '  ', 'AB', '<script>', 'A'.repeat(101)]) {
      component.form.nome = nome;
      component.save();
    }
    expect(service.add).not.toHaveBeenCalled();
  });
  it('não salva permissões vazias quando a consulta ao servidor falha', () => {
    service.catalogoPermissoes.and.returnValue(of(['RH.CONSULTAR']));
    service.permissoes.and.returnValue(throwError(() => ({ error: { mensagem: 'Falha na consulta.' } })));
    component.abrirPermissoes({ id: 7, nome: 'RH' }); component.salvarPermissoes();
    expect(service.salvarPermissoes).not.toHaveBeenCalled();
    expect(component.erroPermissoes).toBe('Falha na consulta.');
  });
  it('edita somente o nome', () => {
    service.update.and.returnValue(of({ id: 3, nome: 'Supervisor' }));
    component.isEditing = true;
    component.form = { id: 3, nome: 'Supervisor' };
    component.save();
    expect(service.update).toHaveBeenCalledOnceWith(3, 'Supervisor');
  });
  it('converte a pagina da tela para a pagina da API e usa o total do servidor', () => {
    service.listar.and.returnValue(of({ content: [{ id: 9, nome: 'Operador' }], number: 1, size: 8, totalElements: 17, totalPages: 3 }));
    component.page = 2;
    component.carregar();
    expect(service.listar).toHaveBeenCalledWith(1, 8, '');
    expect(component.totalPages).toBe(3);
    expect(component.inicio).toBe(9);
    expect(component.fim).toBe(16);
  });
});
