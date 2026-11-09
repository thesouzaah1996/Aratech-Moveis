-- Somente leitura. Contagens das fontes de dados das telas, sem dados pessoais.
SELECT tela, registros, CASE WHEN registros >= 10 THEN '10 ou mais' ELSE 'menos de 10' END AS cobertura
FROM (
  SELECT 'Almoxarifado / Categorias' AS tela, count(*) AS registros FROM categoria
  UNION ALL SELECT 'Almoxarifado / Fornecedores', count(*) FROM fornecedor
  UNION ALL SELECT 'Almoxarifado / Estoque', count(*) FROM produto
  UNION ALL SELECT 'Almoxarifado / Requisições', count(*) FROM pcp_requisicao
  UNION ALL SELECT 'Almoxarifado / Recebimentos em andamento', count(*) FROM registro_chegada WHERE setor_responsavel='ALMOXARIFADO' AND status<>'FINALIZADO'
  UNION ALL SELECT 'Portaria / Chegadas em andamento', count(*) FROM registro_chegada WHERE status<>'FINALIZADO'
  UNION ALL SELECT 'Portaria / Histórico', count(*) FROM registro_chegada WHERE status='FINALIZADO'
  UNION ALL SELECT 'Carregamento / Cargas e descargas em andamento', count(*) FROM registro_chegada WHERE setor_responsavel='CARREGAMENTO' AND status<>'FINALIZADO'
  UNION ALL SELECT 'Carregamento / Produção pronta para expedição', count(*) FROM pcp_ordem WHERE status='FINALIZADA' AND expedido_em IS NULL
  UNION ALL SELECT 'Carregamento / Histórico de expedição', count(*) FROM pcp_ordem WHERE expedido_em IS NOT NULL
  UNION ALL SELECT 'Comercial / Pedidos', count(*) FROM comercial_pedido
  UNION ALL SELECT 'PCP / Ordens e programação', count(*) FROM pcp_ordem
  UNION ALL SELECT 'PCP / Linhas e capacidade', count(*) FROM pcp_linha
  UNION ALL SELECT 'PCP / Requisições', count(*) FROM pcp_requisicao
  UNION ALL SELECT 'Compras / Pedidos', count(*) FROM compra_pedido
  UNION ALL SELECT 'Compras / Cotações', count(*) FROM compra_cotacao
  UNION ALL SELECT 'Financeiro / Contas a pagar', count(*) FROM financeiro_titulo WHERE tipo='PAGAR'
  UNION ALL SELECT 'Financeiro / Contas a receber', count(*) FROM financeiro_titulo WHERE tipo='RECEBER'
  UNION ALL SELECT 'Financeiro / Documentos de contas a pagar', count(*) FROM financeiro_titulo WHERE tipo='PAGAR' AND documento IS NOT NULL
  UNION ALL SELECT 'Financeiro / Baixas que compõem o caixa', count(*) FROM financeiro_titulo WHERE status='BAIXADO'
  UNION ALL SELECT 'Manutenção / Chamados', count(*) FROM manutencao_chamado WHERE origem='MANUTENCAO'
  UNION ALL SELECT 'Manutenção / Solicitações de peças', count(*) FROM manutencao_chamado WHERE origem='PECA'
  UNION ALL SELECT 'Manutenção / Mecânicos', count(*) FROM manutencao_mecanico
  UNION ALL SELECT 'Manutenção / Estoque', count(*) FROM manutencao_peca_estoque
  UNION ALL SELECT 'Manutenção / Pedidos de compra', count(*) FROM compra_pedido WHERE origem='MANUTENCAO'
  UNION ALL SELECT 'Assistência / Lotes', count(*) FROM assistencia_lote
  UNION ALL SELECT 'Assistência / Peças', count(*) FROM assistencia_peca_estoque
  UNION ALL SELECT 'RH / Funcionários', count(*) FROM funcionario
  UNION ALL SELECT 'RH / Dias de ponto', count(*) FROM rh_ponto_dia
  UNION ALL SELECT 'RH / Férias e afastamentos', count(*) FROM rh_afastamento
  UNION ALL SELECT 'RH / Solicitações', count(*) FROM rh_solicitacao
  UNION ALL SELECT 'RH / Perfis de acesso', count(*) FROM perfil_acesso
  UNION ALL SELECT 'Administração / Auditoria', count(*) FROM gestao_auditoria
  UNION ALL SELECT 'Administração / Lotes concluídos', count(*) FROM pcp_ordem WHERE status='FINALIZADA'
  UNION ALL SELECT 'Suporte / Chamados de TI', count(*) FROM suporte_ti_chamado
) fontes ORDER BY tela;

-- Parâmetros têm catálogo fixo. Perfis próprios e indicadores não são listas de cadastros independentes.
SELECT chave, descricao, valor FROM gestao_parametro ORDER BY chave;
