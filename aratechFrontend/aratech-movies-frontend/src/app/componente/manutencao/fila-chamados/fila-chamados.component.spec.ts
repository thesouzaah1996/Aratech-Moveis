import { SessaoState } from '../../../core/services/sessao-state.service';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError, Subject } from 'rxjs';
import { FilaChamadosComponent } from './fila-chamados.component';
import { FilaManutencaoService } from '../../../core/services/fila-manutencao.service';
import { MecanicoService } from '../../../core/services/mecanico.service';
import { ItemFilaManutencao } from '../../../core/models/fila-manutencao.model';
const manutencao: ItemFilaManutencao = { id:42,origem:'MANUTENCAO',equipamento:'Prensa',tipo:'CORRETIVA',prioridade:'ALTA',solicitante:'Ana Silva',setor:'Producao',telefone:'',descricao:'Motor parado',status:'ABERTA',dataAbertura:'2026-11-01T10:00:00Z',mecanico:null,dataInicio:null,dataConclusao:null,peca:null };
const peca: ItemFilaManutencao = { ...manutencao,id:43,origem:'PECA',tipo:null,peca:{id:43,nomePeca:'Rolamento',codigo:'SKF',quantidade:1.5,unidade:'kg',equipamento:'Prensa',finalidade:'MELHORIA',prioridade:'ALTA',solicitante:'Ana Silva',setor:'Producao',observacoes:'Trocar rolamento',criadoEm:manutencao.dataAbertura,status:'ABERTA'} };
describe('Fila unificada de chamados', () => {
  let c:FilaChamadosComponent;let s:jasmine.SpyObj<FilaManutencaoService>;let m:jasmine.SpyObj<MecanicoService>;
  beforeEach(() => {
    s=jasmine.createSpyObj('FilaManutencaoService',['getPagina','getById','atribuirMecanico','concluir','mecanicos']);m=jasmine.createSpyObj('MecanicoService',['lookup']);
    s.getPagina.and.returnValue(of({status:200,itens:[manutencao,peca],pagina:0,tamanho:8,total:17}));s.getById.and.returnValue(of(peca));s.mecanicos.and.returnValue(of([{id:7,nome:'Mecanico'}]));
    c=new FilaChamadosComponent(s,m);
    for(const name of ['atribuirModal','detalhesModal','concluirModal']) (c as any)[name]={show:jasmine.createSpy('show'),hide:jasmine.createSpy('hide'),dispose:()=>{}};
  });
  afterEach(() => c.ngOnDestroy());
  it('mostra as duas origens e usa a pagina do servidor sem recortar novamente', () => {
    c.carregarChamados();expect(c.paged.map(i=>i.id)).toEqual([42,43]);expect(c.totalPages).toBe(3);c.setPage(2);expect(s.getPagina).toHaveBeenCalledWith(1,8,undefined,'');expect(c.paged.length).toBe(2);expect(c.origemLabel(peca)).not.toBe(c.origemLabel(manutencao));expect(c.tipoDescricao(peca)).toBe('Melhoria');
  });
  it('atualiza automaticamente quando chega outra solicitacao e encerra o polling ao sair', fakeAsync(() => {
    c.ngOnInit();s.getPagina.and.returnValue(of({status:200,itens:[peca],pagina:0,tamanho:8,total:1}));tick(5000);expect(c.paged[0].origem).toBe('PECA');c.ngOnDestroy();tick(10000);expect(s.getPagina).toHaveBeenCalledTimes(2);
  }));
  it('busca com debounce e preserva filtros na paginacao', fakeAsync(() => {
    c.ngOnInit();c.searchTerm='Rolamento';c.onSearch();tick(301);expect(s.getPagina).toHaveBeenCalledWith(0,8,undefined,'Rolamento');c.setFiltro('ABERTA');expect(s.getPagina).toHaveBeenCalledWith(0,8,'ABERTA','Rolamento');c.ngOnDestroy();
  }));
  it('mantem os dados e informa quando a atualizacao falha', () => {
    c.carregarChamados();s.getPagina.and.returnValue(throwError(()=>({status:503})));c.carregarChamados();expect(c.paged.length).toBe(2);expect(c.errorMessage).toContain('desatualizados');
  });
  it('atribui peca pelo ID e impede envios simultaneos', () => {
    const resposta=new Subject<ItemFilaManutencao>();s.atribuirMecanico.and.returnValue(resposta);c.openAtribuir(peca);c.mecanicoId=7;c.confirmarAtribuicao();c.confirmarAtribuicao();expect(s.atribuirMecanico).toHaveBeenCalledOnceWith(43,7);resposta.next({...peca,status:'EM_MANUTENCAO'});resposta.complete();expect((c as any).atribuirModal.hide).toHaveBeenCalled();
  });
  it('conflito de atribuicao permanece no modal', () => {
    s.atribuirMecanico.and.returnValue(throwError(()=>({error:{mensagem:'Mecanico inativo.'}})));c.openAtribuir(manutencao);c.mecanicoId=7;c.confirmarAtribuicao();expect(c.modalError).toBe('Mecanico inativo.');expect((c as any).atribuirModal.hide).not.toHaveBeenCalled();
  });
  it('conclui somente quando o usuario confirma', () => {
    s.concluir.and.returnValue(of({...peca,status:'CONCLUIDA'}));c.openConcluir({...peca,status:'EM_MANUTENCAO'});expect(s.concluir).not.toHaveBeenCalled();c.confirmarConclusao();expect(s.concluir).toHaveBeenCalledWith(43);
  });
  it('dropdown da linha carrega mecanicos cadastrados e so atribui ao confirmar', () => {
    c.carregarMecanicos();c.selecionarMecanico(manutencao.id,7);expect(s.atribuirMecanico).not.toHaveBeenCalled();
    const resposta=new Subject<ItemFilaManutencao>();s.atribuirMecanico.and.returnValue(resposta);
    c.atribuirDaLinha(manutencao);c.atribuirDaLinha(manutencao);expect(s.atribuirMecanico).toHaveBeenCalledOnceWith(42,7);expect(c.atribuindoId).toBe(42);
    resposta.next({...manutencao,status:'EM_MANUTENCAO'});resposta.complete();expect(c.atribuindoId).toBeNull();expect(c.atribuicoesPendentes[42]).toBeUndefined();
  });
  it('nao atribui pela linha sem mecanico ativo ou depois de sair de aberto', () => {
    c.carregarMecanicos();c.selecionarMecanico(42,999);c.atribuirDaLinha(manutencao);expect(s.atribuirMecanico).not.toHaveBeenCalled();expect(c.errosAtribuicao[42]).toContain('ativo');
    c.selecionarMecanico(42,7);c.atribuirDaLinha({...manutencao,status:'EM_MANUTENCAO'});expect(s.atribuirMecanico).not.toHaveBeenCalled();
  });
  it('erro de atribuicao fica na linha e permite tentar novamente', () => {
    c.carregarMecanicos();c.selecionarMecanico(42,7);s.atribuirMecanico.and.returnValue(throwError(()=>({error:{mensagem:'Mecanico inativo.'}})));
    c.atribuirDaLinha(manutencao);expect(c.errosAtribuicao[42]).toBe('Mecanico inativo.');expect(c.salvando).toBeFalse();
  });
  it('atualiza mecanicos cadastrados durante o uso da fila', fakeAsync(() => {
    c.ngOnInit();expect(c.mecanicosAtivos.map(m=>m.id)).toEqual([7]);s.mecanicos.and.returnValue(of([{id:8,nome:'Novo mecanico'}]));tick(5000);expect(c.mecanicosAtivos.map(m=>m.id)).toEqual([8]);c.ngOnDestroy();
  }));
  it('detalhes consultam os campos completos da peca', () => {c.openDetalhes(peca);expect(s.getById).toHaveBeenCalledWith(43);expect(c.chamadoSelecionado?.peca?.quantidade).toBe(1.5);});
});

