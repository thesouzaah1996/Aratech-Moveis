import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription, debounceTime } from 'rxjs';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { PerfilService } from '../../../core/services/perfil.service';
import { UsuarioPerfil } from '../../../core/models/perfil.model';
import { criarModal, ModalDaTela } from '../../../core/ui/modal';

@Component({
  selector: 'app-atribuir-perfil', standalone: true, imports: [FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './atribuir-perfil.component.html', styleUrl: './perfil.component.scss'
})
export class AtribuirPerfilComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('atribuirModal') atribuirModalEl!: ElementRef<HTMLElement>;
  private modal?: ModalDaTela;
  private busca = new Subject<void>();
  private buscaSub?: Subscription;
  private request?: Subscription;
  private mutation?: Subscription;
  breadcrumb: BreadcrumbItem[] = [{ label: 'Início', route: '/dashboard' }, { label: 'Recursos Humanos', route: '/recursos-humanos' }, { label: 'Perfis de Acesso', route: '/recursos-humanos/perfil' }, { label: 'Atribuir Perfil' }];
  usuarios: UsuarioPerfil[] = [];
  selecionado: UsuarioPerfil | null = null;
  searchQuery = ''; nomePerfil = ''; errorMessage = ''; modalError = ''; successMessage = '';
  loading = false; salvando = false; pesquisou = false;
  senhaTemporaria = ''; criandoAcesso = false; erroAcesso = '';
  page = 1; pageSize = 8; total = 0;
  constructor(private service: PerfilService) {}
  ngOnInit(): void { this.buscaSub = this.busca.pipe(debounceTime(300)).subscribe(() => this.carregar()); }
  ngAfterViewInit(): void { this.modal = criarModal(this.atribuirModalEl.nativeElement); }
  onSearch(): void {
    this.page = 1; this.request?.unsubscribe(); this.usuarios = []; this.total = 0; this.errorMessage = ''; this.successMessage = '';
    this.loading = !!this.searchQuery.trim(); this.busca.next();
  }
  carregar(): void {
    this.request?.unsubscribe();
    if (!this.searchQuery.trim()) { this.usuarios = []; this.total = 0; this.loading = false; this.pesquisou = false; return; }
    this.loading = true; this.errorMessage = ''; this.pesquisou = true;
    this.request = this.service.buscarUsuarios(this.searchQuery, this.page - 1, this.pageSize).subscribe({
      next: p => { this.usuarios = p.content; this.total = p.totalElements; this.loading = false; },
      error: e => { this.usuarios = []; this.total = 0; this.loading = false; this.errorMessage = this.erro(e); }
    });
  }
  abrirAtribuicao(usuario: UsuarioPerfil): void {
    if (this.salvando || this.criandoAcesso) return;
    this.selecionado = usuario; this.nomePerfil = ''; this.modalError = ''; this.senhaTemporaria = ''; this.erroAcesso = ''; this.modal?.show();
  }
  fechar(): void { if (!this.salvando && !this.criandoAcesso) this.modal?.hide(); }
  criarAcesso(): void {
    if (!this.selecionado || this.criandoAcesso || this.salvando) return;
    this.criandoAcesso = true; this.erroAcesso = '';
    this.mutation = this.service.criarAcesso(this.selecionado.id).subscribe({
      next: r => { this.senhaTemporaria = r.senhaTemporaria; this.criandoAcesso = false; },
      error: e => { this.criandoAcesso = false; this.erroAcesso = this.erro(e); }
    });
  }
  confirmar(): void {
    if (this.salvando || this.criandoAcesso || !this.selecionado) return;
    const nome = this.nomePerfil.trim();
    if (nome !== '#' && !/^[\p{L}\p{N} _-]{3,100}$/u.test(nome)) { this.modalError = 'Informe o nome de um perfil cadastrado (3 a 100 caracteres, ou #).'; return; }
    this.salvando = true; this.modalError = '';
    this.mutation = this.service.atribuir(this.selecionado.id, nome).subscribe({
      next: usuario => {
        this.usuarios = this.usuarios.map(u => u.id === usuario.id ? usuario : u);
        this.selecionado = usuario; this.salvando = false; this.errorMessage = '';
        this.successMessage = `Perfil atribuído a ${usuario.nome} com sucesso.`; this.modal?.hide();
      },
      error: e => { this.salvando = false; this.modalError = this.erro(e); }
    });
  }
  get totalPages(): number { return Math.ceil(this.total / this.pageSize); }
  get inicio(): number { return this.total ? (this.page - 1) * this.pageSize + 1 : 0; }
  get fim(): number { return Math.min(this.page * this.pageSize, this.total); }
  get pages(): number[] { const inicio = Math.max(1, Math.min(this.page - 2, this.totalPages - 4)); return Array.from({ length: Math.min(5, this.totalPages) }, (_, i) => inicio + i); }
  setPage(p: number): void { if (p < 1 || p > this.totalPages || this.loading) return; this.page = p; this.carregar(); }
  private erro(e: any): string { return Object.values(e.error?.erros ?? {}).join(' ') || e.error?.mensagem || 'Não foi possível concluir a operação. Tente novamente.'; }
  ngOnDestroy(): void { this.request?.unsubscribe(); this.mutation?.unsubscribe(); this.buscaSub?.unsubscribe(); this.busca.complete(); this.modal?.dispose(); }
}
