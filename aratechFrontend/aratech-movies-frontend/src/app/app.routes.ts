import { Routes } from '@angular/router';
import { LoginComponent } from './componentes/login/login.component';
import { DashboardComponent } from './componentes/dashboard/dashboard.component';

import { AlmoxarifadoComponent } from './componentes/almoxarifado/almoxarifado.component';
import { EstoqueProdutosComponent } from './componentes/almoxarifado/estoque-produtos/estoque-produtos.component';
import { CategoriasComponent } from './componentes/almoxarifado/categorias/categorias.component';
import { FornecedoresComponent } from './componentes/almoxarifado/fornecedores/fornecedores.component';
import { AguardandoDescargaComponent } from './componentes/almoxarifado/aguardando-descarga/aguardando-descarga.component';

import { PortariaComponent } from './componentes/portaria/portaria.component';
import { BaixaNotasComponent } from './componentes/portaria/baixa-notas/baixa-notas.component';
import { ControleAcessoComponent } from './componentes/portaria/controle-acesso/controle-acesso.component';

import { CarregamentoComponent } from './componentes/carregamento/carregamento.component';
import { CarregamentoAssistenciaComponent } from './componentes/carregamento/assistencia/assistencia.component';
import { CarregamentoAguardandoDescargaComponent } from './componentes/carregamento/aguardando-descarga/aguardando-descarga.component';

import { ManutencaoComponent } from './componentes/manutencao/manutencao.component';
import { SolicitarManutencaoComponent } from './componentes/manutencao/solicitar-manutencao/solicitar-manutencao.component';
import { EstoqueManutencaoComponent } from './componentes/manutencao/estoque/estoque.component';
import { AcompanhamentoComponent } from './componentes/manutencao/acompanhamento/acompanhamento.component';
import { FilaChamadosComponent } from './componentes/manutencao/fila-chamados/fila-chamados.component';
import { SolicitarPecaComponent } from './componentes/manutencao/solicitar-peca/solicitar-peca.component';

import { AssistenciaTecnicaComponent } from './componentes/assistencia/assistencia.component';
import { AssistenciaLotesComponent } from './componentes/assistencia/lotes/lotes.component';
import { EstoquePecasComponent } from './componentes/assistencia/estoque-pecas/estoque-pecas.component';

import { RecursosHumanosComponent } from './componentes/recursos-humanos/recursos-humanos.component';
import { PontoComponent } from './componentes/recursos-humanos/ponto/ponto.component';
import { FuncionariosComponent } from './componentes/recursos-humanos/funcionarios/funcionarios.component';
import { FeriasComponent } from './componentes/recursos-humanos/ferias/ferias.component';
import { SolicitacoesRhComponent } from './componentes/recursos-humanos/solicitacoes/solicitacoes.component';
import { PermissaoAcessoComponent } from './componentes/recursos-humanos/permissao-acesso/permissao-acesso.component';

import { PcpComponent } from './componentes/pcp/pcp.component';
import { OrdensProducaoComponent } from './componentes/pcp/ordens/ordens.component';
import { ProgramacaoComponent } from './componentes/pcp/programacao/programacao.component';
import { CapacidadeComponent } from './componentes/pcp/capacidade/capacidade.component';
import { KpisComponent } from './componentes/pcp/kpis/kpis.component';

import { ComprasComponent } from './componentes/compras/compras.component';
import { PedidosCompraComponent } from './componentes/compras/pedidos/pedidos.component';
import { CotacoesComponent } from './componentes/compras/cotacoes/cotacoes.component';
import { AprovacoesComponent } from './componentes/compras/aprovacoes/aprovacoes.component';

import { FinanceiroComponent } from './componentes/financeiro/financeiro.component';
import { ContasPagarComponent } from './componentes/financeiro/contas-pagar/contas-pagar.component';
import { ContasReceberComponent } from './componentes/financeiro/contas-receber/contas-receber.component';
import { FluxoCaixaComponent } from './componentes/financeiro/fluxo-caixa/fluxo-caixa.component';

import { LotesComponent } from './componentes/lotes/lotes.component';

import { AdministracaoComponent } from './componentes/administracao/administracao.component';
import { UsuariosComponent } from './componentes/administracao/usuarios/usuarios.component';
import { AuditoriaComponent } from './componentes/administracao/auditoria/auditoria.component';
import { RelatoriosComponent } from './componentes/administracao/relatorios/relatorios.component';
import { ParametrosComponent } from './componentes/administracao/parametros/parametros.component';

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

  { path: 'manutencao',                             component: ManutencaoComponent },
  { path: 'manutencao/solicitar-manutencao',        component: SolicitarManutencaoComponent },
  { path: 'manutencao/solicitar-peca',              component: SolicitarPecaComponent },
  { path: 'manutencao/estoque',                     component: EstoqueManutencaoComponent },
  { path: 'manutencao/acompanhamento',              component: AcompanhamentoComponent },
  { path: 'manutencao/fila-chamados',               component: FilaChamadosComponent },

  { path: 'assistencia',                            component: AssistenciaTecnicaComponent },
  { path: 'assistencia/lotes',                      component: AssistenciaLotesComponent },
  { path: 'assistencia/estoque-pecas',              component: EstoquePecasComponent },

  { path: 'recursos-humanos',                       component: RecursosHumanosComponent },
  { path: 'recursos-humanos/ponto',                 component: PontoComponent },
  { path: 'recursos-humanos/funcionarios',          component: FuncionariosComponent },
  { path: 'recursos-humanos/ferias',                component: FeriasComponent },
  { path: 'recursos-humanos/solicitacoes',           component: SolicitacoesRhComponent },
  { path: 'recursos-humanos/permissao-acesso',       component: PermissaoAcessoComponent },

  { path: 'pcp',                                    component: PcpComponent },
  { path: 'pcp/ordens',                             component: OrdensProducaoComponent },
  { path: 'pcp/programacao',                        component: ProgramacaoComponent },
  { path: 'pcp/capacidade',                         component: CapacidadeComponent },
  { path: 'pcp/kpis',                               component: KpisComponent },

  { path: 'compras',                                component: ComprasComponent },
  { path: 'compras/pedidos',                        component: PedidosCompraComponent },
  { path: 'compras/cotacoes',                       component: CotacoesComponent },
  { path: 'compras/aprovacoes',                     component: AprovacoesComponent },

  { path: 'financeiro',                             component: FinanceiroComponent },
  { path: 'financeiro/contas-pagar',                component: ContasPagarComponent },
  { path: 'financeiro/contas-receber',              component: ContasReceberComponent },
  { path: 'financeiro/fluxo-caixa',                 component: FluxoCaixaComponent },

  { path: 'administracao',                          component: AdministracaoComponent },
  { path: 'administracao/usuarios',                 component: UsuariosComponent },
  { path: 'administracao/auditoria',                component: AuditoriaComponent },
  { path: 'administracao/relatorios',               component: RelatoriosComponent },
  { path: 'administracao/parametros',               component: ParametrosComponent },
  { path: 'administracao/lotes',                    component: LotesComponent },
];
