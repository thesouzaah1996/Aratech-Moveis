import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { FuncionarioService } from './funcionario.service';
import { FuncionarioForm, ArquivosFuncionario } from '../models/funcionario.model';

 describe('Cadastro de funcionario - contrato HTTP', () => {
  let service: FuncionarioService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(FuncionarioService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('envia os dados sem mascaras e os documentos com os nomes esperados pelo backend', () => {
    const form: FuncionarioForm = {
      nome: ' Ana Silva ', cpf: '529.982.247-25', pis: '', dataNascimento: '1990-01-01',
      sexo: 'FEMININO', estadoCivil: 'SOLTEIRO', nomeMae: 'Maria Silva',
      cep: '12345-678', logradouro: 'Rua das Flores', numero: '10', bairro: 'Centro',
      cidade: 'Sao Paulo', uf: 'SP', telefone: '(11) 1234-5678', celular: '(11) 91234-5678',
      dataAdmissao: '2026-01-01', tipoContrato: 'CLT', jornadaHoras: 44,
      cargo: 'Analista', tipoFuncionario: 'ADMINISTRATIVO', setor: 'RH', salario: 3000.25,
      email: ' ANA@EXEMPLO.COM ', banco: 'Banco', agencia: '1234', conta: '12345-6',
      tipoConta: 'CORRENTE', pix: 'chave'
    };
    const file = new File(['%PDF-1.7'], 'documento.pdf', {type: 'application/pdf'});
    const arquivos: ArquivosFuncionario = {
      rg: file, ctps: file, aso: file, compResidencia: file, tituloEleitor: null,
      reservista: null, diplomaEscolar: file, certidaoCasamento: null, certidaoNascimento: null
    };
    let resultado: any;
    service.add(form, arquivos).subscribe(novo => resultado = novo);
    const req = http.expectOne('/aratech/rh/funcionarios');
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.has('Content-Type')).toBeFalse();
    const body: FormData = req.request.body;
    const esperados: Record<string, string> = {
      nomeCompleto: 'Ana Silva', cpf: '52998224725', cep: '12345678', telefoneFixo: '1112345678',
      celularWhatsapp: '11912345678', jornadaSemanal: '44', chavePix: 'chave',
      salario: '3000.25', email: 'ana@exemplo.com', dataNascimento: '1990-01-01',
      dataAdmissao: '2026-01-01', sexo: 'FEMININO', tipoContrato: 'CLT', tipoConta: 'CORRENTE'
    };
    Object.entries(esperados).forEach(([campo, valor]) => expect(body.get(campo)).withContext(campo).toBe(valor));
    ['rgOuCnh', 'ctps', 'aso', 'comprovanteResidencia', 'diplomaCertificado'].forEach(campo => {
      expect((body.get(campo) as File).name).toBe('documento.pdf');
    });
    ['nome', 'pis', 'pisNitNis', 'telefone', 'celular', 'jornadaHoras', 'pix', 'reservista'].forEach(campo => {
      expect(body.has(campo)).withContext(campo).toBeFalse();
    });
    req.flush({status: 201, mensagem: 'Cadastrado', funcionarioId: 42, nomeCompleto: 'Ana Silva', ativo: true});
    expect(resultado.id).toBe(42);
    expect(resultado.nome).toBe('Ana Silva');
    expect(resultado.ativo).toBeTrue();
    expect(resultado.cargo).toBe('Analista');
  });

  it('consulta todas as paginas e traduz os nomes devolvidos pelo backend',()=>{
    let resultado:any[]=[];service.getAll().subscribe(lista=>resultado=lista);
    const a=http.expectOne(r=>r.url==='/aratech/rh/funcionarios'&&r.params.get('pagina')==='0');
    expect(a.request.params.get('tamanho')).toBe('100');
    a.flush({status:200,funcionarios:[{id:1,nomeCompleto:'Ana Silva',email:'ana@example.invalid',cargo:'Analista',setor:'RH',telefoneFixo:'1133334444',ativo:true}],pagina:0,tamanho:100,total:101});
    http.expectOne(r=>r.url==='/aratech/rh/funcionarios'&&r.params.get('pagina')==='1').flush({status:200,funcionarios:[{id:2,nomeCompleto:'Carlos Souza',ativo:false}],pagina:1,tamanho:100,total:101});
    expect(resultado.length).toBe(2);expect(resultado[0].nome).toBe('Ana Silva');expect(resultado[0].telefone).toBe('1133334444');expect(resultado[1].ativo).toBeFalse();
  });
  it('propaga erro de consulta para a tela, sem simular lista vazia',()=>{
    let erro=false;service.getAll().subscribe({error:()=>erro=true});http.expectOne(r=>r.url==='/aratech/rh/funcionarios').flush({}, {status:500,statusText:'Erro'});expect(erro).toBeTrue();
  });
  it('desativa e reativa usando os endpoints reais e traduz o status retornado', () => {
    let ativo: boolean | undefined;
    service.disable(42).subscribe(f => ativo = f.ativo);
    const desativar = http.expectOne('/aratech/rh/funcionarios/42/desativar');
    expect(desativar.request.method).toBe('PATCH');
    expect(desativar.request.body).toEqual({});
    desativar.flush({ status: 200, funcionarioId: 42, nomeCompleto: 'Ana Silva', ativo: false });
    expect(ativo).toBeFalse();
    service.enable(42).subscribe(f => ativo = f.ativo);
    const ativar = http.expectOne('/aratech/rh/funcionarios/42/ativar');
    expect(ativar.request.method).toBe('PATCH');
    ativar.flush({ status: 200, funcionarioId: 42, nomeCompleto: 'Ana Silva', ativo: true });
    expect(ativo).toBeTrue();
  });
});
