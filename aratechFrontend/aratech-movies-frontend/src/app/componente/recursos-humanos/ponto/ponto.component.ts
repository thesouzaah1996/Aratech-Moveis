import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subject, Subscription, debounceTime } from 'rxjs';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { FuncionarioService } from '../../../core/services/funcionario.service';
import { Funcionario } from '../../../core/models/funcionario.model';
import { SessaoState } from '../../../core/services/sessao-state.service';
import { DiaPonto, HistoricoPonto, PontoService, EscalaPonto } from '../../../core/services/ponto.service';
import { criarModal, ModalDaTela } from '../../../core/ui/modal';
@Component({ selector: 'app-ponto', standalone: true, imports: [FormsModule, DatePipe, RouterLink, NavbarComponent, BreadcrumbComponent], templateUrl: './ponto.component.html', styleUrl: './ponto.component.scss' })
export class PontoComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('ajusteModal') ajusteModalEl!: ElementRef<HTMLElement>;
  private modal?: ModalDaTela;
  private subs = new Subscription(); private consulta?: Subscription;
  private busca = new Subject<void>(); private todos: {id:number;nome:string;cargo:string;setor:string}[] = [];
  breadcrumb: BreadcrumbItem[] = [];
  buscaNome = ''; resultados: {id:number;nome:string;cargo:string;setor:string}[] = []; pesquisando = false; pronto = false;
  proprio = false; id = 0; nome = ''; cargo = ''; setor = '';
  mes = this.hoje().slice(0, 7); dias: DiaPonto[] = []; loading = false; erro = ''; sucesso = '';
  selecionado: DiaPonto | null = null; motivo = ''; erroModal = ''; salvando = false;
  historico: HistoricoPonto[] = []; carregandoHistorico = false;
  escala: EscalaPonto[] = []; editandoEscala = false; salvandoEscala = false;
  readonly nomesDias = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo'];
  configurarEscala(): void {
    this.subs.add(this.ponto.escala(this.id).subscribe({next:dias=>{
      this.escala=Array.from({length:7},(_,i)=>dias.find(d=>d.diaSemana===i+1)??{diaSemana:i+1,horarios:['','','','','',''],extraMinutos:0});this.editandoEscala=true;
    },error:e=>this.erro=this.mensagem(e)}));
  }
  gravarEscala(): void {
    if(this.salvandoEscala) return;
    this.salvandoEscala=true;this.erro='';
    this.subs.add(this.ponto.salvarEscala(this.id,this.escala).subscribe({next:()=>{this.salvandoEscala=false;this.editandoEscala=false;this.carregar();},error:e=>{this.salvandoEscala=false;this.erro=this.mensagem(e);}}));
  }
  readonly colunas = ['Entrada 1', 'Saída 1', 'Entrada 2', 'Saída 2', 'Entrada 3', 'Saída 3'];
  constructor(private funcionarios: FuncionarioService, private ponto: PontoService, private route: ActivatedRoute, public sessao: SessaoState) {}
  ngOnInit(): void {
    this.subs.add(this.busca.pipe(debounceTime(300)).subscribe(() => this.filtrar()));
    this.subs.add(this.route.paramMap.subscribe(params => {
      this.proprio = this.route.snapshot.routeConfig?.path === 'meu-ponto';
      this.id = this.proprio ? this.sessao.usuario()?.id ?? 0 : Number(params.get('id') ?? 0);
      this.breadcrumb = [{ label: 'Início', route: '/dashboard' }, ...(this.proprio ? [] : [{ label: 'Recursos Humanos', route: '/recursos-humanos' }]), { label: this.proprio ? 'Meu ponto' : 'Controle de Ponto', ...(this.id && !this.proprio ? { route: '/recursos-humanos/ponto' } : {}) }];
      if (this.proprio) {
        this.nome = this.sessao.usuario()?.nome ?? '';
        if (this.id === 0) { this.erro = 'A conta técnica não possui vínculo de funcionário nem registros de ponto.'; return; }
        this.carregar();
      } else if (this.id) {
        this.subs.add(this.ponto.funcionario(this.id).subscribe({ next: f => { this.nome = f.nome; this.cargo = f.cargo; this.setor = f.setor; this.breadcrumb.push({ label: f.nome }); }, error: e => this.erro = this.mensagem(e) })); this.carregar();
      } else { this.carregarFuncionarios(); }
    }));
  }
  ngAfterViewInit(): void { this.modal = criarModal(this.ajusteModalEl.nativeElement); }
  carregarFuncionarios(): void {
    this.erro = ''; this.pesquisando = true;
    this.subs.add(this.ponto.funcionarios().subscribe({ next: fs => { this.todos = fs; this.pronto = true; this.pesquisando = false; this.filtrar(); }, error: e => { this.pesquisando = false; this.erro = this.mensagem(e); } }));
  }
  buscar(): void { this.resultados = []; this.pesquisando = !!this.buscaNome.trim(); this.busca.next(); }
  private filtrar(): void {
    const termo = this.buscaNome.trim().toLocaleLowerCase('pt-BR');
    this.resultados = termo ? this.todos.filter(f => f.nome.toLocaleLowerCase('pt-BR').includes(termo) || String(f.id).startsWith(termo)) : [];
    this.pesquisando = !this.pronto && !!termo;
  }
  carregar(): void {
    this.consulta?.unsubscribe(); this.erro = ''; this.sucesso = ''; this.dias = [];
    if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(this.mes)) { this.erro = 'Selecione um mês válido.'; return; }
    this.loading = true;
    this.consulta = this.ponto.listar(this.id, this.mes, this.proprio).subscribe({ next: registros => {
      const [ano, mes] = this.mes.split('-').map(Number);
      this.dias = Array.from({ length: new Date(ano, mes, 0).getDate() }, (_, i) => {
        const data = `${this.mes}-${String(i + 1).padStart(2, '0')}`;
        return registros.find(d => d.data === data) ?? { data, horarios: ['', '', '', '', '', ''], versao: 0, ajustado: false };
      }); this.loading = false;
    }, error: e => { this.loading = false; this.erro = this.mensagem(e); } });
  }
  get podeEditar(): boolean { return !!this.sessao.usuario()?.perfis.includes('#') && this.sessao.permite('RH_PONTO.ALTERAR'); }
  podeEditarDia(d: DiaPonto): boolean { return this.podeEditar && d.data <= this.hoje(); }
  diaSemana(data: string): string { return new Date(data + 'T12:00:00').toLocaleDateString('pt-BR', { weekday: 'long' }); }
  incompleto(d: DiaPonto): boolean { return d.horarios.filter(Boolean).length % 2 !== 0; }
  status(d: DiaPonto): string { return this.incompleto(d) ? 'Incompleto' : 'Registrado'; }
  total(d: DiaPonto): number { let m=0; for(let i=0;i<6;i+=2) if(d.horarios[i] && d.horarios[i+1]) m+=this.minutos(d.horarios[i+1])-this.minutos(d.horarios[i]); return m; }
  horas(m: number): string { return `${String(Math.floor(m/60)).padStart(2,'0')}:${String(m%60).padStart(2,'0')}`; }
  get totalMes(): string { return this.horas(this.dias.reduce((s,d)=>s+this.total(d),0)); }
  get diasMarcados(): number { return this.dias.filter(d=>d.horarios.some(Boolean)).length; }
  get incompletos(): number { return this.dias.filter(d=>d.horarios.filter(Boolean).length%2!==0).length; }
  abrir(dia: DiaPonto): void {
    if(this.salvando) return;
    this.selecionado={...dia,horarios:[...dia.horarios]}; this.motivo=''; this.erroModal=''; this.historico=[]; this.carregandoHistorico=true; this.modal?.show();
    this.subs.add(this.ponto.historico(this.id,dia.data,this.proprio).subscribe({ next:h=>{if(this.selecionado?.data===dia.data){this.historico=h;this.carregandoHistorico=false;}}, error:e=>{if(this.selecionado?.data===dia.data){this.carregandoHistorico=false;this.erroModal=this.mensagem(e);}} }));
  }
  fechar(): void { if(!this.salvando){this.modal?.hide();this.selecionado=null;} }
  salvar(): void {
    if(this.salvando || !this.selecionado || !this.podeEditarDia(this.selecionado)) return;
    if(this.motivo.trim().length<5){this.erroModal='Informe o motivo da alteração (5 a 500 caracteres).';return;}
    this.salvando=true; this.erroModal='';
    this.subs.add(this.ponto.salvar(this.id,this.selecionado,this.motivo.trim()).subscribe({next:d=>{this.dias=this.dias.map(dia=>dia.data===d.data?d:dia);this.salvando=false;this.fechar();this.sucesso='Marcação alterada com sucesso.';},error:e=>{this.salvando=false;this.erroModal=this.mensagem(e);}}));
  }
  textoHorarios(v:string):string{return v.split(',').map(h=>h||'—').join(' · ');}
  private minutos(h:string):number{const [a,b]=h.split(':').map(Number);return a*60+b;}
  private hoje():string{const d=new Date();return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;}
  private mensagem(e:any):string{return e.error?.mensagem??'Não foi possível carregar os dados. Tente novamente.';}
  ngOnDestroy():void{this.consulta?.unsubscribe();this.subs.unsubscribe();this.busca.complete();this.modal?.dispose();}
}
