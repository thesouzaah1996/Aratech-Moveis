import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { FooterComponent } from '../../footer/footer.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../breadcrumb/breadcrumb.component';

export interface Parametro {
  chave: string;
  descricao: string;
  valor: string;
  tipo: 'texto' | 'numero' | 'booleano';
  editando: boolean;
}

@Component({
  selector: 'app-parametros',
  standalone: true,
  imports: [FormsModule, NavbarComponent, FooterComponent, BreadcrumbComponent],
  templateUrl: './parametros.component.html',
  styleUrl: './parametros.component.scss'
})
export class ParametrosComponent {
  breadcrumb: BreadcrumbItem[] = [
    { label: 'Início', route: '/dashboard' },
    { label: 'Administração', route: '/administracao' },
    { label: 'Parâmetros do Sistema' }
  ];

  successMessage = '';

  parametros: Parametro[] = [
    { chave: 'estoque.alerta_minimo',       descricao: 'Qtd mínima de estoque antes do alerta',    valor: '5',     tipo: 'numero',    editando: false },
    { chave: 'compras.valor_aprovacao',      descricao: 'Valor máximo sem necessidade de aprovação', valor: '1000',  tipo: 'numero',    editando: false },
    { chave: 'ponto.tolerancia_minutos',     descricao: 'Tolerância de atraso em minutos',           valor: '10',    tipo: 'numero',    editando: false },
    { chave: 'lotes.despacho_automatico',    descricao: 'Despachar lote automaticamente ao finalizar',valor: 'false', tipo: 'booleano',  editando: false },
    { chave: 'portaria.acesso_24h',          descricao: 'Portaria opera 24 horas',                   valor: 'false', tipo: 'booleano',  editando: false },
    { chave: 'sistema.empresa',              descricao: 'Nome da empresa exibido no sistema',         valor: 'Aratech Móveis', tipo: 'texto', editando: false },
  ];

  editar(p: Parametro): void { p.editando = true; }

  salvar(p: Parametro): void {
    p.editando = false;
    this.showSuccess(`Parâmetro "${p.chave}" salvo com sucesso!`);
  }

  cancelar(p: Parametro, valorOriginal: string): void {
    p.valor = valorOriginal;
    p.editando = false;
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }
}
