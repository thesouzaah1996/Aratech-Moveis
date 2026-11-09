import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { PermissaoDirective } from './permissao.directive';
import { SessaoState } from './services/sessao-state.service';
import { permissaoDaRota } from './acesso.guard';
@Component({standalone:true,imports:[PermissaoDirective],template:`<button *appPermissao="'PCP_ORDENS.CADASTRAR'">Criar ordem</button><a *appPermissao="'FINANCEIRO_PAGAR.CONSULTAR'">Financeiro</a>`})
class Host {}
describe('Visibilidade de funções autorizadas',()=>{
 it('remove o elemento do DOM e reage à revogação',()=>{
   TestBed.configureTestingModule({imports:[Host]});const sessao=TestBed.inject(SessaoState);const fixture=TestBed.createComponent(Host);fixture.detectChanges();
   expect(fixture.nativeElement.querySelector('button')).toBeNull();
   sessao.usuario.set({id:1,nome:'PCP',email:'pcp@example.test',administrador:false,perfis:[],permissoes:['PCP_ORDENS.CONSULTAR','PCP_ORDENS.CADASTRAR'],trocarSenha:false});fixture.detectChanges();
   expect(fixture.nativeElement.querySelector('button')).not.toBeNull();expect(fixture.nativeElement.querySelector('a')).toBeNull();
   sessao.usuario.update(u=>({...u!,permissoes:[]}));fixture.detectChanges();expect(fixture.nativeElement.querySelector('button')).toBeNull();fixture.destroy();
 });
 it('preserva permissões amplas e bloqueia rotas de criação sem cadastrar',()=>{
   const s=new SessaoState();s.usuario.set({id:1,nome:'PCP',email:'pcp@example.test',administrador:false,perfis:[],permissoes:['PCP.CONSULTAR','PCP.ALTERAR'],trocarSenha:false});
   expect(s.permite('PCP_ORDENS.ALTERAR')).toBeTrue();expect(s.permite('PCP_ORDENS.CADASTRAR')).toBeFalse();expect(s.permite('COMPRAS_PEDIDOS.CONSULTAR')).toBeFalse();
   expect(permissaoDaRota('manutencao/solicitar-peca')).toBe('MANUTENCAO_PECAS.CADASTRAR');expect(permissaoDaRota('financeiro/contas-pagar')).toBe('FINANCEIRO_PAGAR.CONSULTAR');
 });
});
