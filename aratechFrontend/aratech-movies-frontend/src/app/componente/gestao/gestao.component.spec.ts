import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { SessaoState } from '../../core/services/sessao-state.service';
import { GestaoComponent } from './gestao.component';
describe('Telas operacionais e permissões',()=>{
  let fixture:ComponentFixture<GestaoComponent>;let http:HttpTestingController;let sessao:SessaoState;
  const titulo={id:1,tipo:'PAGAR',descricao:'Compra de MDF',pessoa:'Fornecedor',valor:200,vencimento:'2026-11-20',origem:'COMPRA',origemId:4,status:'PENDENTE',versao:0};
  beforeEach(async()=>{
    await TestBed.configureTestingModule({imports:[GestaoComponent],providers:[provideRouter([]),provideHttpClient(),provideHttpClientTesting(),{provide:AuthService,useValue:{logout:()=>{}}}]}).compileComponents();
    http=TestBed.inject(HttpTestingController);sessao=TestBed.inject(SessaoState);
    sessao.usuario.set({id:1,nome:'Usuário',email:'teste@example.test',administrador:false,perfis:[],permissoes:['FINANCEIRO_PAGAR.CONSULTAR'],trocarSenha:false});
    fixture=TestBed.createComponent(GestaoComponent);fixture.componentInstance.recurso='pagar';
  });
  afterEach(()=>{fixture.destroy();http.verify();});
  function carregar(){fixture.detectChanges();const req=http.expectOne(r=>r.url==='/aratech/financeiro/titulos/pagar');expect(req.request.params.get('pagina')).toBe('0');req.flush({itens:[titulo],total:1,pagina:0,tamanho:8});fixture.detectChanges();}
  it('consulta somente a tela autorizada e oculta funções de escrita',()=>{
    carregar();const texto=fixture.nativeElement.textContent;
    expect(texto).toContain('Compra de MDF');expect(texto).not.toContain('Novo registro');expect(texto).not.toContain('Registrar pagamento');expect(texto).not.toContain('Cancelar título');
    expect(fixture.componentInstance.permitido('FINALIZAR')).toBeFalse();
  });
  it('recebe assistência sem consultar dados de outro setor',()=>{
    sessao.usuario.update(u=>({...u!,permissoes:['ADMINISTRACAO_LOTES.CONSULTAR','ADMINISTRACAO_LOTES.CADASTRAR']}));const c=fixture.componentInstance;c.recurso='recebimentoAssistencia';fixture.detectChanges();
    http.expectNone(r=>r.url.startsWith('/aratech/'));c.abrir('novo');c.form={origemId:10,codigo:'RET-10',produto:'Cadeira',quantidade:2,empresa:'Cliente',responsavel:'Recepção',defeito:'Acabamento'};c.salvar();
    const req=http.expectOne('/aratech/assistencia/lotes');expect(req.request.method).toBe('POST');expect(req.request.body.status).toBeUndefined();req.flush({id:1,status:'Não iniciado'});http.expectNone(r=>r.url.startsWith('/aratech/'));
  });
  it('permite baixa com permissão detalhada sem liberar outras telas',()=>{
    sessao.usuario.update(u=>({...u!,permissoes:['FINANCEIRO_PAGAR.CONSULTAR','FINANCEIRO_PAGAR.FINALIZAR']}));carregar();
    expect(fixture.nativeElement.textContent).toContain('Registrar pagamento');expect(fixture.nativeElement.textContent).not.toContain('Novo registro');
    expect(sessao.permite('FINANCEIRO_RECEBER.CONSULTAR')).toBeFalse();
  });
  it('repete exatamente o mesmo corpo após falha de rede e só altera após confirmação',()=>{
    sessao.usuario.update(u=>({...u!,permissoes:['FINANCEIRO_PAGAR.CONSULTAR','FINANCEIRO_PAGAR.FINALIZAR']}));carregar();
    const c=fixture.componentInstance;const a=c.acoes(titulo)[0];c.abrir('acao',titulo,a);c.form={data:'2026-11-05',motivo:'Pagamento confirmado'};c.salvar();
    const primeiro=http.expectOne('/aratech/financeiro/titulos/pagar/1/baixar');const corpo=primeiro.request.body;
    primeiro.error(new ProgressEvent('rede'));expect(c.emConfirmacao).toBeTrue();expect(c.itens[0]['status']).toBe('PENDENTE');
    c.form['motivo']='Outro motivo';c.salvar();const repeticao=http.expectOne('/aratech/financeiro/titulos/pagar/1/baixar');expect(repeticao.request.body).toEqual(corpo);
    repeticao.flush({...titulo,status:'BAIXADO',versao:1});http.expectOne(r=>r.url==='/aratech/financeiro/titulos/pagar').flush({itens:[{...titulo,status:'BAIXADO'}],total:1});expect(c.emConfirmacao).toBeFalse();
  });
});
