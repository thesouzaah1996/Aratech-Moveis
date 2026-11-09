import { Directive, Input, TemplateRef, ViewContainerRef, effect, inject, signal } from '@angular/core';
import { SessaoState } from './services/sessao-state.service';
@Directive({selector:'[appPermissao]',standalone:true})
export class PermissaoDirective {
  private readonly acesso=inject(SessaoState);
  private readonly template=inject(TemplateRef<unknown>);
  private readonly container=inject(ViewContainerRef);
  private readonly codigo=signal<string|string[]>('');
  private visivel=false;
  @Input() set appPermissao(codigo:string|string[]){this.codigo.set(codigo);}
  constructor(){effect(()=>{
    const valor=this.codigo();const permitido=(Array.isArray(valor)?valor:[valor]).some(c=>!!c&&this.acesso.permite(c));
    if(permitido===this.visivel)return;
    this.container.clear();if(permitido)this.container.createEmbeddedView(this.template);this.visivel=permitido;
  });}
}
