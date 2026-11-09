import { LotesProducaoComponent } from './componente/fabricacao/lotes-producao.component';
import { VariantesComponent } from './componente/fabricacao/variantes.component';
import { OrdensSetorComponent } from './componente/fabricacao/ordens-setor.component';
import { GestaoComponent } from './componente/gestao/gestao.component';
import { Routes } from '@angular/router';
import { EmbalagemComponent } from './componente/embalagem/embalagem.component';
import { EsteirasComponent } from './componente/embalagem/esteiras.component';
import { LotesEmbalagemComponent } from './componente/embalagem/lotes-embalagem.component';
import { GuardaRoupasComponent } from './componente/fabricacao/guarda-roupas.component';
import { LotesFabricacaoComponent } from './componente/fabricacao/lotes-fabricacao.component';
import { CorteComponent } from './componente/fabricacao/corte.component';
import { MarcacaoPontoComponent } from './componente/recursos-humanos/ponto/marcacao-ponto.component';
import { acessoGuard } from './core/acesso.guard';
import { TrocarSenhaComponent } from './componente/login/trocar-senha.component';
import { AcessoNegadoComponent, FalhaConexaoComponent } from './componente/acesso-negado.component';
import { LoginComponent } from './componente/login/login.component';
import { DashboardComponent } from './componente/dashboard/dashboard.component';

import { AlmoxarifadoComponent } from './componente/almoxarifado/almoxarifado.component';
import { EstoqueProdutosComponent } from './componente/almoxarifado/estoque-produtos/estoque-produtos.component';
import { CategoriasComponent } from './componente/almoxarifado/categorias/categorias.component';
import { FornecedoresComponent } from './componente/almoxarifado/fornecedores/fornecedores.component';
import { RecebimentoComponent } from './componente/almoxarifado/recebimento/recebimento.component';

import { PortariaComponent } from './componente/portaria/portaria.component';
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
import { MecanicosComponent } from './componente/manutencao/mecanicos/mecanicos.component';

import { AssistenciaTecnicaComponent } from './componente/assistencia/assistencia.component';
import { AssistenciaLotesComponent } from './componente/assistencia/lotes/lotes.component';
import { EstoquePecasComponent } from './componente/assistencia/estoque-pecas/estoque-pecas.component';

import { RecursosHumanosComponent } from './componente/recursos-humanos/recursos-humanos.component';
import { PontoComponent } from './componente/recursos-humanos/ponto/ponto.component';
import { FuncionariosComponent } from './componente/recursos-humanos/funcionarios/funcionarios.component';
import { FeriasComponent } from './componente/recursos-humanos/ferias/ferias.component';
import { SolicitacoesRhComponent } from './componente/recursos-humanos/solicitacoes/solicitacoes.component';
import { PerfilComponent } from './componente/recursos-humanos/perfil/perfil.component';
import { PerfilMenuComponent } from './componente/recursos-humanos/perfil/perfil-menu.component';
import { AtribuirPerfilComponent } from './componente/recursos-humanos/perfil/atribuir-perfil.component';

import { PcpComponent } from './componente/pcp/pcp.component';
import { OrdensProducaoComponent } from './componente/pcp/ordens/ordens.component';
import { ProgramacaoComponent } from './componente/pcp/programacao/programacao.component';
import { CapacidadeComponent } from './componente/pcp/capacidade/capacidade.component';
import { KpisComponent } from './componente/pcp/kpis/kpis.component';

import { ComprasComponent } from './componente/compras/compras.component';
import { PedidosCompraComponent } from './componente/compras/pedidos/pedidos.component';
import { CotacoesComponent } from './componente/compras/cotacoes/cotacoes.component';

import { FinanceiroComponent } from './componente/financeiro/financeiro.component';
import { ContasPagarComponent } from './componente/financeiro/contas-pagar/contas-pagar.component';
import { ContasReceberComponent } from './componente/financeiro/contas-receber/contas-receber.component';
import { FluxoCaixaComponent } from './componente/financeiro/fluxo-caixa/fluxo-caixa.component';
import { BaixaNotasComponent } from './componente/financeiro/baixa-notas/baixa-notas.component';

import { LotesComponent } from './componente/lotes/lotes.component';

