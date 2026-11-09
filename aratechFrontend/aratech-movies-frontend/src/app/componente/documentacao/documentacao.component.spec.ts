import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { DocumentacaoComponent } from './documentacao.component';

@Component({ selector: 'app-navbar', standalone: true, template: '' })
class NavbarTesteComponent {}

describe('Documentação do Sistema', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DocumentacaoComponent], providers: [provideRouter([])]
    }).overrideComponent(DocumentacaoComponent, {
      remove: { imports: [NavbarComponent] }, add: { imports: [NavbarTesteComponent] }
    }).compileComponents();
  });

  it('apresenta os 17 módulos no padrão da aplicação com breadcrumb e orientações', () => {
    const fixture = TestBed.createComponent(DocumentacaoComponent);
    fixture.detectChanges();
    const tela: HTMLElement = fixture.nativeElement;
    expect(tela.querySelector('main.app-main app-breadcrumb')).not.toBeNull();
    expect(tela.querySelectorAll('article.setor.card').length).toBe(17);
    expect(tela.querySelector('.abertura')).toBeNull();
    expect(tela.querySelector('h1')).toBeNull();
    expect(tela.querySelectorAll('.exemplo').length).toBe(17);
  });

  it('busca pelo campo sem exigir acentos ou letras maiúsculas', async () => {
    const fixture = TestBed.createComponent(DocumentacaoComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    const campo: HTMLInputElement = fixture.nativeElement.querySelector('#busca-guia');
    campo.value = 'FERIAS';
    campo.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(fixture.componentInstance.resultados.map(item => item.id)).toEqual(['rh']);
    expect(fixture.nativeElement.querySelectorAll('article.setor').length).toBe(1);
  });

  it('combina setor e busca e permite recuperar a lista vazia', () => {
    const fixture = TestBed.createComponent(DocumentacaoComponent);
    fixture.componentInstance.setor = 'portaria';
    fixture.componentInstance.busca = 'pagamento';
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.vazio')).not.toBeNull();
    fixture.nativeElement.querySelector('.vazio button').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('article.setor').length).toBe(17);
    expect(fixture.componentInstance.busca).toBe('');
    expect(fixture.componentInstance.setor).toBe('');
  });

  it('abre as orientações ao filtrar Furação e utiliza o ícone de furadeira', () => {
    const fixture = TestBed.createComponent(DocumentacaoComponent);
    fixture.componentInstance.setor = 'furacao';
    fixture.detectChanges();
    const modulo: HTMLElement = fixture.nativeElement.querySelector('article.setor');
    expect(modulo.querySelector('app-furadeira-icon')).not.toBeNull();
    expect(modulo.querySelector('details')!.open).toBeTrue();
    expect(modulo.textContent).toContain('revisão congelada');
    expect(modulo.textContent).toContain('300 laterais');
  });

  it('encontra os fluxos novos e distingue o consumo de material da operação', () => {
    const fixture = TestBed.createComponent(DocumentacaoComponent);
    const c = fixture.componentInstance;
    c.busca = 'VARIANTES';
    expect(c.resultados.map(item => item.id)).toContain('variantes');
    c.setor = 'almoxarifado';
    c.busca = 'conferir e consumir';
    fixture.detectChanges();
    expect(c.resultados.map(item => item.id)).toEqual(['almoxarifado']);
    expect(fixture.nativeElement.querySelector('article.setor').textContent).toContain('Liberar ou iniciar a separação não baixa saldo.');
  });

  it('mantém os módulos recolhidos até abrir uma orientação ou usar filtros', () => {
    const fixture = TestBed.createComponent(DocumentacaoComponent);
    fixture.detectChanges();
    const detalhes: HTMLDetailsElement = fixture.nativeElement.querySelector('article.setor details');
    expect(detalhes.open).toBeFalse();
    detalhes.querySelector('summary')!.click();
    fixture.detectChanges();
    expect(detalhes.open).toBeTrue();
  });
});
