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

import { ManutencaoComponent } from './componente/manutencao/manutencao.component';
import { SolicitarManutencaoComponent } from './componente/manutencao/solicitar-manutencao/solicitar-manutencao.component';
import { EstoqueManutencaoComponent } from './componente/manutencao/estoque/estoque.component';
import { AcompanhamentoComponent } from './componente/manutencao/acompanhamento/acompanhamento.component';
import { FilaChamadosComponent } from './componente/manutencao/fila-chamados/fila-chamados.component';
import { SolicitarPecaComponent } from './componente/manutencao/solicitar-peca/solicitar-peca.component';

import { AssistenciaTecnicaComponent } from './componente/assistencia/assistencia.component';
import { AssistenciaLotesComponent } from './componente/assistencia/lotes/lotes.component';
import { EstoquePecasComponent } from './componente/assistencia/estoque-pecas/estoque-pecas.component';

import { RecursosHumanosComponent } from './componente/recursos-humanos/recursos-humanos.component';
import { PontoComponent } from './componente/recursos-humanos/ponto/ponto.component';
import { FuncionariosComponent } from './componente/recursos-humanos/funcionarios/funcionarios.component';
import { FeriasComponent } from './componente/recursos-humanos/ferias/ferias.component';
import { SolicitacoesRhComponent } from './componente/recursos-humanos/solicitacoes/solicitacoes.component';

import { PcpComponent } from './componente/pcp/pcp.component';
import { OrdensProducaoComponent } from './componente/pcp/ordens/ordens.component';
import { ProgramacaoComponent } from './componente/pcp/programacao/programacao.component';
import { CapacidadeComponent } from './componente/pcp/capacidade/capacidade.component';
import { KpisComponent } from './componente/pcp/kpis/kpis.component';

import { ComprasComponent } from './componente/compras/compras.component';
import { PedidosCompraComponent } from './componente/compras/pedidos/pedidos.component';
import { CotacoesComponent } from './componente/compras/cotacoes/cotacoes.component';
import { AprovacoesComponent } from './componente/compras/aprovacoes/aprovacoes.component';

import { FinanceiroComponent } from './componente/financeiro/financeiro.component';
import { ContasPagarComponent } from './componente/financeiro/contas-pagar/contas-pagar.component';
import { ContasReceberComponent } from './componente/financeiro/contas-receber/contas-receber.component';
import { FluxoCaixaComponent } from './componente/financeiro/fluxo-caixa/fluxo-caixa.component';

import { LotesComponent } from './componente/lotes/lotes.component';

import { AdministracaoComponent } from './componente/administracao/administracao.component';
import { UsuariosComponent } from './componente/administracao/usuarios/usuarios.component';
import { AuditoriaComponent } from './componente/administracao/auditoria/auditoria.component';
import { RelatoriosComponent } from './componente/administracao/relatorios/relatorios.component';
import { ParametrosComponent } from './componente/administracao/parametros/parametros.component';

import { ChamadoTiComponent } from './componente/suporte/chamado-ti/chamado-ti.component';

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
  { path: 'recursos-humanos/solicitacoes',          component: SolicitacoesRhComponent },

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

  { path: 'suporte/chamado-ti',                     component: ChamadoTiComponent },
];