import { AdministracaoComponent } from './componente/administracao/administracao.component';
import { AuditoriaComponent } from './componente/administracao/auditoria/auditoria.component';
import { RelatoriosComponent } from './componente/administracao/relatorios/relatorios.component';
import { ParametrosComponent } from './componente/administracao/parametros/parametros.component';

import { SuporteComponent } from './componente/suporte/suporte.component';
import { ChamadoTiComponent } from './componente/suporte/chamado-ti/chamado-ti.component';
import { AcompanhamentoTiComponent } from './componente/suporte/acompanhamento/acompanhamento-ti.component';

import { MeuPerfilComponent } from './componente/meu-perfil/meu-perfil.component';

const appRoutes: Routes = [
  { path:'embalagem/ordens',component:OrdensSetorComponent,data:{caminho:'/aratech/embalagem/ordens',recurso:'EMBALAGEM_LOTES',nome:'Embalagem',volta:'/embalagem'} },
  { path:'producao/montagem',component:OrdensSetorComponent,data:{caminho:'/aratech/producao/montagem',recurso:'PRODUCAO_MONTAGEM',nome:'Montagem',volta:'/dashboard'} },
  { path:'producao/furacao',component:OrdensSetorComponent,data:{caminho:'/aratech/producao/furacao',recurso:'PRODUCAO_FURACAO',nome:'Furação',volta:'/dashboard'} },
  { path:'almoxarifado/separacoes',component:OrdensSetorComponent,data:{caminho:'/aratech/almoxarifado/separacoes',recurso:'ALMOXARIFADO_REQUISICOES',nome:'Almoxarifado',volta:'/almoxarifado'} },
  { path:'corte/ordens',component:OrdensSetorComponent,data:{caminho:'/aratech/corte/ordens',recurso:'CORTE_LOTES',nome:'Corte',volta:'/corte'} },
  { path: 'documentacao', loadComponent: () => import('./componente/documentacao/documentacao.component').then(m => m.DocumentacaoComponent) },
  { path: 'trocar-senha', component: TrocarSenhaComponent },
  { path: 'sem-acesso', component: AcessoNegadoComponent },
  { path: 'falha-conexao', component: FalhaConexaoComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login',                                  component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'embalagem', component:EmbalagemComponent },
  { path: 'embalagem/esteiras', component:EsteirasComponent },
  { path: 'embalagem/lotes', component:LotesEmbalagemComponent },
  { path: 'embalagem/lotes/:id', component:LotesEmbalagemComponent },
  { path: 'comercial', redirectTo:'corte', pathMatch:'full' },
  { path: 'comercial/pedidos', redirectTo:'corte/lotes', pathMatch:'full' },
  { path: 'corte', component:CorteComponent },
  { path: 'corte/lotes', component:LotesFabricacaoComponent, data:{corte:true} },
  { path: 'pcp/lotes', component:LotesProducaoComponent },
  { path: 'pcp/lotes/corte-legado', component:LotesFabricacaoComponent, data:{corte:false} },
  { path: 'administracao/guarda-roupas/variantes', component:VariantesComponent },
  { path: 'pcp/lotes/concluidos', component:LotesComponent },
  { path: 'pcp/linhas', component:GestaoComponent, data:{recurso:'linhas'} },
  { path: 'pcp/requisicoes', component:GestaoComponent, data:{recurso:'requisicoesPcp'} },
  { path: 'almoxarifado/requisicoes', component:GestaoComponent, data:{recurso:'requisicoesAlmox'} },
  { path: 'administracao/receber-assistencia', component:GestaoComponent, data:{recurso:'recebimentoAssistencia'} },
  { path: 'carregamento/expedicao', component:GestaoComponent, data:{recurso:'expedicao'} },

  { path: 'almoxarifado',                           component: AlmoxarifadoComponent },
  { path: 'almoxarifado/estoque-produtos',          component: EstoqueProdutosComponent },
  { path: 'almoxarifado/categorias',                component: CategoriasComponent },
  { path: 'almoxarifado/fornecedores',              component: FornecedoresComponent },
  { path: 'almoxarifado/recebimento',               component: RecebimentoComponent },

  { path: 'portaria',                               component: PortariaComponent },
  { path: 'portaria/controle-acesso',               component: ControleAcessoComponent },

  { path: 'carregamento',                           component: CarregamentoComponent },
  { path: 'carregamento/assistencia',               component: CarregamentoAssistenciaComponent },
  { path: 'carregamento/aguardando-descarga', redirectTo: 'carregamento/cargas-descargas', pathMatch: 'full' },
  { path: 'carregamento/cargas-descargas',       component: CarregamentoAguardandoDescargaComponent },

  { path: 'manutencao',                             component: ManutencaoComponent },
  { path: 'manutencao/solicitar-manutencao',        component: SolicitarManutencaoComponent },
  { path: 'manutencao/solicitar-peca',              component: SolicitarPecaComponent },
  { path: 'manutencao/estoque',                     component: EstoqueManutencaoComponent },
  { path: 'manutencao/acompanhamento',              component: AcompanhamentoComponent },
  { path: 'manutencao/fila-chamados',               component: FilaChamadosComponent },
  { path: 'manutencao/mecanicos',                   component: MecanicosComponent },

  { path: 'assistencia',                            component: AssistenciaTecnicaComponent },
  { path: 'assistencia/lotes',                      component: AssistenciaLotesComponent },
  { path: 'assistencia/estoque-pecas',              component: EstoquePecasComponent },

  { path: 'recursos-humanos',                       component: RecursosHumanosComponent },
  { path: 'recursos-humanos/ponto',                 component: PontoComponent },
  { path: 'recursos-humanos/funcionarios',          component: FuncionariosComponent },
  { path: 'recursos-humanos/ferias',                component: FeriasComponent },
  { path: 'recursos-humanos/solicitacoes',          component: SolicitacoesRhComponent },
  { path: 'recursos-humanos/perfil',                component: PerfilMenuComponent },
  { path: 'recursos-humanos/perfil/criar',          component: PerfilComponent },
  { path: 'recursos-humanos/perfil/atribuir',       component: AtribuirPerfilComponent },

  { path: 'pcp',                                    component: PcpComponent },
  { path: 'pcp/ordens',                             component: OrdensProducaoComponent },
  { path: 'pcp/programacao',                        component: ProgramacaoComponent },
  { path: 'pcp/capacidade',                         component: CapacidadeComponent },
  { path: 'pcp/kpis',                               component: KpisComponent },

  { path: 'almoxarifado/pedidos', component: PedidosCompraComponent, data: { origem: 'almoxarifado' } },
  { path: 'manutencao/pedidos', component: PedidosCompraComponent, data: { origem: 'manutencao' } },
  { path: 'compras',                                component: ComprasComponent },
  { path: 'compras/pedidos',                        component: PedidosCompraComponent },
  { path: 'compras/cotacoes',                       component: CotacoesComponent },
  { path: 'compras/aprovacoes', redirectTo: 'compras/pedidos', pathMatch: 'full' },

  { path: 'financeiro',                             component: FinanceiroComponent },
  { path: 'financeiro/contas-pagar',                component: ContasPagarComponent },
  { path: 'financeiro/contas-receber',              component: ContasReceberComponent },
  { path: 'financeiro/fluxo-caixa',                 component: FluxoCaixaComponent },
  { path: 'financeiro/baixa-notas',                 component: BaixaNotasComponent },

  { path: 'administracao',                          component: AdministracaoComponent },
  { path: 'administracao/auditoria',                component: AuditoriaComponent },
  { path: 'administracao/relatorios',               component: RelatoriosComponent },
  { path: 'administracao/parametros',               component: ParametrosComponent },
  { path: 'administracao/lotes', redirectTo:'pcp/lotes/concluidos', pathMatch:'full' },
  { path: 'administracao/guarda-roupas', component:GuardaRoupasComponent },

  { path: 'meu-ponto', component: PontoComponent },
  { path: 'marcacao-ponto', component: MarcacaoPontoComponent },
  { path: 'recursos-humanos/ponto/:id', component: PontoComponent },
  { path: 'meu-perfil',                             component: MeuPerfilComponent },

  { path: 'suporte',                                component: SuporteComponent },
  { path: 'suporte/chamado-ti',                     component: ChamadoTiComponent },
  { path: 'suporte/acompanhamento',                 component: AcompanhamentoTiComponent },
];

const publicas = new Set(['', 'login', 'falha-conexao']);
export const routes: Routes = appRoutes.map(route => publicas.has(route.path ?? '') || route.redirectTo ? route : { ...route, canActivate: [acessoGuard] });
