import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { NavbarComponent } from '../../navbar/navbar.component';
import { PontoService, DiaPonto } from '../../../core/services/ponto.service';
import { SessaoState } from '../../../core/services/sessao-state.service';

@Component({
  selector: 'app-marcacao-ponto', standalone: true,
  imports: [NavbarComponent],
  template: `
    <app-navbar />
    <main class="container py-5" style="max-width: 620px">
      <section class="card shadow-sm mt-3"><div class="card-body p-4">
        <h1 class="h4 text-center">Marcação de ponto</h1>
        <div class="text-center my-4">
          <i class="bi bi-clock fs-1" aria-hidden="true"></i>
          <div class="display-4 font-monospace">{{ horaExibida }}</div>
          <div>{{ dataExibida }}</div><small class="text-muted">Horário de Brasília</small>
        </div>
        @if (erro) { <div class="alert alert-danger" role="alert">{{ erro }}</div> }
        @if (sucesso) { <div class="alert alert-success" role="status">{{ sucesso }}</div> }
        @if (aviso) { <div class="alert alert-danger" role="alert">{{ aviso }}</div> }
        @if (contaTecnica) {
          <p class="alert alert-info mb-0" role="status">Esta conta é de administração do sistema e não possui ponto. Entre com a conta de um funcionário para registrar.</p>
        } @else {
          <button type="button" class="btn btn-danger w-100" [disabled]="salvando || !pronto" (click)="marcar()">{{ salvando ? 'Registrando...' : 'Bater ponto' }}</button>
        }
        <!-- <h2 class="h6 mt-4">Marcações de hoje</h2> -->
        <!-- <div class="d-flex flex-wrap gap-2">@for (h of dia?.horarios ?? []; track $index) { <span class="badge bg-light text-dark border p-2">{{ rotulos[$index] }}: {{ h || 'Sem marcação' }}</span> }</div> -->
        <!-- <p class="small text-muted mt-3">Confira os horários antes de registrar. Cada clique confirmado registra a próxima entrada ou saída.</p> -->
        <!-- <a routerLink="/meu-ponto">Consultar meu ponto e total de horas</a> -->
      </div></section>
    </main>`
})
export class MarcacaoPontoComponent implements OnInit, OnDestroy {
  private ponto = inject(PontoService);
  private sessao = inject(SessaoState);
  private subs = new Subscription();
  private timer?: ReturnType<typeof setInterval>;
  private diferenca = 0;
  agora = new Date(); data = '';
  dia?: DiaPonto; erro = ''; sucesso = ''; aviso = ''; salvando = false; pronto = false;
  rotulos = ['Entrada 1', 'Saída 1', 'Entrada 2', 'Saída 2', 'Entrada 3', 'Saída 3'];
  get contaTecnica(): boolean { return this.sessao.usuario()?.id === 0; }
  get horaExibida(): string { return this.agora.toLocaleTimeString('pt-BR', {timeZone:'America/Sao_Paulo'}); }
  get dataExibida(): string { return this.agora.toLocaleDateString('pt-BR', {timeZone:'America/Sao_Paulo', weekday:'long', day:'2-digit', month:'long', year:'numeric'}); }
  ngOnInit(): void {
    this.sincronizar();
    this.timer = setInterval(() => {
      this.agora = new Date(Date.now() + this.diferenca);
      const data = this.agora.toLocaleDateString('en-CA', {timeZone:'America/Sao_Paulo'});
      if (this.pronto && data !== this.data) this.sincronizar();
    }, 1000);
  }
  sincronizar(): void {
    this.pronto = false;
    this.subs.add(this.ponto.relogio().subscribe({next:r=>{
      this.diferenca = Date.parse(r.instante) - Date.now(); this.agora = new Date(r.instante); this.data = r.data;
      if (this.contaTecnica) return;
      this.subs.add(this.ponto.listar(0,this.data.slice(0,7),true).subscribe({next:ds=>{
        this.dia = ds.find(d=>d.data===this.data) ?? {data:this.data, horarios:['','','','','',''], versao:0, ajustado:false}; this.pronto=true;
      },error:e=>this.erro=e.error?.mensagem??'Não foi possível consultar o ponto.'}));
    },error:e=>this.erro=e.error?.mensagem??'Não foi possível sincronizar o relógio.'}));
  }
  marcar(): void {
    if(this.salvando || !this.pronto || this.contaTecnica) return;
    this.salvando=true; this.erro=''; this.sucesso=''; this.aviso='';
    this.subs.add(this.ponto.marcar({data:this.data,versao:this.dia?.versao??0}).subscribe({next:d=>{
      this.dia=d;this.salvando=false;this.sucesso='Ponto registrado com sucesso.';this.aviso=d.aviso??'';
    },error:e=>{this.salvando=false;this.erro=e.error?.mensagem??'Não foi possível registrar o ponto.'; if(e.status===409 || e.status===400) this.sincronizar();}}));
  }
  ngOnDestroy(): void { if(this.timer) clearInterval(this.timer); this.subs.unsubscribe(); }
}
