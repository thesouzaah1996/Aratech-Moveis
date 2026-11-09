import { TestBed,ComponentFixture } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of,Subject } from 'rxjs';
import { EsteirasComponent } from './esteiras.component';
import { LotesEmbalagemComponent } from './lotes-embalagem.component';
import { EmbalagemService } from '../../core/services/embalagem.service';
import { FabricacaoService } from '../../core/services/fabricacao.service';
import { SessaoState } from '../../core/services/sessao-state.service';
import { AuthService } from '../../core/services/auth.service';
import { Esteira,PainelEsteira,TarefaEsteira,LoteEmbalagem } from '../../core/models/embalagem.model';
import { permissaoDaRota } from '../../core/acesso.guard';

const a:Esteira={id:1,codigo:'EST-01',nome:'Esteira 1',atividade:'Proteção de peças',responsavel:'Equipe A',capacidade:1,ativo:true,observacoes:'',versao:1};
const b:Esteira={...a,id:2,codigo:'EST-02',nome:'Esteira 2',atividade:'Fechamento de caixas',responsavel:'Equipe B'};
const tarefa:TarefaEsteira={etapaId:11,loteId:20,versao:2,sequencia:1,modelo:'Aurora',quantidade:10,previsao:'2026-12-20',prioridade:'NORMAL',nome:'Proteção',instrucoes:'Aplicar proteção nas peças',status:'EM_EXECUCAO',operador:'Ana Silva',motivoPausa:'',segundosAtivos:120,ultima:false};
const painel:PainelEsteira={esteira:a,ocupadas:1,trabalhando:[tarefa],fila:[],futuras:0};
const lote:LoteEmbalagem={id:20,origemId:10,status:'EM_EMBALAGEM',versao:2,quantidade:10,previsao:'2026-12-20',prioridade:'NORMAL',observacoes:'',volumes:null,criadoEm:'2026-11-08T10:00:00Z',concluidoEm:null,modelo:{id:5,versao:1,ficha:{codigo:'GR-01',nome:'Aurora',descricao:'',larguraMm:1800,alturaMm:2200,profundidadeMm:600,ativo:true,versao:0,itens:[]},fotos:[]},etapas:[],historico:[]};

