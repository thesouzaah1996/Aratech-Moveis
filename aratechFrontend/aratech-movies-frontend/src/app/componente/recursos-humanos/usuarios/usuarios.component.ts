import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';
import { FuncionarioService } from '../../../core/services/funcionario.service';
import { PerfilService } from '../../../core/services/perfil.service';
import { Funcionario } from '../../../core/models/funcionario.model';
import { Perfil } from '../../../core/models/perfil.model';

declare const bootstrap: any;

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [FormsModule, NavbarComponent, BreadcrumbComponent],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.scss'
})
export class UsuariosComponent implements OnInit, AfterViewInit {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Recursos Humanos', route: '/recursos-humanos' },
    { label: 'Permissões de Usuário' }
  ];

  searchQuery = '';
  buscando = false;
  naoEncontrado = false;
  resultados: Funcionario[] = [];
  usuarioEncontrado: Funcionario | null = null;

  perfisDisponiveis: Perfil[] = [];
  perfisSelecionados = new Set<number>();
  dropdownAberto = false;
  perfilSearchTerm = '';
  salvando = false;

  successMessage = '';
  errorMessage = '';

  @ViewChild('emailModal') emailModalEl!: ElementRef;
  private emailModal?: any;
  emailForm = { email: '' };
  emailSubmitted = false;
  salvandoEmail = false;

  constructor(
    private readonly funcionarioService: FuncionarioService,
    private readonly perfilService: PerfilService
  ) {}

  ngOnInit(): void {
    this.perfilService.getAll().subscribe({
      next: perfis => (this.perfisDisponiveis = perfis.filter(p => p.ativo)),
      error: () => (this.errorMessage = 'Não foi possível carregar os perfis disponíveis.')
    });
  }

  ngAfterViewInit(): void {
    this.emailModal = new bootstrap.Modal(this.emailModalEl.nativeElement);
  }

  buscar(): void {
    const q = this.searchQuery.trim();
    if (!q) return;

    this.buscando = true;
    this.naoEncontrado = false;
    this.usuarioEncontrado = null;
    this.resultados = [];
    this.dropdownAberto = false;
    this.errorMessage = '';

    this.funcionarioService.search(q).subscribe({
      next: funcionarios => {
        this.buscando = false;
        if (funcionarios.length === 0) {
          this.naoEncontrado = true;
        } else if (funcionarios.length === 1) {
          this.selecionarUsuario(funcionarios[0]);
        } else {
          this.resultados = funcionarios;
        }
      },
      error: () => {
        this.naoEncontrado = true;
        this.buscando = false;
      }
    });
  }

  selecionarUsuario(f: Funcionario): void {
    this.usuarioEncontrado = f;
    this.resultados = [];
    this.perfisSelecionados = new Set(f.perfis.map(p => p.id));
  }

  limparBusca(): void {
    this.searchQuery = '';
    this.usuarioEncontrado = null;
    this.naoEncontrado = false;
    this.resultados = [];
    this.dropdownAberto = false;
  }

  toggleDropdown(): void {
    this.dropdownAberto = !this.dropdownAberto;
    this.perfilSearchTerm = '';
  }

  get perfisFiltrados(): Perfil[] {
    const term = this.perfilSearchTerm.trim().toLowerCase();
    if (!term) return this.perfisDisponiveis;
    return this.perfisDisponiveis.filter(p => p.nome.toLowerCase().includes(term));
  }

  temPerfil(id: number): boolean { return this.perfisSelecionados.has(id); }

  togglePerfil(id: number): void {
    const s = new Set(this.perfisSelecionados);
    s.has(id) ? s.delete(id) : s.add(id);
    this.perfisSelecionados = s;
  }

  salvarPermissoes(): void {
    if (!this.usuarioEncontrado || this.perfisSelecionados.size === 0) return;

    this.salvando = true;
    this.errorMessage = '';

    this.funcionarioService
      .atribuirPerfis(this.usuarioEncontrado.id, [...this.perfisSelecionados])
      .subscribe({
        next: funcionario => {
          this.usuarioEncontrado = funcionario;
          this.salvando = false;
          this.dropdownAberto = false;
          this.showSuccess(`Permissões de ${funcionario.nome} atualizadas com sucesso!`);
        },
        error: err => {
          this.salvando = false;
          this.errorMessage = err.error?.mensagem ?? 'Não foi possível salvar as permissões.';
        }
      });
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3500);
  }

  abrirModalEmail(): void {
    if (!this.usuarioEncontrado) return;

    this.emailForm = { email: this.usuarioEncontrado.emailCorporativo ?? '' };
    this.emailSubmitted = false;
    this.emailModal.show();
  }

  isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
  }

  get emailErro(): string {
    const email = this.emailForm.email.trim();
    if (!email) return 'E-mail é obrigatório.';
    if (!this.isValidEmail(email)) return 'Formato de e-mail inválido.';
    return '';
  }

  salvarEmail(): void {
    this.emailSubmitted = true;
    if (this.emailErro || !this.usuarioEncontrado) return;

    this.salvandoEmail = true;
    this.errorMessage = '';

    this.funcionarioService
      .atribuirEmailCorporativo(this.usuarioEncontrado.id, this.emailForm.email.trim())
      .subscribe({
        next: funcionario => {
          this.usuarioEncontrado = funcionario;
          this.salvandoEmail = false;
          this.emailModal.hide();
          this.showSuccess('E-mail corporativo atualizado com sucesso!');
        },
        error: err => {
          this.salvandoEmail = false;
          this.errorMessage = err.error?.mensagem ?? 'Não foi possível atualizar o e-mail corporativo.';
        }
      });
  }
}
