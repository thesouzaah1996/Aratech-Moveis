import { SessaoState } from '../../../core/services/sessao-state.service';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { of, Subject, throwError } from 'rxjs';
import { MecanicosComponent } from './mecanicos.component';
import { MecanicoService } from '../../../core/services/mecanico.service';
import { Mecanico } from '../../../core/models/mecanico.model';
const mecanico: Mecanico = {id:7,nome:'Carlos Demo',especialidades:['PRENSA'],turno:'MANHA',ativo:true};
describe('Cadastro de mecanicos', () => {
  let c:MecanicosComponent;let s:jasmine.SpyObj<MecanicoService>;
  beforeEach(() => {
    s=jasmine.createSpyObj('MecanicoService',['getAll','add','update','enable','disable']);s.getAll.and.returnValue(of([mecanico]));c=new MecanicosComponent(s);
    for (const name of ['mecanicoModal','editModal','deleteModal']) (c as any)[name]={show:jasmine.createSpy('show'),hide:jasmine.createSpy('hide'),dispose:()=>{}};
  });
  afterEach(() => c.ngOnDestroy());
  it('lista os mecanicos persistidos e filtra ativos e inativos', () => {c.ngOnInit();expect(c.paged[0].id).toBe(7);c.setFiltroStatus('inativos');expect(c.paged.length).toBe(0);c.setFiltroStatus('ativos');expect(c.paged.length).toBe(1);});
  it('normaliza cadastro e bloqueia cliques duplicados', () => {
    const resposta=new Subject<Mecanico>();s.add.and.returnValue(resposta);c.form={nome:'  Carlos   Demo ',especialidades:['PRENSA'],turno:'MANHA'};
    c.save();c.save();expect(s.add).toHaveBeenCalledOnceWith({nome:'Carlos Demo',especialidades:['PRENSA'],turno:'MANHA'});expect(c.salvando).toBeTrue();
    resposta.next(mecanico);resposta.complete();expect(c.salvando).toBeFalse();expect((c as any).mecanicoModal.hide).toHaveBeenCalled();
  });
  it('recusa dados invalidos antes de enviar', () => {c.form={nome:'C',especialidades:[],turno:''};c.save();expect(s.add).not.toHaveBeenCalled();expect(c.modalError).toContain('3 a 150');});
  it('erro do servidor aparece no modal sem fechar ou perder os dados', () => {
    s.add.and.returnValue(throwError(()=>({error:{mensagem:'Confira o nome.'}})));c.form={nome:'Carlos Demo',especialidades:['PRENSA'],turno:'MANHA'};c.save();expect(c.modalError).toBe('Confira o nome.');expect(c.form.nome).toBe('Carlos Demo');expect((c as any).mecanicoModal.hide).not.toHaveBeenCalled();
  });
  it('edita uma copia e preserva dados ao cancelar', () => {
    c.openEditModal(mecanico);c.formEdit.nome='Carlos Editado';c.cancelarEdicao();expect(mecanico.nome).toBe('Carlos Demo');
    s.update.and.returnValue(of({...mecanico,nome:'Carlos Editado'}));c.saveEdit();expect(s.update).toHaveBeenCalledWith(7,jasmine.objectContaining({nome:'Carlos Editado'}));
  });
  it('ativa ou desativa usando o cadastro e conserva o modal se houver erro', () => {
    s.enable.and.returnValue(of(undefined));c.ativarMecanico(7);expect(s.enable).toHaveBeenCalledWith(7);
    s.disable.and.returnValue(throwError(()=>({error:{mensagem:'Tente novamente.'}})));c.openDesativar(mecanico);c.confirmDesativar();expect(c.statusError).toBe('Tente novamente.');expect((c as any).deleteModal.hide).not.toHaveBeenCalled();
  });
});

describe('Acoes de ativar e desativar na tabela de mecanicos', () => {
  let fixture:ComponentFixture<MecanicosComponent>;let service:jasmine.SpyObj<MecanicoService>;
  beforeEach(async () => {
    service=jasmine.createSpyObj('MecanicoService',['getAll','add','update','enable','disable']);service.getAll.and.returnValue(of([mecanico]));service.enable.and.returnValue(of(undefined));service.disable.and.returnValue(of(undefined));
    await TestBed.configureTestingModule({imports:[MecanicosComponent],providers:[provideRouter([]),{provide:AuthService,useValue:{logout:()=>{}}},{provide:MecanicoService,useValue:service}]}).compileComponents();
    TestBed.inject(SessaoState).usuario.set({id:1,nome:'Operador autorizado',email:'teste@example.test',administrador:true,perfis:[],permissoes:[],trocarSenha:false}); fixture=TestBed.createComponent(MecanicosComponent);
  });
  afterEach(() => {fixture?.destroy();document.querySelectorAll('.modal-backdrop').forEach(e=>e.remove());document.body.classList.remove('modal-open');document.body.style.removeProperty('overflow');document.body.style.removeProperty('padding-right');});
  it('exibe a acao ao lado de editar, confirma desativacao e permite reativar', fakeAsync(() => {
    try {
      fixture.detectChanges();tick();fixture.detectChanges();
      const acoes=fixture.nativeElement.querySelector('tbody td:last-child .d-flex') as HTMLElement;
      expect(acoes.querySelectorAll('button').length).toBe(2);expect(acoes.classList.contains('gap-1')).toBeTrue();
      (acoes.querySelector('button[title="Desativar"]') as HTMLButtonElement).click();fixture.detectChanges();tick(400);
      expect(service.disable).not.toHaveBeenCalled();
      service.getAll.and.returnValue(of([{...mecanico,ativo:false}]));
      (fixture.componentInstance.deleteModalEl.nativeElement.querySelector('button.btn-danger') as HTMLButtonElement).click();fixture.detectChanges();tick(400);fixture.detectChanges();
      expect(service.disable).toHaveBeenCalledOnceWith(7);expect(fixture.nativeElement.querySelector('tbody button[title="Ativar"]')).not.toBeNull();
      service.getAll.and.returnValue(of([mecanico]));
      (fixture.nativeElement.querySelector('tbody button[title="Ativar"]') as HTMLButtonElement).click();fixture.detectChanges();tick();fixture.detectChanges();
      expect(service.enable).toHaveBeenCalledOnceWith(7);expect(fixture.nativeElement.querySelector('tbody button[title="Desativar"]')).not.toBeNull();
    } finally {tick(3500);fixture.destroy();tick(400);}
  }));
});