describe('Embalagem: lotes e esteiras',()=>{
  let api:jasmine.SpyObj<EmbalagemService>;let fixture:ComponentFixture<EsteirasComponent>|ComponentFixture<LotesEmbalagemComponent>|undefined;
  beforeEach(()=>{
    api=jasmine.createSpyObj('EmbalagemService',['painel','salvarEsteira','esteiras','origens','lotes','lote','receber','cancelar','operar']);
    api.painel.and.returnValue(of([structuredClone(painel),{esteira:b,ocupadas:1,trabalhando:[{...tarefa,etapaId:12,loteId:19,nome:'Fechamento',ultima:true}],fila:[],futuras:1}]));
    api.esteiras.and.returnValue(of([a,b]));api.origens.and.returnValue(of({itens:[{id:10,codigoModelo:'GR-01',nomeModelo:'Aurora',quantidade:10,previsao:'2026-12-20',status:'CORTADO',versao:4}],total:1,pagina:0,tamanho:100}));api.lotes.and.returnValue(of({itens:[],total:0,pagina:0,tamanho:12}));api.lote.and.returnValue(of(lote));
    TestBed.configureTestingModule({providers:[provideRouter([]),{provide:EmbalagemService,useValue:api},{provide:FabricacaoService,useValue:{urlFoto:()=>'/foto-teste.png'}},{provide:AuthService,useValue:{logout:()=>{}}}]});
    TestBed.inject(SessaoState).usuario.set({id:7,nome:'Ana Silva',email:'ana@aratechmoveis',administrador:true,perfis:[],permissoes:[],trocarSenha:false});
  });
  afterEach(()=>fixture?.destroy());
  function esteiras(){const f=TestBed.createComponent(EsteirasComponent);fixture=f;f.detectChanges();return f.componentInstance;}
  function lotes(){const f=TestBed.createComponent(LotesEmbalagemComponent);fixture=f;f.detectChanges();return f.componentInstance;}
  it('mostra trabalhos diferentes em esteiras diferentes e identifica o lote em execução',()=>{
    esteiras();const tela:HTMLElement=fixture!.nativeElement;
    expect(tela.querySelectorAll('.coluna-esteira').length).toBe(2);expect(tela.textContent).toContain('Proteção de peças');expect(tela.textContent).toContain('Fechamento de caixas');expect(tela.textContent).toContain('EB-20');expect(tela.textContent).toContain('EB-19');expect(tela.textContent).toContain('Ana Silva');
  });
  it('mantém posição ocupada por pausa e bloqueia início de outro trabalho no painel',()=>{
    const c=esteiras();c.paineis=[{...painel,trabalhando:[{...tarefa,status:'PAUSADA',motivoPausa:'Aguardando material'}],fila:[{...tarefa,etapaId:99,loteId:30,status:'AGUARDANDO'}]}];fixture!.detectChanges();
    const botao=Array.from((fixture!.nativeElement as HTMLElement).querySelectorAll('button')).find(b=>b.textContent?.includes('Iniciar etapa'))!;
    expect((botao as HTMLButtonElement).disabled).toBeTrue();expect(c.ocupadas).toBe(1);expect(c.pausadas).toBe(1);expect(fixture!.nativeElement.textContent).toContain('Aguardando material');
  });
  it('evita duas operações por clique repetido e envia a versão exibida do lote',()=>{
    const c=esteiras();const resposta=new Subject<LoteEmbalagem>();api.operar.and.returnValue(resposta);
    c.preparar(painel,tarefa,'retomar');c.preparar(painel,tarefa,'retomar');expect(api.operar).toHaveBeenCalledOnceWith(1,11,'retomar',{versao:2});
    resposta.next(lote);expect(c.ocupado).toBeFalse();
  });
  it('exige volumes e as conferências para concluir a última etapa',()=>{
    const c=esteiras();spyOn(c.dialogo.nativeElement,'showModal').and.stub();const final={...tarefa,ultima:true};c.preparar(painel,final,'concluir');c.confirmar();expect(api.operar).not.toHaveBeenCalled();
    c.volumes=4;c.pecas=true;c.protecao=true;c.identificacao=true;api.operar.and.returnValue(of({...lote,status:'EMBALADO',volumes:4}));c.confirmar();
    expect(api.operar.calls.mostRecent().args[3]).toEqual({versao:2,motivo:'',volumes:4,pecasConferidas:true,protecaoAplicada:true,identificacaoConferida:true});
  });
  it('recebe lote apenas após prontidão e mantém o UUID em uma nova tentativa de rede',()=>{
    const c=lotes();c.novoLote();c.origemId=10;c.previsao='2026-12-20';c.etapas=[{esteiraId:1,nome:'Proteção',instrucoes:''}];c.receber();expect(api.receber).not.toHaveBeenCalled();c.pronto=true;
    const resposta=new Subject<LoteEmbalagem>();api.receber.and.returnValues(resposta,of(lote));c.receber();const primeiro=api.receber.calls.mostRecent().args[0];resposta.error({status:0});c.receber();expect(api.receber.calls.mostRecent().args[0]).toEqual(primeiro);expect(primeiro.prontoParaEmbalagem).toBeTrue();expect(primeiro.origemId).toBe(10);
  });
  it('associa a operação à esteira escolhida e preserva a ordem ao reorganizar roteiro',()=>{
    const c=lotes();c.sugerirRoteiro();c.etapas[0].esteiraId=1;c.etapas[1].esteiraId=2;c.mover(1,-1);expect(c.etapas[0].esteiraId).toBe(2);expect(c.etapas[1].esteiraId).toBe(1);expect(c.etapas[0].nome).toBe('Montagem e fechamento');
  });
  it('mantém permissões independentes para lotes e esteiras',()=>{
    expect(permissaoDaRota('embalagem/lotes/20')).toBe('EMBALAGEM_LOTES.CONSULTAR');expect(permissaoDaRota('embalagem/esteiras')).toBe('EMBALAGEM_ESTEIRAS.CONSULTAR');expect(permissaoDaRota('embalagem')).toBe('EMBALAGEM.CONSULTAR');
  });
});