describe('Dropdown de atribuicao na tabela da manutencao', () => {
  let fixture:ComponentFixture<FilaChamadosComponent>;let s:jasmine.SpyObj<FilaManutencaoService>;
  beforeEach(async () => {
    s=jasmine.createSpyObj('FilaManutencaoService',['getPagina','getById','atribuirMecanico','concluir','mecanicos']);
    s.getPagina.and.returnValue(of({status:200,itens:[manutencao,peca,{...manutencao,id:44,status:'CONCLUIDA'}],pagina:0,tamanho:8,total:3}));
    await TestBed.configureTestingModule({imports:[FilaChamadosComponent],providers:[provideRouter([]),{provide:AuthService,useValue:{logout:()=>{}}},{provide:FilaManutencaoService,useValue:s},{provide:MecanicoService,useValue:{lookup:()=>of([{id:7,nome:'Carlos cadastrado'},{id:8,nome:'Ana cadastrada'}])}}]}).compileComponents();
    s.mecanicos.and.returnValue(of([{id:7,nome:'Carlos cadastrado'},{id:8,nome:'Ana cadastrada'}]));
    TestBed.inject(SessaoState).usuario.set({id:1,nome:'Operador autorizado',email:'teste@example.test',administrador:true,perfis:[],permissoes:[],trocarSenha:false}); fixture=TestBed.createComponent(FilaChamadosComponent);
  });
  afterEach(() => fixture?.destroy());
  it('mostra dropdown somente em abertos e atribui o mecanico selecionado pelo botao da linha', fakeAsync(() => {
    fixture.detectChanges();tick();fixture.detectChanges();
    const selects=fixture.nativeElement.querySelectorAll('tbody select') as NodeListOf<HTMLSelectElement>;
    expect(selects.length).toBe(2);expect(selects[0].classList.contains('form-select')).toBeTrue();expect(selects[0].classList.contains('form-select-sm')).toBeFalse();
    const primeiraLinha=fixture.nativeElement.querySelector('tbody tr') as HTMLTableRowElement;expect(primeiraLinha.cells[6].querySelector('button')).toBeNull();expect(primeiraLinha.cells[8].querySelector('.d-flex.justify-content-end.align-items-center.gap-1')?.querySelectorAll('button').length).toBe(2);expect(Array.from(selects[0].options).map(o=>o.textContent?.trim())).toEqual([jasmine.any(String),'Carlos cadastrado','Ana cadastrada']);
    selects[0].value=selects[0].options[1].value;selects[0].dispatchEvent(new Event('change'));tick();fixture.detectChanges();expect(s.atribuirMecanico).not.toHaveBeenCalled();
    const atualizado:ItemFilaManutencao={...manutencao,status:'EM_MANUTENCAO',mecanico:{id:7,nome:'Carlos cadastrado',especialidades:['PRENSA'],turno:'MANHA'}};
    s.atribuirMecanico.and.returnValue(of(atualizado));s.getPagina.and.returnValue(of({status:200,itens:[atualizado,peca],pagina:0,tamanho:8,total:2}));
    (fixture.nativeElement.querySelector('tbody button[title="Atribuir chamado"]') as HTMLButtonElement).click();tick();fixture.detectChanges();
    expect(s.atribuirMecanico).toHaveBeenCalledOnceWith(42,7);expect(fixture.nativeElement.querySelector('tbody tr').textContent).toContain('Carlos cadastrado');
    expect(fixture.nativeElement.querySelectorAll('tbody select').length).toBe(1);fixture.destroy();
  }));
});
