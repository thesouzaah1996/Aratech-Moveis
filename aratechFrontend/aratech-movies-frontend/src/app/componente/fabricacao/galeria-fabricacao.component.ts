import { Component,Input,Output,EventEmitter,OnChanges,OnDestroy,ViewChild,ElementRef,inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FotoFabricacao } from '../../core/models/fabricacao.model';
import { FabricacaoService } from '../../core/services/fabricacao.service';

@Component({selector:'app-galeria-fabricacao',standalone:true,imports:[FormsModule],template:`
  <div class="galeria-fotos">
    @for(f of fotos;track f.id){<div class="foto-mini"><button type="button" class="abrir-foto" (click)="ampliar(f)" [attr.aria-label]="'Ampliar foto de '+titulo"><img [src]="api.urlFoto(modeloId,f.id,contexto)" [alt]="f.legenda || titulo" loading="lazy"></button>@if(editavel){<button type="button" class="retirar-foto" [disabled]="ocupado" (click)="retirar.emit(f.id)" [attr.aria-label]="'Retirar foto de '+titulo"><i class="bi bi-x"></i></button>}@if(f.legenda){<small>{{ f.legenda }}</small>}</div>}
    @if(!fotos.length){<span class="sem-foto"><i class="bi bi-camera"></i><small>Sem fotos</small></span>}
  </div>
  @if(editavel && fotos.length<4){<label class="btn btn-sm btn-outline-secondary mt-2"><i class="bi bi-camera me-1"></i>Adicionar foto<input type="file" class="visually-hidden" accept="image/jpeg,image/png" [disabled]="ocupado" (change)="selecionar($event)"></label><small class="d-block text-muted mt-1">JPEG ou PNG · até 5 MB · até 4 fotos</small>}
  @if(erro){<p class="text-danger small mt-2" role="alert">{{ erro }}</p>}
  @if(preview){<div class="preview-envio mt-2"><img [src]="preview" alt="Prévia da foto escolhida"><div><label class="small">Legenda<input class="form-control form-control-sm" maxlength="150" [(ngModel)]="legenda" [disabled]="ocupado"></label><div class="mt-2"><button type="button" class="btn btn-sm btn-danger me-2" [disabled]="ocupado" (click)="confirmar()">{{ ocupado?'Enviando…':'Enviar foto' }}</button><button type="button" class="btn btn-sm btn-light" [disabled]="ocupado" (click)="limpar()">Cancelar</button></div></div></div>}
  <dialog #dialogo class="zoom-foto"><div class="d-flex justify-content-between align-items-center mb-2"><strong>{{ titulo }}</strong><button type="button" class="btn-close ms-3" aria-label="Fechar imagem" (click)="dialogo.close()"></button></div>@if(ampliada){<img [src]="api.urlFoto(modeloId,ampliada.id,contexto)" [alt]="ampliada.legenda || titulo"><p class="small mb-0 mt-2">{{ ampliada.legenda }}</p>}</dialog>
`})
export class GaleriaFabricacaoComponent implements OnChanges,OnDestroy {
  @Input() modeloId=0;@Input() fotos:FotoFabricacao[]=[];@Input() titulo='Componente';@Input() contexto:'administracao'|'pcp'|'corte'|'embalagem'='administracao';@Input() editavel=false;@Input() ocupado=false;
  @Output() enviar=new EventEmitter<{arquivo:File;legenda:string}>();@Output() retirar=new EventEmitter<number>();
  @ViewChild('dialogo') dialogo!:ElementRef<HTMLDialogElement>;
  readonly api=inject(FabricacaoService);arquivo?:File;preview='';legenda='';erro='';ampliada?:FotoFabricacao;
  private ids='';
  ngOnChanges():void{const ids=this.fotos.map(f=>f.id).join(',');if(ids!==this.ids){this.ids=ids;this.limpar();}}
  selecionar(event:Event):void{const input=event.target as HTMLInputElement;const arquivo=input.files?.[0];input.value='';if(!arquivo)return;this.limpar();if(!['image/png','image/jpeg'].includes(arquivo.type)||arquivo.size>5*1024*1024){this.erro='Escolha JPEG ou PNG de até 5 MB.';return;}this.arquivo=arquivo;this.preview=URL.createObjectURL(arquivo);}
  confirmar():void{if(this.arquivo&&!this.ocupado)this.enviar.emit({arquivo:this.arquivo,legenda:this.legenda});}
  ampliar(foto:FotoFabricacao):void{this.ampliada=foto;this.dialogo.nativeElement.showModal();}
  limpar():void{if(this.preview)URL.revokeObjectURL(this.preview);this.preview='';this.arquivo=undefined;this.legenda='';this.erro='';}
  ngOnDestroy():void{this.limpar();}
}
