\set ON_ERROR_STOP on
CREATE OR REPLACE TEMP VIEW demo_contagens AS
 SELECT 'RH / funcionários' modulo,count(*) quantidade FROM funcionario WHERE email LIKE 'demo.erp.%@example.invalid'
 UNION ALL SELECT 'RH / perfis',count(*) FROM perfil_acesso WHERE nome LIKE 'DEMO - %'
 UNION ALL SELECT 'RH / permissões dos perfis',count(*) FROM perfil_permissao_acesso p JOIN perfil_acesso a ON a.id=p.perfil_id WHERE a.nome LIKE 'DEMO - %'
 UNION ALL SELECT 'RH / férias',count(*) FROM rh_afastamento WHERE motivo='DEMO - solicitação de férias'
 UNION ALL SELECT 'RH / solicitações',count(*) FROM rh_solicitacao WHERE descricao LIKE 'DEMO - solicitar%'
 UNION ALL SELECT 'RH / dias de ponto',count(*) FROM rh_ponto_dia p JOIN funcionario f ON f.id=p.funcionario_id WHERE f.email LIKE 'demo.erp.%@example.invalid'
 UNION ALL SELECT 'RH / escalas',count(*) FROM rh_ponto_escala p JOIN funcionario f ON f.id=p.funcionario_id WHERE f.email LIKE 'demo.erp.%@example.invalid'
 UNION ALL SELECT 'Almoxarifado / categorias',count(*) FROM categoria WHERE nome LIKE 'DEMO - Ferragens %'
 UNION ALL SELECT 'Almoxarifado / fornecedores',count(*) FROM fornecedor WHERE nome LIKE 'Fornecedor DEMO %'
 UNION ALL SELECT 'Almoxarifado / produtos',count(*) FROM produto WHERE sku LIKE 'DEMO-ERP-%'
 UNION ALL SELECT 'Almoxarifado / movimentos',count(*) FROM movimentacao_estoque m JOIN produto p ON p.id=m.produto_id WHERE p.sku LIKE 'DEMO-ERP-%'
 UNION ALL SELECT 'Almoxarifado / requisições',count(*) FROM pcp_requisicao WHERE motivo LIKE 'DEMO - material auxiliar%'
 UNION ALL SELECT 'Compras / pedidos',count(*) FROM compra_pedido WHERE codigo LIKE 'DEMO-ERP-DOB-%' OR codigo LIKE 'DEMO-MAN-ROL-%'
 UNION ALL SELECT 'Compras / cotações',count(*) FROM compra_cotacao c JOIN compra_pedido p ON p.id=c.pedido_id WHERE p.codigo LIKE 'DEMO-ERP-DOB-%' OR p.codigo LIKE 'DEMO-MAN-ROL-%'
 UNION ALL SELECT 'Financeiro / pagar',count(*) FROM financeiro_titulo t JOIN compra_pedido p ON p.id=t.origem_id AND t.origem='COMPRA' WHERE p.codigo LIKE 'DEMO-ERP-DOB-%'
 UNION ALL SELECT 'Financeiro / receber',count(*) FROM financeiro_titulo t JOIN comercial_pedido p ON p.id=t.origem_id AND t.origem='VENDA' WHERE p.cliente LIKE 'Cliente DEMO %'
 UNION ALL SELECT 'Financeiro / caixa realizado',count(*) FROM financeiro_titulo WHERE documento LIKE 'DEMO-FOLHA-%' AND origem='MANUAL' AND status='BAIXADO' AND baixa<=CURRENT_DATE
 UNION ALL SELECT 'Comercial / pedidos',count(*) FROM comercial_pedido WHERE cliente LIKE 'Cliente DEMO %'
 UNION ALL SELECT 'PCP / linhas',count(*) FROM pcp_linha WHERE nome LIKE 'Linha DEMO %'
 UNION ALL SELECT 'PCP / ordens',count(*) FROM pcp_ordem WHERE produto LIKE 'Guarda-roupa DEMO %'
 UNION ALL SELECT 'PCP / lotes',count(*) FROM pcp_lote_producao WHERE observacoes LIKE 'DEMO-ERP-V1:%'
 UNION ALL SELECT 'Catálogo / modelos',count(*) FROM fabricacao_modelo WHERE codigo LIKE 'DEMO-ERP-%'
 UNION ALL SELECT 'Catálogo / variantes',count(*) FROM fabricacao_variante v JOIN fabricacao_modelo m ON m.id=v.modelo_id WHERE m.codigo LIKE 'DEMO-ERP-%'
 UNION ALL SELECT 'Produção / '||s.nome,count(*) FROM pcp_ordem_setor o JOIN fabricacao_setor s ON s.id=o.setor_id JOIN pcp_lote_producao l ON l.id=o.lote_id WHERE l.observacoes LIKE 'DEMO-ERP-V1:%' GROUP BY s.nome
 UNION ALL SELECT 'Almoxarifado / consumos',count(*) FROM pcp_consumo_setor c JOIN produto p ON p.id=c.produto_id WHERE p.sku LIKE 'DEMO-ERP-%'
 UNION ALL SELECT 'Corte / lotes anteriores',count(*) FROM fabricacao_lote WHERE observacoes='DEMO - lote do fluxo de esteiras'
 UNION ALL SELECT 'Embalagem / esteiras',count(*) FROM embalagem_esteira WHERE codigo LIKE 'DEMO-%'
 UNION ALL SELECT 'Embalagem / lotes',count(*) FROM embalagem_lote WHERE observacoes='DEMO - peças prontas para embalar'
 UNION ALL SELECT 'Embalagem / etapas',count(*) FROM embalagem_etapa e JOIN embalagem_lote l ON l.id=e.lote_id WHERE l.observacoes='DEMO - peças prontas para embalar'
 UNION ALL SELECT 'Portaria / recebimentos',count(*) FROM registro_chegada WHERE nota_fiscal LIKE '990100%' AND empresa LIKE 'Fornecedor DEMO %'
 UNION ALL SELECT 'Carregamento / caminhões',count(*) FROM registro_chegada WHERE nota_fiscal LIKE '990200%' AND empresa LIKE 'Transportadora DEMO %'
 UNION ALL SELECT 'Carregamento / retornos',count(*) FROM registro_chegada WHERE nota_fiscal LIKE '990300%' AND empresa LIKE 'Cliente DEMO %'
 UNION ALL SELECT 'Carregamento / ordens prontas',count(*) FROM pcp_ordem WHERE produto LIKE 'Guarda-roupa DEMO %' AND status='FINALIZADA' AND expedido_em IS NULL
 UNION ALL SELECT 'Assistência / lotes',count(*) FROM assistencia_lote WHERE codigo LIKE 'DEMO-ASS-%'
 UNION ALL SELECT 'Assistência / estoque',count(*) FROM assistencia_peca_estoque WHERE codigo LIKE 'DEMO-ASS-%'
 UNION ALL SELECT 'Manutenção / mecânicos',count(*) FROM manutencao_mecanico WHERE nome LIKE 'Mecânico DEMO %'
 UNION ALL SELECT 'Manutenção / estoque',count(*) FROM manutencao_peca_estoque WHERE codigo LIKE 'DEMO-MAN-%'
 UNION ALL SELECT 'Manutenção / chamados',count(*) FROM manutencao_chamado WHERE equipamento LIKE 'Furadeira DEMO %' AND origem='MANUTENCAO'
 UNION ALL SELECT 'Manutenção / solicitações de peças',count(*) FROM manutencao_chamado WHERE equipamento LIKE 'Furadeira DEMO %' AND origem='PECA'
 UNION ALL SELECT 'Suporte / chamados',count(*) FROM suporte_ti_chamado WHERE numero LIKE 'DEMO-TI-%'
 UNION ALL SELECT 'Administração / auditoria da carga',count(*) FROM gestao_auditoria WHERE acao='CARGA_DEMO' AND detalhe LIKE 'DEMO-ERP-V1:%';

