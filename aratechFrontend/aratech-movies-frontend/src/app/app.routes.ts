import { Routes } from '@angular/router';
import { LoginComponent } from './componente/login/login.component';
import { DashboardComponent } from './componente/dashboard/dashboard.component';
import { AlmoxarifadoComponent } from './componente/almoxarifado/almoxarifado.component';
import { EstoqueProdutosComponent } from './componente/almoxarifado/estoque-produtos/estoque-produtos.component';
import { CategoriasComponent } from './componente/almoxarifado/categorias/categorias.component';
import { FornecedoresComponent } from './componente/almoxarifado/fornecedores/fornecedores.component';
import { AguardandoDescargaComponent } from './componente/almoxarifado/aguardando-descarga/aguardando-descarga.component';
import { PortariaComponent } from './componente/portaria/portaria.component';
import { BaixaNotasComponent } from './componente/portaria/baixa-notas/baixa-notas.component';
import { ControleAcessoComponent } from './componente/portaria/controle-acesso/controle-acesso.component';
import { CarregamentoComponent } from './componente/carregamento/carregamento.component';
import { CarregamentoAssistenciaComponent } from './componente/carregamento/assistencia/assistencia.component';
import { CarregamentoAguardandoDescargaComponent } from './componente/carregamento/aguardando-descarga/aguardando-descarga.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login',                                  component: LoginComponent },
  { path: 'dashboard',                              component: DashboardComponent },

  { path: 'almoxarifado',                           component: AlmoxarifadoComponent },
  { path: 'almoxarifado/estoque-produtos',          component: EstoqueProdutosComponent },
  { path: 'almoxarifado/categorias',                component: CategoriasComponent },
  { path: 'almoxarifado/fornecedores',              component: FornecedoresComponent },
  { path: 'almoxarifado/aguardando-descarga',       component: AguardandoDescargaComponent },

  { path: 'portaria',                               component: PortariaComponent },
  { path: 'portaria/baixa-notas',                   component: BaixaNotasComponent },
  { path: 'portaria/controle-acesso',               component: ControleAcessoComponent },

  { path: 'carregamento',                           component: CarregamentoComponent },
  { path: 'carregamento/assistencia',               component: CarregamentoAssistenciaComponent },
  { path: 'carregamento/aguardando-descarga',       component: CarregamentoAguardandoDescargaComponent },
];
