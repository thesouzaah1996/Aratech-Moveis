import { FuncionariosComponent } from './funcionarios.component';
import { FuncionarioService } from '../../../core/services/funcionario.service';
import { Funcionario } from '../../../core/models/funcionario.model';
import { of, Subject, throwError } from 'rxjs';

describe('Formulario de admissao', () => {
  let service: jasmine.SpyObj<FuncionarioService>;
  let component: FuncionariosComponent;
  beforeEach(() => {
    service = jasmine.createSpyObj('FuncionarioService', ['add', 'enable', 'disable', 'atribuirEmailCorporativo']);
    component = new FuncionariosComponent(service);
  });

  const funcionario: Funcionario = { id: 42, nome: 'Ana Silva', email: 'ana@example.test', cargo: 'Analista', setor: 'RH', ativo: true, perfis: [] };
  function prepararStatus(): Funcionario {
    const f = { ...funcionario };
    component.funcionarios = [f];
    (component as any).statusModal = { show: jasmine.createSpy(), hide: jasmine.createSpy() };
    return f;
  }
  it('cria o acesso corporativo sem substituir o e-mail pessoal e bloqueia cliques duplicados', () => {
    const f = { ...funcionario };
    (component as any).emailModal = { show: jasmine.createSpy(), hide: jasmine.createSpy() };
    const resposta = new Subject<{ emailCorporativo: string; senhaTemporaria: string }>();
    service.atribuirEmailCorporativo.and.returnValue(resposta);
    component.abrirEmail(f); component.emailCorporativo = 'ana@aratechmoveis';
    component.criarEmailCorporativo(); component.criarEmailCorporativo();
    expect(service.atribuirEmailCorporativo).toHaveBeenCalledOnceWith(42, 'ana@aratechmoveis');
    component.abrirEmail({ ...f, id: 99 }); expect(component.funcionarioEmail?.id).toBe(42);
    resposta.next({ emailCorporativo: 'ana@aratechmoveis', senhaTemporaria: 'temporaria' });
    expect(f.email).toBe('ana@example.test'); expect(f.emailCorporativo).toBe('ana@aratechmoveis');
    expect(component.senhaTemporaria).toBe('temporaria');
    component.fecharEmail(); expect(component.senhaTemporaria).toBe('');
  });
  it('nao envia e-mail invalido e mostra falha sem alterar o funcionario', () => {
    component.abrirEmail({ ...funcionario }); component.emailCorporativo = 'invalido'; component.criarEmailCorporativo();
    expect(service.atribuirEmailCorporativo).not.toHaveBeenCalled();
    component.emailCorporativo = 'ana@aratechmoveis';
    service.atribuirEmailCorporativo.and.returnValue(throwError(() => ({ error: { mensagem: 'O usuário já possui acesso.' } })));
    component.criarEmailCorporativo(); expect(component.erroEmail).toContain('já possui');
    expect(component.criandoEmail).toBeFalse(); expect(component.senhaTemporaria).toBe('');
  });
  it('pede confirmação e cancelar preserva o status sem chamar o backend', () => {
    const f = prepararStatus();
    component.toggleAtivo(f);
    expect((component as any).statusModal.show).toHaveBeenCalled();
    expect(service.disable).not.toHaveBeenCalled();
    expect(f.ativo).toBeTrue();
    component.cancelarStatus(); component.confirmarStatus();
    expect(service.disable).not.toHaveBeenCalled();
    expect(f.ativo).toBeTrue();
  });
  it('desativa só após confirmar, impede envio duplicado e respeita o retorno do servidor', () => {
    const f = prepararStatus();
    const resposta = new Subject<Pick<Funcionario, 'id' | 'nome' | 'ativo'>>();
    service.disable.and.returnValue(resposta);
    component.toggleAtivo(f); component.confirmarStatus(); component.confirmarStatus();
    expect(service.disable).toHaveBeenCalledOnceWith(42);
    expect(f.ativo).toBeTrue();
    resposta.next({ id: 42, nome: f.nome, ativo: false });
    expect(f.ativo).toBeFalse(); expect(component.alterandoStatus).toBeFalse();
    expect((component as any).statusModal.hide).toHaveBeenCalled();
  });
  it('mantém o status e a confirmação aberta quando o servidor falha', () => {
    const f = prepararStatus();
    service.disable.and.returnValue(throwError(() => ({ error: { mensagem: 'Falha ao desativar.' } })));
    component.toggleAtivo(f); component.confirmarStatus();
    expect(f.ativo).toBeTrue(); expect(component.erroStatus).toBe('Falha ao desativar.');
    expect((component as any).statusModal.hide).not.toHaveBeenCalled();
    expect(component.alterandoStatus).toBeFalse();
  });
  it('permite reativar mediante confirmação', () => {
    const f = prepararStatus(); f.ativo = false;
    service.enable.and.returnValue(of({ id: 42, nome: f.nome, ativo: true }));
    component.toggleAtivo(f);
    expect(service.enable).not.toHaveBeenCalled();
    component.confirmarStatus();
    expect(service.enable).toHaveBeenCalledOnceWith(42); expect(f.ativo).toBeTrue();
  });

  it('nao envia cadastro incompleto mesmo ao concluir na etapa de documentos', () => {
    component.activeTab = 4;
    component.save();
    expect(service.add).not.toHaveBeenCalled();
    expect(component.activeTab).toBe(0);
    expect(component.erroCadastro).toBeTruthy();
  });

  it('preserva todos os digitos ao aplicar as mascaras de CPF e PIS', () => {
    const input = document.createElement('input');
    input.value = '52998224725';
    component.applyCpfMask({target: input} as unknown as Event);
    expect(component.form.cpf).toBe('529.982.247-25');
    input.value = '12345678901';
    component.applyPisMask({target: input} as unknown as Event);
    expect(component.form.pis).toBe('123.45678.90-1');
  });

  it('rejeita jornada zero inclusive quando o input numerico retorna um numero', () => {
    Object.assign(component.form, {
      dataNascimento: '1990-01-01', dataAdmissao: '2026-01-01', tipoContrato: 'CLT',
      cargo: 'Analista', tipoFuncionario: 'ADMINISTRATIVO', setor: 'RH', salario: '3.000,00', jornadaHoras: 0
    });
    expect(component.isTabValid(2)).toBeFalse();
    component.form.jornadaHoras = '44';
    expect(component.isTabValid(2)).toBeTrue();
  });

  function preencherIdentificacao(): void {
    Object.assign(component.form, {
      nome: 'Maria Silva', cpf: '529.982.247-25', rg: '', pis: '',
      dataNascimento: '1990-01-01', sexo: 'NAO_INFORMADO', nomeMae: 'Ana Silva'
    });
  }

  it('aceita identificacao valida com CPF formatado e opcionais vazios', () => {
    preencherIdentificacao();
    component.nextTab();
    expect(component.activeTab).toBe(1);
    expect(component.erroCadastro).toBe('');
  });

  it('nao depende do formato regional do navegador para validar nascimento', () => {
    preencherIdentificacao();
    spyOn(Date.prototype, 'toLocaleDateString').and.returnValue('03/11/2026');
    expect(component.isTabValid(0)).toBeTrue();
  });

  it('indica exatamente o campo que impede avancar', () => {
    preencherIdentificacao();
    component.form.sexo = '';
    component.nextTab();
    expect(component.activeTab).toBe(0);
    expect(component.erroCadastro).toBe('Selecione o sexo ou a opção Não informado.');
    expect(Object.keys(component.errosIdentificacao)).toEqual(['sexo']);
    component.form.sexo = 'MASCULINO';
    component.form.cpf = '529.982.247-24';
    component.nextTab();
    expect(component.erroCadastro).toContain('CPF inválido');
    expect(component.erroCadastro).not.toContain('Nome');
  });

  it('rejeita nascimento inexistente, hoje e futuro, aceitando dia bissexto valido', () => {
    preencherIdentificacao();
    jasmine.clock().install();
    try {
      jasmine.clock().mockDate(new Date(2026, 10, 3, 14));
      for (const data of ['2026-11-03', '2026-11-04', '1990-02-30', '1991-02-29']) {
        component.form.dataNascimento = data;
        expect(component.errosIdentificacao.dataNascimento).toBeTruthy();
      }
      component.form.dataNascimento = '1992-02-29';
      expect(component.isTabValid(0)).toBeTrue();
    } finally { jasmine.clock().uninstall(); }
  });
});
