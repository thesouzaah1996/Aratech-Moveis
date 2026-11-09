import { TestBed,ComponentFixture } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of,Subject } from 'rxjs';
import { GuardaRoupasComponent } from './guarda-roupas.component';
import { LotesFabricacaoComponent } from './lotes-fabricacao.component';
import { FabricacaoService } from '../../core/services/fabricacao.service';
import { SessaoState } from '../../core/services/sessao-state.service';
import { AuthService } from '../../core/services/auth.service';
import { ItemGuardaRoupa,ModeloGuardaRoupa,LoteCorte } from '../../core/models/fabricacao.model';
import { permissaoDaRota } from '../../core/acesso.guard';

const item:ItemGuardaRoupa={chave:'d4ed7de5-e18c-4621-b038-c6004f4444e6',codigo:'LAT',nome:'Lateral',tipo:'PAINEL',grupo:'Estrutura',material:'MDF',acabamento:'Branco',unidade:'UN',quantidade:2,cortar:true,comprimentoMm:2200,larguraMm:600,espessuraMm:18,veio:'LONGITUDINAL',bordas:['C1'],fitaBorda:'Branco 1 mm',furacao:'Desenho D-01',observacoes:'Lateral direita e esquerda',atributos:{Carga:'Conforme projeto'}};
const modelo:ModeloGuardaRoupa={id:10,versao:4,ficha:{codigo:'G-01',nome:'Aurora',descricao:'Guarda-roupa seis portas',larguraMm:1800,alturaMm:2200,profundidadeMm:600,ativo:true,versao:3,itens:[item]},fotos:[]};
const lote:LoteCorte={id:20,modeloId:10,modeloVersao:4,quantidade:10,previsao:'2026-12-20',observacoes:'Conferir acabamento',status:'ENVIADO_CORTE',versao:2,modelo,autor:'Ana Silva',criadoEm:'2026-11-08T10:00:00Z',enviadoEm:'2026-11-08T11:00:00Z',iniciadoEm:null,concluidoEm:null};

describe('Fabricação: catálogo e lotes',()=>{
  let api:jasmine.SpyObj<FabricacaoService>;
  let fixture:ComponentFixture<GuardaRoupasComponent>|ComponentFixture<LotesFabricacaoComponent>|undefined;
  beforeEach(()=>{
    api=jasmine.createSpyObj('FabricacaoService',['modelos','modelo','salvar','foto','retirarFoto','lotes','lote','criarLote','acao','urlFoto']);
    api.modelos.and.returnValue(of({itens:[],total:0,pagina:0,tamanho:12}));
    api.lotes.and.returnValue(of({itens:[],total:0,pagina:0,tamanho:12}));
    api.modelo.and.returnValue(of(structuredClone(modelo)));api.lote.and.returnValue(of(structuredClone(lote)));api.urlFoto.and.returnValue('/foto-teste.png');
    TestBed.configureTestingModule({providers:[provideRouter([]),{provide:FabricacaoService,useValue:api},{provide:AuthService,useValue:{logout:()=>{}}}]});
    TestBed.inject(SessaoState).usuario.set({id:1,nome:'Ana Silva',email:'ana@aratechmoveis',administrador:true,perfis:[],permissoes:[],trocarSenha:false});
  });
  afterEach(()=>fixture?.destroy());
  function catalogo(){const f=TestBed.createComponent(GuardaRoupasComponent);fixture=f;f.detectChanges();return f.componentInstance;}
  function lotes(){const f=TestBed.createComponent(LotesFabricacaoComponent);fixture=f;f.detectChanges();return f.componentInstance;}
  it('preserva a chave do componente e os atributos livres ao editar a revisão atual',()=>{
    const c=catalogo();c.selecionado=structuredClone(modelo);c.editar();
    c.itensEditor[0].campos.push({chave:'Fornecedor',valor:'Referência X'});
    api.salvar.and.returnValue(of({...modelo,versao:5}));c.salvar();
    const payload=api.salvar.calls.mostRecent().args[0];
    expect(payload.versao).toBe(4);expect(payload.itens[0].chave).toBe(item.chave);
    expect(payload.itens[0].atributos).toEqual({Carga:'Conforme projeto',Fornecedor:'Referência X'});
    expect((payload.itens[0] as any).campos).toBeUndefined();
  });
  it('impede salvar uma composição quando um painel está sem medida de corte',()=>{
    const c=catalogo();c.selecionado=structuredClone(modelo);c.editar();c.itensEditor[0].larguraMm=null;c.salvar();
    expect(api.salvar).not.toHaveBeenCalled();expect(c.erro).toContain('LAT');
  });
  it('mostra no catálogo salvo os detalhes de fabricação e o controle de fotos por item',()=>{
    const c=catalogo();c.abrir(10);fixture!.detectChanges();
    const texto=(fixture!.nativeElement as HTMLElement).textContent!;
    expect(texto).toContain('Aurora');expect(texto).toContain('2200 × 600 × 18 mm');expect(texto).toContain('Furações / usinagem');
    expect(fixture!.nativeElement.querySelectorAll('app-galeria-fabricacao').length).toBe(2);
  });
  it('usa a versão atual ao enviar foto para o componente correto',()=>{
    const c=catalogo();c.selecionado=structuredClone(modelo);api.foto.and.returnValue(of({...modelo,versao:5}));
    const arquivo=new File(['imagem'],'lateral.png',{type:'image/png'});c.enviarFoto(item.chave,{arquivo,legenda:'Lateral'});
    expect(api.foto).toHaveBeenCalledOnceWith(jasmine.objectContaining({id:10,versao:4}),item.chave,arquivo,'Lateral');expect(c.selecionado?.versao).toBe(5);
  });
  it('multiplica peças e área pelo lote sem confundir a área com quantidade de chapas',()=>{
    const c=lotes();c.corte=true;c.detalhe=structuredClone(lote);fixture!.detectChanges();
    expect(c.totalItem(item)).toBe(20);expect(c.pecasCorte).toBe(20);expect(c.areaCorte).toBeCloseTo(26.4);
    const texto=fixture!.nativeElement.textContent;expect(texto).toContain('Área das peças');expect(texto).not.toContain('Quantidade de chapas');
  });
  it('impede clique duplicado ao iniciar corte e mantém a ficha recebida',()=>{
    const c=lotes();c.corte=true;c.detalhe=structuredClone(lote);const resposta=new Subject<LoteCorte>();api.acao.and.returnValue(resposta);
    c.executar('iniciar');c.executar('iniciar');expect(api.acao).toHaveBeenCalledTimes(1);
    resposta.next({...lote,status:'EM_CORTE',versao:3});expect(c.detalhe?.modeloVersao).toBe(4);expect(c.detalhe?.status).toBe('EM_CORTE');
  });
  it('separa permissões de catálogo, lotes do PCP e Corte',()=>{
    expect(permissaoDaRota('administracao/guarda-roupas')).toBe('ADMINISTRACAO_GUARDA_ROUPAS.CONSULTAR');
    expect(permissaoDaRota('pcp/lotes/concluidos')).toBe('PCP_LOTES.CONSULTAR');
    expect(permissaoDaRota('corte/lotes')).toBe('CORTE_LOTES.CONSULTAR');
  });
});
