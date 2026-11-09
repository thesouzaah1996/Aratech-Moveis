import { TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { MarcacaoPontoComponent } from './marcacao-ponto.component';
import { DiaPonto, PontoService } from '../../../core/services/ponto.service';
import { SessaoState } from '../../../core/services/sessao-state.service';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

describe('Marcação de ponto', () => {
  let componente: MarcacaoPontoComponent;
  let ponto: jasmine.SpyObj<PontoService>;
  let sessao: SessaoState;
  const dia: DiaPonto = {data:'2026-11-07',horarios:['08:00','','','','',''],versao:1,ajustado:true};
  beforeEach(() => {
    ponto=jasmine.createSpyObj('PontoService',['relogio','listar','marcar']);
    ponto.relogio.and.returnValue(of({instante:'2026-11-07T11:00:00Z',data:'2026-11-07'}));
    ponto.listar.and.returnValue(of([dia]));
    TestBed.configureTestingModule({providers:[{provide:PontoService,useValue:ponto},provideRouter([]),{provide:AuthService,useValue:{}}]});
    sessao=TestBed.inject(SessaoState);
    sessao.usuario.set({id:7,nome:'Ana Maria Silva',email:'ana@aratechmoveis',administrador:false,perfis:[],permissoes:[],trocarSenha:false});
    componente=TestBed.runInInjectionContext(()=>new MarcacaoPontoComponent());
    componente.ngOnInit();
  });
  afterEach(()=>componente.ngOnDestroy());
  it('sincroniza hora e versão com o servidor',()=>{
    expect(componente.horaExibida).toBe('08:00:00');
    expect(componente.dia?.versao).toBe(1);
    expect(componente.pronto).toBeTrue();
  });
  it('marca com a sessão sem credenciais e evita envio duplicado',()=>{
    const resposta=new Subject<DiaPonto>();ponto.marcar.and.returnValue(resposta);
    componente.marcar();componente.marcar();
    expect(ponto.marcar).toHaveBeenCalledTimes(1);
    expect(ponto.marcar.calls.mostRecent().args[0]).toEqual({data:'2026-11-07',versao:1});
    resposta.next({...dia,versao:2,aviso:'Limite ultrapassado'});
    expect(componente.aviso).toContain('Limite');
    expect(componente.salvando).toBeFalse();
  });
  it('reconsulta o dia após conflito sem reenviar a marcação',()=>{
    const resposta=new Subject<DiaPonto>();ponto.marcar.and.returnValue(resposta);
    componente.marcar();resposta.error({status:409,error:{mensagem:'Ponto atualizado'}});
    expect(componente.erro).toBe('Ponto atualizado');
    expect(ponto.listar).toHaveBeenCalledTimes(2);expect(ponto.marcar).toHaveBeenCalledTimes(1);
  });
  it('não consulta funcionário nem permite marcação para a conta técnica',()=>{
    sessao.usuario.update(u=>({...u!,id:0,administrador:true}));
    ponto.listar.calls.reset();componente.sincronizar();componente.marcar();
    expect(componente.contaTecnica).toBeTrue();expect(ponto.listar).not.toHaveBeenCalled();expect(ponto.marcar).not.toHaveBeenCalled();
  });
  it('mostra nome e sobrenome e permite bater ponto sem formulário nem link de início',()=>{
    ponto.marcar.and.returnValue(of({...dia,versao:2}));
    const fixture=TestBed.createComponent(MarcacaoPontoComponent);
    fixture.detectChanges();
    const tela:HTMLElement=fixture.nativeElement;
    expect(tela.querySelector('.profile-info span')?.textContent?.trim()).toBe('Ana Silva');
    expect(tela.querySelector('main input')).toBeNull();
    expect(tela.querySelector('main a[href="/dashboard"]')).toBeNull();
    (tela.querySelector('main button') as HTMLButtonElement).click();
    expect(ponto.marcar).toHaveBeenCalledOnceWith({data:'2026-11-07',versao:1});
    fixture.destroy();
  });
});
