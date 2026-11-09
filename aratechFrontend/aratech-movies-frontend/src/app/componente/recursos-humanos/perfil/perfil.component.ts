import { Component,OnInit,AfterViewInit,OnDestroy,ViewChild,ElementRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription,Subject,debounceTime,forkJoin } from 'rxjs';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent,BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { PerfilService } from '../../../core/services/perfil.service';
import { Perfil } from '../../../core/models/perfil.model';
declare const bootstrap:any;

@Component({selector:'app-perfil',standalone:true,imports:[FormsModule,NavbarComponent,BreadcrumbComponent],templateUrl:'./perfil.component.html',styleUrl:'./perfil.component.scss'})
export class PerfilComponent implements OnInit,AfterViewInit,OnDestroy {
  @ViewChild('perfilModal') perfilModalEl!:ElementRef;
  @ViewChild('permissoesModal') permissoesModalEl!: ElementRef;
  private permissoesModal?: any;
  private permissoesRequest?: Subscription;
  perfilPermissoes: Perfil | null = null;
  permissoesSelecionadas = new Set<string>();
  catalogo: string[] = [];
  carregandoPermissoes = false;
  permissoesCarregadas = false;
  salvandoPermissoes = false;
  erroPermissoes = '';
  readonly acoesPermissao = ['CONSULTAR', 'CADASTRAR', 'ALTERAR', 'FINALIZAR'];
  filtroSetorPermissao='ALMOXARIFADO';
  readonly nomesModulos: Record<string, string> = { ALMOXARIFADO: 'Almoxarifado', PORTARIA: 'Portaria', CARREGAMENTO: 'Carregamento', MANUTENCAO: 'Manutenção', ASSISTENCIA: 'Assistência Técnica', RH: 'Recursos Humanos', PCP: 'PCP', COMPRAS: 'Compras', FINANCEIRO: 'Financeiro', ADMINISTRACAO: 'Administração', SUPORTE: 'Suporte', COMERCIAL:'Pedidos legados', CORTE:'Corte', EMBALAGEM:'Embalagem' };
  breadcrumb:BreadcrumbItem[]=[{label:'Início',route:'/dashboard'},{label:'Recursos Humanos',route:'/recursos-humanos'},{label:'Perfis de Acesso',route:'/recursos-humanos/perfil'},{label:'Criar Perfis de Acesso'}];
  perfis:Perfil[]=[];
  loading=false;salvando=false;isEditing=false;page=1;pageSize=8;total=0;
  searchQuery='';successMessage='';errorMessage='';modalError='';form:{id?:number;nome:string}={nome:''};
  private modal?:any;private request?:Subscription;private mutation?:Subscription;
  private search=new Subject<void>();private searchSub?:Subscription;
  constructor(private service:PerfilService){}
  ngOnInit():void{
    this.searchSub=this.search.pipe(debounceTime(300)).subscribe(()=>this.carregar());
    this.carregar();
  }
  ngAfterViewInit():void{this.modal=new bootstrap.Modal(this.perfilModalEl.nativeElement);this.permissoesModal=new bootstrap.Modal(this.permissoesModalEl.nativeElement);}
  ngOnDestroy():void{this.request?.unsubscribe();this.mutation?.unsubscribe();this.permissoesRequest?.unsubscribe();this.searchSub?.unsubscribe();this.search.complete();this.modal?.dispose();this.permissoesModal?.dispose();}
  nomeModulo(modulo:string):string { const [setor,...tela]=modulo.split('_'); return (this.nomesModulos[setor] || setor)+(tela.length ? ' — '+tela.join(' ').toLocaleLowerCase('pt-BR') : ' — setor inteiro'); }
  get setoresPermissao():string[]{return [...new Set(this.catalogo.filter(p=>!p.startsWith('ACESSOS.')).map(p=>p.split('.')[0].split('_')[0]))];}
  get modulosPermissao(): string[] { return [...new Set(this.catalogo.filter(p => !p.startsWith('ACESSOS.')&&p.split('.')[0].split('_')[0]===this.filtroSetorPermissao).map(p => p.split('.')[0]))]; }
  abrirPermissoes(perfil: Perfil): void {
    this.perfilPermissoes = perfil; this.erroPermissoes = ''; this.carregandoPermissoes = true; this.permissoesCarregadas = false; this.permissoesSelecionadas = new Set();
    this.permissoesModal?.show(); this.permissoesRequest?.unsubscribe();
    this.permissoesRequest = forkJoin({ catalogo: this.service.catalogoPermissoes(), atuais: this.service.permissoes(perfil.id) }).subscribe({
      next: r => { this.catalogo = r.catalogo; this.permissoesSelecionadas = new Set(r.atuais); this.carregandoPermissoes = false; this.permissoesCarregadas = true; },
      error: e => { this.carregandoPermissoes = false; this.erroPermissoes = this.erro(e); }
    });
  }
  alternarPermissao(codigo: string, marcada: boolean): void {
    if (this.salvandoPermissoes || this.carregandoPermissoes) return;
    const [modulo, acao] = codigo.split('.');
    if (marcada) { this.permissoesSelecionadas.add(codigo); if (modulo !== 'ACESSOS') this.permissoesSelecionadas.add(modulo + '.CONSULTAR'); }
    else { this.permissoesSelecionadas.delete(codigo); if (acao === 'CONSULTAR') this.acoesPermissao.forEach(a => this.permissoesSelecionadas.delete(modulo + '.' + a)); }
  }
  salvarPermissoes(): void {
    if (!this.perfilPermissoes || !this.permissoesCarregadas || this.carregandoPermissoes || this.salvandoPermissoes) return;
    this.salvandoPermissoes = true; this.erroPermissoes = '';
    this.permissoesRequest = this.service.salvarPermissoes(this.perfilPermissoes.id, [...this.permissoesSelecionadas]).subscribe({
      next: () => { this.salvandoPermissoes = false; this.permissoesModal?.hide(); this.successMessage = 'Permissões do perfil atualizadas.'; },
      error: e => { this.salvandoPermissoes = false; this.erroPermissoes = this.erro(e); }
    });
  }
  carregar():void{this.request?.unsubscribe();this.loading=true;this.request=this.service.listar(this.page-1,this.pageSize,this.searchQuery).subscribe({
    next:p=>{this.perfis=p.content;this.total=p.totalElements;this.loading=false;},error:e=>{this.loading=false;this.errorMessage=this.erro(e);}});}
  onSearch():void{this.page=1;this.search.next();}
  get totalPages():number{return Math.ceil(this.total/this.pageSize);}
  get inicio():number{return this.total ? (this.page-1)*this.pageSize+1 : 0;}
  get fim():number{return Math.min(this.page*this.pageSize,this.total);}
  get pages():number[]{const start=Math.max(1,Math.min(this.page-2,this.totalPages-4));return Array.from({length:Math.min(5,this.totalPages)},(_,i)=>start+i);}
  setPage(p:number):void{if(p<1||p>this.totalPages||this.loading)return;this.page=p;this.carregar();}
  openAdd():void{this.isEditing=false;this.form={nome:''};this.modalError='';this.modal.show();}
  openEdit(p:Perfil):void{this.isEditing=true;this.form={id:p.id,nome:p.nome};this.modalError='';this.modal.show();}
  save():void{
    if(this.salvando)return;
    const nome=this.form.nome.trim();if(!/^[\p{L}\p{N} _-]{3,100}$/u.test(nome)){this.modalError='Informe um nome de 3 a 100 caracteres, usando letras, numeros, espacos, hifen ou sublinhado.';return;}
    this.salvando=true;this.modalError='';
    const request=this.isEditing?this.service.update(this.form.id!,nome):this.service.add(nome);
    this.mutation=request.subscribe({next:()=>{this.salvando=false;this.modal.hide();this.successMessage='Perfil salvo com sucesso.';this.carregar();},error:e=>{this.salvando=false;this.modalError=this.erro(e);}});
  }
  private erro(e:any):string{return Object.values(e.error?.erros??{}).join(' ')||e.error?.mensagem||'Nao foi possivel concluir a operacao.';}
}