DO $validar$
DECLARE r record;
BEGIN
 IF current_database()<>'aratechmoveis_dev' THEN RAISE EXCEPTION 'Banco diferente de dev'; END IF;
 FOR r IN SELECT * FROM demo_contagens WHERE quantidade<10 LOOP
  RAISE EXCEPTION 'Módulo % com apenas % registros',r.modulo,r.quantidade;
 END LOOP;
 IF (SELECT count(*) FROM demo_contagens WHERE modulo LIKE 'Produção / %')<>5 THEN
  RAISE EXCEPTION 'Faltam destinos de produção';
 END IF;
 IF EXISTS(SELECT 1 FROM produto p WHERE p.sku LIKE 'DEMO-ERP-%' AND p.quantidade<>(
  SELECT sum(CASE WHEN m.tipo='SAIDA' THEN -m.quantidade ELSE m.quantidade END)
  FROM movimentacao_estoque m WHERE m.produto_id=p.id)) THEN
  RAISE EXCEPTION 'Saldo do estoque difere dos movimentos';
 END IF;
 IF EXISTS(SELECT 1 FROM pcp_consumo_setor c JOIN produto p ON p.id=c.produto_id
  LEFT JOIN movimentacao_estoque m ON m.chave_operacao=c.chave_operacao
  WHERE p.sku LIKE 'DEMO-ERP-%' AND (m.id IS NULL OR m.tipo<>'SAIDA' OR m.produto_id<>c.produto_id OR m.quantidade<>c.quantidade)) THEN
  RAISE EXCEPTION 'Consumo sem movimentação correspondente';
 END IF;
 IF EXISTS(SELECT 1 FROM pcp_lote_producao l JOIN pcp_ordem o ON o.id=l.ordem_origem_id
  JOIN comercial_pedido v ON v.id=o.pedido_id
  LEFT JOIN financeiro_titulo t ON t.origem='VENDA' AND t.origem_id=v.id
  WHERE l.observacoes LIKE 'DEMO-ERP-V1:%' AND (l.quantidade<>o.quantidade OR o.quantidade<>v.quantidade
   OR t.id IS NULL OR t.valor<>v.quantidade*v.valor_unitario OR l.revisao<>(l.snapshot->>'versao')::bigint)) THEN
  RAISE EXCEPTION 'Divergência entre venda, OP, lote, revisão ou financeiro';
 END IF;
 IF EXISTS(SELECT 1 FROM compra_pedido c LEFT JOIN financeiro_titulo t ON t.origem='COMPRA' AND t.origem_id=c.id
  WHERE c.codigo LIKE 'DEMO-ERP-%' AND c.status='Recebido' AND (t.id IS NULL OR t.valor<>c.quantidade*c.valor_unitario)) THEN
  RAISE EXCEPTION 'Compra recebida sem provisão financeira correta';
 END IF;
 IF EXISTS(SELECT 1 FROM pcp_lote_producao l CROSS JOIN LATERAL jsonb_array_elements(l.calculo->'setores') g
  LEFT JOIN pcp_ordem_setor o ON o.lote_id=l.id AND o.setor_id=(g->'setor'->>'id')::bigint
  WHERE l.observacoes LIKE 'DEMO-ERP-V1:%' AND (o.id IS NULL OR o.dados<>g)) THEN
  RAISE EXCEPTION 'Ordem setorial diferente da prévia congelada';
 END IF;
 IF EXISTS(SELECT 1 FROM pcp_lote_producao l CROSS JOIN LATERAL jsonb_array_elements(l.calculo->'setores') g
  CROSS JOIN LATERAL jsonb_array_elements(g->'linhas') i
  WHERE l.observacoes LIKE 'DEMO-ERP-V1:%' AND (
   (i->>'total')::numeric<>(i->>'porUnidade')::numeric*l.quantidade
   OR ((g->'setor'->>'codigo')='ALMOXARIFADO' AND (i->>'natureza'<>'CONSUMO' OR i->'especificacao'<>'null'::jsonb OR i->>'total'<>'1200'))
   OR ((g->'setor'->>'codigo')='CORTE' AND (i->>'codigo'='DOB' OR i->'especificacao'='null'::jsonb))
   OR (i->>'codigo'='LAT' AND (i->>'total')::numeric<>300))) THEN
  RAISE EXCEPTION 'Totais calculados ou isolamento das linhas incorretos';
 END IF;
 IF EXISTS(SELECT 1 FROM pcp_lote_producao l JOIN pcp_ordem op ON op.id=l.ordem_origem_id
  WHERE l.observacoes LIKE 'DEMO-ERP-V1:%' AND op.status='FINALIZADA'
   AND EXISTS(SELECT 1 FROM pcp_ordem_setor o WHERE o.lote_id=l.id AND o.status<>'CONCLUIDA')) THEN
  RAISE EXCEPTION 'OP finalizada com trabalho setorial pendente';
 END IF;
 IF EXISTS(SELECT 1 FROM pcp_ordem_setor o JOIN fabricacao_setor s ON s.id=o.setor_id
  JOIN pcp_lote_producao l ON l.id=o.lote_id
  WHERE l.observacoes LIKE 'DEMO-ERP-V1:%' AND s.codigo='ALMOXARIFADO' AND o.status='CONCLUIDA'
  AND NOT EXISTS(SELECT 1 FROM pcp_consumo_setor c WHERE c.ordem_id=o.id)) THEN
  RAISE EXCEPTION 'Separação concluída sem consumo';
 END IF;
 IF EXISTS(SELECT 1 FROM embalagem_lote e JOIN fabricacao_lote c ON c.id=e.origem_id
  WHERE e.observacoes='DEMO - peças prontas para embalar' AND (c.status<>'CORTADO' OR e.quantidade<>c.quantidade OR e.ficha<>c.ficha)) THEN
  RAISE EXCEPTION 'Embalagem incompatível com origem de corte';
 END IF;
 IF EXISTS(SELECT 1 FROM funcionario f JOIN acesso_conta a ON a.funcionario_id=f.id WHERE f.email LIKE 'demo.erp.%@example.invalid') THEN
  RAISE EXCEPTION 'Funcionário de demonstração não deve possuir conta de acesso';
 END IF;
 RAISE NOTICE 'Vínculos, mínimos, cálculos, revisão, separação, estoque, financeiro e embalagem validados.';
END $validar$;
SELECT modulo,quantidade FROM demo_contagens ORDER BY modulo;
