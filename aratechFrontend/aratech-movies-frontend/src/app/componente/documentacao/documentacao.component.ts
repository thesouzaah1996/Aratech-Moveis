import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../navbar/navbar.component';
import { GUIA_SETORES } from './guia-sistema';
import { SerraIconComponent } from '../fabricacao/serra-icon.component';
import { FuradeiraIconComponent } from '../fabricacao/furadeira-icon.component';
import { BreadcrumbComponent } from '../breadcrumb/breadcrumb.component';

@Component({
  selector: 'app-documentacao',
  standalone: true,
  imports: [FormsModule, NavbarComponent, BreadcrumbComponent, SerraIconComponent, FuradeiraIconComponent],
  templateUrl: './documentacao.component.html',
  styleUrl: './documentacao.component.scss'
})
export class DocumentacaoComponent {
  readonly breadcrumb = [{ label: 'Início', route: '/dashboard' }, { label: 'Documentação do Sistema' }];
  busca = '';
  setor = '';
  readonly setores = GUIA_SETORES;

  get resultados() {
    const termo = this.normalizar(this.busca.trim());
    return this.setores.filter(item => (!this.setor || item.id === this.setor)
      && (!termo || this.normalizar([item.nome, item.resumo, item.telas, ...item.passos, ...item.cuidados, item.exemplo].join(' ')).includes(termo)));
  }

  limpar() { this.busca = ''; this.setor = ''; }

  private normalizar(texto: string) {
    return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
  }
}
