-- Carga manual de demonstração; não é migração Flyway.
-- Apenas INSERT em tabelas persistentes. Não modifica registros anteriores.
\set ON_ERROR_STOP on
BEGIN;
SET LOCAL TIME ZONE 'America/Sao_Paulo';
SET LOCAL lock_timeout = '10s';
SELECT pg_advisory_xact_lock(7241202601);

DO $guard$
BEGIN
 IF current_database() <> 'aratechmoveis_dev' THEN
  RAISE EXCEPTION 'Carga permitida exclusivamente em aratechmoveis_dev';
 END IF;
 IF NOT EXISTS(SELECT 1 FROM flyway_schema_history WHERE version='17' AND success) THEN
  RAISE EXCEPTION 'A carga exige schema atualizado até V17';
 END IF;
END $guard$;

CREATE FUNCTION pg_temp.demo_item(chave uuid, codigo text, nome text, tipo text, material text,
 quantidade numeric, cortar boolean, comprimento numeric, largura numeric, espessura numeric)
RETURNS jsonb LANGUAGE SQL AS $fn$
 SELECT jsonb_build_object('chave',chave,'codigo',codigo,'nome',nome,'tipo',tipo,
  'grupo',CASE WHEN cortar THEN 'Estrutura' ELSE 'Ferragens' END,'material',material,
  'acabamento',CASE WHEN cortar THEN 'Branco' ELSE 'Zincado' END,'unidade','UN',
  'quantidade',quantidade,'cortar',cortar,'comprimentoMm',comprimento,'larguraMm',largura,
  'espessuraMm',espessura,'veio','SEM','bordas','[]'::jsonb,'fitaBorda','',
  'furacao','','observacoes','','atributos','{}'::jsonb)
$fn$;

DO $seed$
DECLARE
 n integer; k integer; etapa integer; digito integer; soma integer; cpf_demo text; pis_base text;
 sufixo text; pessoa text; nome_modelo text; setor_nome text; recurso text; instante timestamptz := CURRENT_TIMESTAMP;
 funcionario_id_demo bigint; perfil_id_demo bigint; categoria_id_demo bigint; fornecedor_id_demo bigint;
 produto_id_demo bigint; auxiliar_id_demo bigint; linha_id_demo bigint; compra_id_demo bigint;
 pedido_id_demo bigint; ordem_id_demo bigint; modelo_id_demo bigint; variante_id_demo bigint;
 lote_id_demo bigint; ordem_setor_id_demo bigint; corte_id_demo bigint; embalagem_id_demo bigint;
 esteira_id_demo bigint; mecanico_id_demo bigint; peca_id_demo bigint; chamado_id_demo bigint;
 chegada_compra bigint; chegada_carga bigint; chegada_assistencia bigint;
 corte_setor bigint; almox_setor bigint; furacao_setor bigint; montagem_setor bigint; embalagem_setor bigint;
 ficha jsonb; modelo jsonb; dados_variante jsonb; snapshot_variante jsonb; roteiro jsonb;
 calculo jsonb; grupos jsonb; linhas jsonb; especificacao jsonb; item jsonb; grupo jsonb; setor_json jsonb;
 setor_reg record; quantidade_planejada integer := 150; concluido boolean; consumo_chave uuid;
 insumo_chave uuid; quantidade_insercoes integer;
BEGIN
 SELECT count(*) INTO quantidade_insercoes FROM gestao_auditoria
  WHERE acao='CARGA_DEMO' AND entidade='CENARIO_DEMO' AND detalhe LIKE 'DEMO-ERP-V1:%';
 IF quantidade_insercoes=10 THEN
  RAISE NOTICE 'DEMO-ERP-V1 já foi inserido; nenhum registro duplicado.';
  RETURN;
 ELSIF quantidade_insercoes<>0 THEN
  RAISE EXCEPTION 'Marcadores de carga incompletos; revisar antes de inserir';
 END IF;
 IF EXISTS(SELECT 1 FROM produto WHERE sku LIKE 'DEMO-ERP-%')
  OR EXISTS(SELECT 1 FROM funcionario WHERE email LIKE 'demo.erp.%@example.invalid')
  OR EXISTS(SELECT 1 FROM fabricacao_modelo WHERE codigo LIKE 'DEMO-ERP-%') THEN
  RAISE EXCEPTION 'Prefixos reservados da demonstração já estão ocupados';
 END IF;
 SELECT id INTO STRICT corte_setor FROM fabricacao_setor WHERE codigo='CORTE';
 SELECT id INTO STRICT almox_setor FROM fabricacao_setor WHERE codigo='ALMOXARIFADO';
 SELECT id INTO STRICT furacao_setor FROM fabricacao_setor WHERE codigo='FURACAO';
 SELECT id INTO STRICT montagem_setor FROM fabricacao_setor WHERE codigo='MONTAGEM';
 SELECT id INTO STRICT embalagem_setor FROM fabricacao_setor WHERE codigo='EMBALAGEM';

 FOR n IN 1..10 LOOP
  sufixo := lpad(n::text,2,'0');
  pessoa := 'Operador DEMO ' || sufixo;
  nome_modelo := 'Guarda-roupa DEMO Aurora ' || sufixo;
  setor_nome := (ARRAY['PCP','Corte','Furação','Montagem','Embalagem','Almoxarifado','Manutenção','RH','Financeiro','Suporte'])[n];
  recurso := (ARRAY['PCP_LOTES','CORTE_LOTES','PRODUCAO_FURACAO','PRODUCAO_MONTAGEM','EMBALAGEM_LOTES','ALMOXARIFADO_REQUISICOES','MANUTENCAO_ESTOQUE','RH_PONTO','FINANCEIRO_RECEBER','SUPORTE_CHAMADOS'])[n];
  -- Documentos sintéticos com dígitos verificadores. Não representam pessoas reais.
  cpf_demo := '8901234' || sufixo;
  FOR etapa IN 1..2 LOOP
   soma := 0;
   FOR k IN 1..length(cpf_demo) LOOP
    soma := soma + substr(cpf_demo,k,1)::integer * (length(cpf_demo)+2-k);
   END LOOP;
   digito := 11-(soma%11); IF digito>=10 THEN digito:=0; END IF;
   cpf_demo := cpf_demo || digito::text;
  END LOOP;
  pis_base := '12000000' || sufixo; soma := 0;
  FOR k IN 1..10 LOOP
   soma := soma + substr(pis_base,k,1)::integer * (ARRAY[3,2,9,8,7,6,5,4,3,2])[k];
  END LOOP;
  digito := 11-(soma%11); IF digito>=10 THEN digito:=0; END IF;
  INSERT INTO funcionario(nome_completo,cpf,rg,pis_nit_nis,data_nascimento,sexo,estado_civil,
   nome_mae,nome_pai,cep,logradouro,numero,bairro,cidade,uf,telefone_fixo,celular_whatsapp,
   data_admissao,tipo_contrato,jornada_semanal,cargo,tipo_funcionario,setor,salario,email,
   banco,agencia,conta,tipo_conta,chave_pix)
  VALUES(pessoa,cpf_demo,'DEMO'||sufixo,pis_base||digito,DATE '1990-01-01'+n,2,0,
   'Responsável DEMO Materno','Responsável DEMO Paterno','01001000','Rua de demonstração',n::text,
   'Centro','São Paulo',24,'11300000'||sufixo,'119900000'||sufixo,CURRENT_DATE-730,'CLT',40,
   'Operador de '||setor_nome,CASE WHEN n<7 THEN 'PRODUCAO' ELSE 'ADMINISTRATIVO' END,
   setor_nome,3000+n*100,'demo.erp.'||sufixo||'@example.invalid','Banco de demonstração',
   '0001',('10000'||sufixo)||'-0','CORRENTE','demo.erp.'||sufixo||'@example.invalid')
  RETURNING id INTO funcionario_id_demo;
  INSERT INTO perfil_acesso(nome) VALUES('DEMO - '||setor_nome) RETURNING id INTO perfil_id_demo;
  INSERT INTO funcionario_perfil_acesso(funcionario_id,perfil_id) VALUES(funcionario_id_demo,perfil_id_demo);
  -- Somente permissões já existentes. Não cria conta nem altera acesso de usuários anteriores.
  INSERT INTO perfil_permissao_acesso(perfil_id,permissao)
   SELECT perfil_id_demo,codigo FROM acesso_permissao
   WHERE codigo=recurso||'.CONSULTAR';
  FOR k IN 1..5 LOOP
   INSERT INTO rh_ponto_escala(funcionario_id,dia_semana,horarios)
    VALUES(funcionario_id_demo,k,'08:00,12:00,13:00,17:00,,');
   INSERT INTO rh_ponto_dia(funcionario_id,data,horarios)
    VALUES(funcionario_id_demo,date_trunc('week',CURRENT_DATE)::date-7+(k-1),'08:00,12:00,13:00,17:00,,');
  END LOOP;
  INSERT INTO rh_ponto_historico(funcionario_id,data,anteriores,novos,motivo,autor_id,autor_nome)
   VALUES(funcionario_id_demo,date_trunc('week',CURRENT_DATE)::date-7,',,,,,',
    '08:00,12:00,13:00,17:00,,','Carga demonstrativa de jornada',funcionario_id_demo,pessoa);
  INSERT INTO rh_afastamento(funcionario_id,tipo,inicio,fim,motivo)
   VALUES(funcionario_id_demo,'FERIAS',CURRENT_DATE+30+n,CURRENT_DATE+39+n,'DEMO - solicitação de férias');
  INSERT INTO rh_solicitacao(funcionario_id,tipo,descricao)
   VALUES(funcionario_id_demo,'DECLARACAO','DEMO - solicitar declaração de vínculo para '||pessoa);

  INSERT INTO categoria(nome,nome_normalizado) VALUES('DEMO - Ferragens '||sufixo,'demo - ferragens '||sufixo)
   RETURNING id INTO categoria_id_demo;
  INSERT INTO fornecedor(nome,nome_normalizado,email,telefone,nome_representante,telefone_representante,email_representante)
   VALUES('Fornecedor DEMO '||sufixo,'fornecedor demo '||sufixo,'fornecedor.'||sufixo||'@example.invalid',
    '11310000'||sufixo,'Representante DEMO '||sufixo,'119910000'||sufixo,'representante.'||sufixo||'@example.invalid')
   RETURNING id INTO fornecedor_id_demo;
  INSERT INTO produto(nome,sku,quantidade,quantidade_minima,categoria_id,fornecedor_id,local_armazenamento,descricao,criado_em)
   VALUES('Dobradiça DEMO 35 mm '||sufixo,'DEMO-ERP-DOB-'||sufixo,18800,1000,categoria_id_demo,
    fornecedor_id_demo,'A-'||sufixo,'Dobradiça em unidades, vinculada à ficha do modelo '||nome_modelo,instante-interval '15 days')
   RETURNING id INTO produto_id_demo;
  INSERT INTO produto(nome,sku,quantidade,quantidade_minima,categoria_id,fornecedor_id,local_armazenamento,descricao,criado_em)
   VALUES('Parafuso de manutenção DEMO '||sufixo,'DEMO-ERP-PAR-'||sufixo,1000,100,categoria_id_demo,
    fornecedor_id_demo,'B-'||sufixo,'Material auxiliar de bancada, fora da ficha do móvel',instante-interval '15 days')
   RETURNING id INTO auxiliar_id_demo;
  INSERT INTO movimentacao_estoque(produto_id,tipo,quantidade,saldo_anterior,saldo_posterior,chave_operacao,
   responsavel_id,responsavel_nome,motivo,criado_em)
   VALUES(produto_id_demo,'INICIAL',10000,0,10000,gen_random_uuid(),funcionario_id_demo,pessoa,'DEMO - saldo inicial',instante-interval '15 days'),
    (produto_id_demo,'ENTRADA',10000,10000,20000,gen_random_uuid(),funcionario_id_demo,pessoa,'DEMO - recebimento de compra',instante-interval '10 days'),
    (auxiliar_id_demo,'INICIAL',1000,0,1000,gen_random_uuid(),funcionario_id_demo,pessoa,'DEMO - material de bancada',instante-interval '15 days');
  INSERT INTO registro_chegada(nota_fiscal,empresa,nome_motorista,placa,descricao_carga,setor_responsavel,
   status,chave_operacao,data_chegada,data_autorizacao,data_entrada,data_finalizacao)
   VALUES('990100'||sufixo,'Fornecedor DEMO '||sufixo,'Motorista DEMO '||sufixo,'DMA1A'||sufixo,
    'DEMO - recebimento de 10000 dobradiças','ALMOXARIFADO','FINALIZADO',gen_random_uuid(),
    instante-interval '10 days 2 hours',instante-interval '10 days 90 minutes',instante-interval '10 days 1 hour',instante-interval '10 days')
   RETURNING id INTO chegada_compra;
  INSERT INTO compra_pedido(origem,tipo,estoque_id,item,codigo,quantidade,unidade,fornecedor,valor_unitario,
   data_pedido,data_entrega,data_vencimento,status,motivo,solicitante,decidido_por,decidido_nome,decidido_em)
   VALUES('ALMOXARIFADO','MANUAL',produto_id_demo,'Dobradiça DEMO 35 mm '||sufixo,'DEMO-ERP-DOB-'||sufixo,
    10000,'un','Fornecedor DEMO '||sufixo,4.50,CURRENT_DATE-15,CURRENT_DATE-10,CURRENT_DATE+20,
    'Recebido','DEMO - recebido na Portaria #'||chegada_compra,pessoa,funcionario_id_demo,pessoa,instante-interval '12 days')
   RETURNING id INTO compra_id_demo;
  INSERT INTO compra_cotacao(pedido_id,fornecedor1,valor1,fornecedor2,valor2,escolhido,status)
   VALUES(compra_id_demo,'Fornecedor DEMO '||sufixo,4.50,'Alternativa DEMO '||sufixo,4.90,1,'ESCOLHIDA');
  INSERT INTO financeiro_titulo(tipo,descricao,pessoa,valor,vencimento,documento,emissao,referencia,origem,origem_id)
   VALUES('PAGAR','Compra de dobradiças DEMO '||sufixo,'Fornecedor DEMO '||sufixo,45000,CURRENT_DATE+20,
    '990100'||sufixo,CURRENT_DATE-10,'Chegada #'||chegada_compra,'COMPRA',compra_id_demo);

  INSERT INTO pcp_linha(nome,horas_dia) VALUES('Linha DEMO '||sufixo,8) RETURNING id INTO linha_id_demo;
  insumo_chave := gen_random_uuid();
  ficha := jsonb_build_object('codigo','DEMO-ERP-MOD-'||sufixo,'nome',nome_modelo,
   'descricao','Modelo de demonstração: branco, duas portas. Conferir projeto antes de produção real.',
   'larguraMm',1800,'alturaMm',2200,'profundidadeMm',600,'ativo',true,'versao',0,
   'itens',jsonb_build_array(
    pg_temp.demo_item(gen_random_uuid(),'LAT','Lateral','PAINEL','MDF',2,true,2200,600,18),
    pg_temp.demo_item(gen_random_uuid(),'POR','Porta','PAINEL','MDF',2,true,2196,897,18),
    pg_temp.demo_item(gen_random_uuid(),'TOP','Tampo','PAINEL','MDF',1,true,1764,600,18),
    pg_temp.demo_item(gen_random_uuid(),'BAS','Base','PAINEL','MDF',1,true,1764,600,18),
    pg_temp.demo_item(gen_random_uuid(),'PRA','Prateleira','PAINEL','MDF',2,true,1764,580,18),
    pg_temp.demo_item(gen_random_uuid(),'FUN','Fundo','PAINEL','MDF',1,true,2200,1800,6),
    pg_temp.demo_item(insumo_chave,'DOB','Dobradiça 35 mm','FERRAGEM','Aço',8,false,null,null,null)));
  INSERT INTO fabricacao_modelo(codigo,nome,ficha) VALUES('DEMO-ERP-MOD-'||sufixo,nome_modelo,ficha)
   RETURNING id INTO modelo_id_demo;
  modelo := jsonb_build_object('id',modelo_id_demo,'versao',1,'ficha',ficha,'fotos','[]'::jsonb);
  roteiro := '[]'::jsonb;
  FOR item IN SELECT value FROM jsonb_array_elements(ficha->'itens') LOOP
   IF (item->>'cortar')::boolean THEN
    roteiro := roteiro || jsonb_build_array(jsonb_build_object('itemChave',item->>'chave',
     'consumoSetorId',corte_setor,'produtoEstoqueId',null,'fatorEstoque',1,
     'operacoes',jsonb_build_array(
      jsonb_build_object('setorId',corte_setor,'nome','Cortar peça','instrucoes','Conferir medidas finais e acabamento branco'),
      jsonb_build_object('setorId',furacao_setor,'nome','Furar peça','instrucoes','Conferir posição dos furos no desenho do modelo DEMO'),
      jsonb_build_object('setorId',montagem_setor,'nome','Conferir montagem','instrucoes','Conferir identificação e encaixes desta peça'),
      jsonb_build_object('setorId',embalagem_setor,'nome','Proteger peça','instrucoes','Proteger bordas e identificar o modelo'))));
   ELSE
    roteiro := roteiro || jsonb_build_array(jsonb_build_object('itemChave',item->>'chave',
     'consumoSetorId',almox_setor,'produtoEstoqueId',produto_id_demo,'fatorEstoque',1,'operacoes','[]'::jsonb));
   END IF;
  END LOOP;
  dados_variante := jsonb_build_object('modeloId',modelo_id_demo,'codigo','BRANCO','nome','Branco',
   'ativo',true,'versao',0,'ficha',ficha,'roteiro',roteiro);
  INSERT INTO fabricacao_variante(modelo_id,codigo,nome,ativo,dados)
   VALUES(modelo_id_demo,'BRANCO','Branco',true,dados_variante) RETURNING id INTO variante_id_demo;
  snapshot_variante := jsonb_build_object('id',variante_id_demo,'versao',1,'dados',dados_variante);
  grupos := '[]'::jsonb;
  FOR setor_reg IN SELECT * FROM fabricacao_setor WHERE id IN(corte_setor,almox_setor,furacao_setor,montagem_setor,embalagem_setor) ORDER BY id LOOP
   setor_json := jsonb_build_object('id',setor_reg.id,'codigo',setor_reg.codigo,'nome',setor_reg.nome,
    'recurso',setor_reg.recurso,'caminho',setor_reg.caminho,'natureza',setor_reg.natureza,'exigeMedidas',setor_reg.exige_medidas);
   linhas := '[]'::jsonb;
   FOR item IN SELECT value FROM jsonb_array_elements(ficha->'itens') LOOP
    IF ((item->>'cortar')::boolean AND setor_reg.id<>almox_setor)
     OR (NOT (item->>'cortar')::boolean AND setor_reg.id=almox_setor) THEN
     especificacao := CASE WHEN setor_reg.id=almox_setor THEN null ELSE item END;
     linhas := linhas || jsonb_build_array(jsonb_build_object('itemChave',item->>'chave',
      'natureza',CASE WHEN setor_reg.id=almox_setor THEN 'CONSUMO' ELSE 'OPERACAO' END,
      'codigo',item->>'codigo','descricao',item->>'nome','unidade',item->>'unidade',
      'porUnidade',(item->>'quantidade')::numeric,'total',(item->>'quantidade')::numeric*quantidade_planejada,
      'produtoEstoqueId',CASE WHEN setor_reg.id=almox_setor THEN produto_id_demo ELSE null END,
      'especificacao',especificacao,
      'operacao',CASE setor_reg.id WHEN corte_setor THEN 'Cortar peça' WHEN furacao_setor THEN 'Furar peça'
       WHEN montagem_setor THEN 'Conferir montagem' WHEN embalagem_setor THEN 'Proteger peça' ELSE 'Separar / consumir material' END,
      'instrucoes',CASE setor_reg.id WHEN corte_setor THEN 'Conferir medidas finais e acabamento branco'
       WHEN furacao_setor THEN 'Conferir posição dos furos no desenho do modelo DEMO'
       WHEN montagem_setor THEN 'Conferir identificação e encaixes desta peça'
       WHEN embalagem_setor THEN 'Proteger bordas e identificar o modelo' ELSE '' END,
      'quantidadeEstoque',CASE WHEN setor_reg.id=almox_setor THEN 1200 ELSE null END,
      'consomeMaterial',setor_reg.id IN(corte_setor,almox_setor)));
    END IF;
   END LOOP;
   grupos := grupos || jsonb_build_array(jsonb_build_object('setor',setor_json,'linhas',linhas));
  END LOOP;
  calculo := jsonb_build_object('setores',grupos,'pendencias','[]'::jsonb);
  FOR k IN 1..2 LOOP
   concluido := k=2;
   INSERT INTO comercial_pedido(cliente,produto,quantidade,valor_unitario,entrega,vencimento,status,criado_em)
    VALUES('Cliente DEMO '||sufixo,nome_modelo,quantidade_planejada,1500+n*10,CURRENT_DATE+10,
     CURRENT_DATE+30,CASE WHEN concluido THEN 'PRONTO' ELSE 'EM_PRODUCAO' END,instante-interval '7 days')
    RETURNING id INTO pedido_id_demo;
   INSERT INTO financeiro_titulo(tipo,descricao,pessoa,valor,vencimento,origem,origem_id)
    VALUES('RECEBER','Pedido de venda DEMO #'||pedido_id_demo,'Cliente DEMO '||sufixo,
     quantidade_planejada*(1500+n*10),CURRENT_DATE+30,'VENDA',pedido_id_demo);
   INSERT INTO pcp_ordem(produto,quantidade,inicio,previsao,responsavel,linha_id,horas_previstas,pedido_id,status,iniciado_em,concluido_em)
    VALUES(nome_modelo,quantidade_planejada,CURRENT_DATE-5,CURRENT_DATE+5,pessoa,linha_id_demo,40,
     pedido_id_demo,CASE WHEN concluido THEN 'FINALIZADA' ELSE 'EM_PRODUCAO' END,
     instante-interval '5 days',CASE WHEN concluido THEN instante-interval '1 day' ELSE null END)
    RETURNING id INTO ordem_id_demo;
   INSERT INTO pcp_lote_producao(variante_id,revisao,ordem_origem_id,quantidade,previsao,observacoes,snapshot,calculo,chave_operacao,autor_nome,criado_em)
    VALUES(variante_id_demo,1,ordem_id_demo,quantidade_planejada,CURRENT_DATE+5,
     'DEMO-ERP-V1: '||CASE WHEN concluido THEN 'ciclo concluído' ELSE 'ordens liberadas' END,
     snapshot_variante,calculo,gen_random_uuid(),pessoa,instante-interval '5 days')
    RETURNING id INTO lote_id_demo;
   FOR grupo IN SELECT value FROM jsonb_array_elements(grupos) LOOP
    INSERT INTO pcp_ordem_setor(lote_id,setor_id,status,versao,dados,liberado_em,iniciado_em,concluido_em)
     VALUES(lote_id_demo,(grupo->'setor'->>'id')::bigint,CASE WHEN concluido THEN 'CONCLUIDA' ELSE 'LIBERADA' END,
      CASE WHEN concluido AND (grupo->'setor'->>'id')::bigint=almox_setor THEN 4 WHEN concluido THEN 3 ELSE 1 END,grupo,instante-interval '5 days',
      CASE WHEN concluido THEN instante-interval '4 days' ELSE null END,
      CASE WHEN concluido THEN instante-interval '2 days' ELSE null END)
     RETURNING id INTO ordem_setor_id_demo;
    IF concluido AND (grupo->'setor'->>'id')::bigint=almox_setor THEN
     consumo_chave := gen_random_uuid();
     INSERT INTO movimentacao_estoque(produto_id,tipo,quantidade,saldo_anterior,saldo_posterior,chave_operacao,
      responsavel_id,responsavel_nome,motivo,criado_em)
      VALUES(produto_id_demo,'SAIDA',1200,20000,18800,consumo_chave,funcionario_id_demo,pessoa,
       'DEMO - consumo LP-'||lote_id_demo||' / OS-'||ordem_setor_id_demo,instante-interval '3 days');
     INSERT INTO pcp_consumo_setor(ordem_id,item_chave,produto_id,quantidade,chave_operacao,confirmado_em)
      VALUES(ordem_setor_id_demo,insumo_chave,produto_id_demo,1200,consumo_chave,instante-interval '3 days');
    END IF;
   END LOOP;
   IF NOT concluido THEN
    INSERT INTO pcp_requisicao(ordem_id,produto_id,quantidade,motivo)
     VALUES(ordem_id_demo,auxiliar_id_demo,20,'DEMO - material auxiliar da bancada, fora do consumo da ficha');
   END IF;
  END LOOP;

  -- Fluxo anterior de corte/esteiras: preserva suas entidades e não simula ligação inexistente com a OP.
  INSERT INTO fabricacao_lote(modelo_id,modelo_versao,quantidade,previsao,observacoes,ficha,status,
   chave_operacao,autor_id,autor_nome,criado_em,enviado_em,iniciado_em,concluido_em)
   VALUES(modelo_id_demo,1,150,CURRENT_DATE+5,'DEMO - lote do fluxo de esteiras',modelo,'CORTADO',
    gen_random_uuid(),funcionario_id_demo,pessoa,instante-interval '5 days',instante-interval '4 days',
    instante-interval '3 days',instante-interval '2 days') RETURNING id INTO corte_id_demo;
  INSERT INTO embalagem_esteira(codigo,nome,atividade,responsavel,capacidade)
   VALUES('DEMO-EST-'||sufixo,'Esteira DEMO '||sufixo,'Conferência, proteção e identificação',pessoa,2)
   RETURNING id INTO esteira_id_demo;
  INSERT INTO embalagem_lote(origem_id,ficha,quantidade,previsao,prioridade,observacoes,chave_operacao)
   VALUES(corte_id_demo,modelo,150,CURRENT_DATE+5,'NORMAL','DEMO - peças prontas para embalar',gen_random_uuid())
   RETURNING id INTO embalagem_id_demo;
  FOR k IN 1..3 LOOP
   INSERT INTO embalagem_etapa(lote_id,sequencia,esteira_id,nome,instrucoes)
    VALUES(embalagem_id_demo,k,esteira_id_demo,(ARRAY['Conferir peças','Proteger e fechar caixas','Identificar volumes'])[k],
     'Conferir o modelo e a quantidade do lote DEMO');
  END LOOP;
  INSERT INTO embalagem_evento(lote_id,acao,detalhe,autor)
   VALUES(embalagem_id_demo,'RECEBER','DEMO - recebimento da origem CT-'||corte_id_demo,pessoa);
  INSERT INTO registro_chegada(nota_fiscal,empresa,nome_motorista,placa,descricao_carga,setor_responsavel,
   tipo_operacao,status,chave_operacao,data_chegada,data_autorizacao,data_entrada)
   VALUES('990200'||sufixo,'Transportadora DEMO '||sufixo,'Motorista DEMO '||sufixo,'DMB1A'||sufixo,
    'DEMO - coleta do pedido #'||pedido_id_demo,'CARREGAMENTO','CARREGAR','EM_RECEBIMENTO',
    gen_random_uuid(),instante-interval '2 hours',instante-interval '90 minutes',instante-interval '1 hour')
   RETURNING id INTO chegada_carga;
  INSERT INTO registro_chegada(nota_fiscal,empresa,nome_motorista,placa,descricao_carga,setor_responsavel,
   tipo_operacao,status,chave_operacao,data_chegada,data_autorizacao,data_entrada,data_finalizacao)
   VALUES('990300'||sufixo,'Cliente DEMO '||sufixo,'Motorista retorno DEMO '||sufixo,'DMC1A'||sufixo,
    'DEMO - retorno para assistência de '||nome_modelo,'CARREGAMENTO','DESCARREGAR','FINALIZADO',
    gen_random_uuid(),instante-interval '7 days 2 hours',instante-interval '7 days 90 minutes',
    instante-interval '7 days 1 hour',instante-interval '7 days') RETURNING id INTO chegada_assistencia;
  INSERT INTO assistencia_lote(origem_id,codigo,produto,empresa,quantidade,data_entrada,responsavel,defeito,status,iniciado_em,finalizado_em)
   VALUES(chegada_assistencia,'DEMO-ASS-'||sufixo,nome_modelo,'Cliente DEMO '||sufixo,2,CURRENT_DATE-7,
    pessoa,'Regulagem e conferência de dobradiças','Finalizado',instante-interval '6 days',instante-interval '5 days');
  INSERT INTO assistencia_peca_estoque(nome,codigo,modelo,quantidade_minima,quantidade,unidade,localizacao,descricao,criado_em)
   VALUES('Dobradiça para assistência DEMO '||sufixo,'DEMO-ASS-DOB-'||sufixo,nome_modelo,10,100,'un','AS-'||sufixo,
    'Reposição de assistência, separada do estoque industrial',instante-interval '15 days') RETURNING id INTO peca_id_demo;
  INSERT INTO assistencia_movimentacao_estoque(peca_id,tipo,quantidade,saldo_anterior,saldo_posterior,chave_operacao,criado_em,responsavel_nome,motivo)
   VALUES(peca_id_demo,'INICIAL',100,0,100,gen_random_uuid(),instante-interval '15 days',pessoa,'DEMO - saldo inicial');

  INSERT INTO manutencao_mecanico(nome,turno) VALUES('Mecânico DEMO '||sufixo,'MANHA') RETURNING id INTO mecanico_id_demo;
  INSERT INTO manutencao_mecanico_especialidade(mecanico_id,especialidade) VALUES(mecanico_id_demo,'FURADEIRA');
  INSERT INTO manutencao_peca_estoque(nome,codigo,quantidade,quantidade_minima,unidade,localizacao,descricao,criado_em)
   VALUES('Rolamento DEMO '||sufixo,'DEMO-MAN-ROL-'||sufixo,20,5,'un','MN-'||sufixo,
    'Reposição da furadeira da linha DEMO '||sufixo,instante-interval '15 days') RETURNING id INTO peca_id_demo;
  INSERT INTO manutencao_movimentacao_estoque(peca_id,tipo,quantidade,saldo_anterior,saldo_posterior,chave_operacao,criado_em,responsavel_nome,motivo)
   VALUES(peca_id_demo,'INICIAL',20,0,20,gen_random_uuid(),instante-interval '15 days',pessoa,'DEMO - saldo inicial');
  INSERT INTO manutencao_chamado(equipamento,tipo,prioridade,solicitante,setor,telefone,descricao,status,
   data_abertura,chave_operacao,mecanico_id,data_inicio)
   VALUES('Furadeira DEMO '||sufixo,'PREVENTIVA','MEDIA',pessoa,'Furação','119900000'||sufixo,
    'DEMO - inspeção preventiva e lubrificação','EM_MANUTENCAO',instante-interval '2 days',
    gen_random_uuid(),mecanico_id_demo,instante-interval '1 day');
  INSERT INTO manutencao_chamado(equipamento,origem,prioridade,solicitante,setor,descricao,data_abertura,
   chave_operacao,nome_peca,codigo_peca,quantidade,unidade,finalidade)
   VALUES('Furadeira DEMO '||sufixo,'PECA','MEDIA',pessoa,'Furação','DEMO - reposição de rolamentos',
    instante,gen_random_uuid(),'Rolamento DEMO '||sufixo,'DEMO-MAN-ROL-'||sufixo,5,'un','ESTOQUE')
   RETURNING id INTO chamado_id_demo;
  INSERT INTO compra_pedido(origem,tipo,estoque_id,solicitacao_id,item,codigo,quantidade,unidade,fornecedor,
   data_vencimento,status,motivo,solicitante)
   VALUES('MANUTENCAO','SOLICITACAO_PECA',peca_id_demo,chamado_id_demo,'Rolamento DEMO '||sufixo,
    'DEMO-MAN-ROL-'||sufixo,5,'un','Fornecedor DEMO '||sufixo,CURRENT_DATE+20,
    'Aguardando Aprovação','DEMO - reposição solicitada pela manutenção',pessoa)
   RETURNING id INTO compra_id_demo;
  INSERT INTO compra_cotacao(pedido_id,fornecedor1,valor1,fornecedor2,valor2)
   VALUES(compra_id_demo,'Fornecedor DEMO '||sufixo,35,'Alternativa DEMO '||sufixo,38);
  INSERT INTO suporte_ti_chamado(numero,tipo,prioridade,modulo_afetado,titulo,solicitante,setor,contato,
   descricao,tentativas_solucao,status,data_abertura,chave_operacao)
   VALUES('DEMO-TI-'||sufixo,'HARDWARE','MEDIA',setor_nome,'Conferir terminal DEMO '||sufixo,pessoa,
    setor_nome,'119900000'||sufixo,'DEMO - terminal requer verificação de periféricos',
    'Cabos e alimentação conferidos','ABERTO',instante,gen_random_uuid());
  INSERT INTO gestao_auditoria(modulo,acao,entidade,registro_id,usuario_id,usuario,detalhe)
   VALUES('ADMINISTRACAO','CARGA_DEMO','CENARIO_DEMO',n,funcionario_id_demo,'Carga SQL de demonstração',
    'DEMO-ERP-V1: cenário '||sufixo||'; modelo #'||modelo_id_demo||'; funcionário #'||funcionario_id_demo||'; caminhão #'||chegada_carga);
 END LOOP;
END $seed$;

-- Salários demonstrativos baixados alimentam o caixa realizado e referenciam o RH.
-- Bloco independente e idempotente permite complementar uma carga V1 existente, somente com INSERT.
INSERT INTO financeiro_titulo(tipo,descricao,pessoa,valor,vencimento,documento,emissao,referencia,origem,status,baixa)
 SELECT 'PAGAR','DEMO - salário mês anterior',f.nome_completo,f.salario,CURRENT_DATE-1,
  'DEMO-FOLHA-'||f.id,CURRENT_DATE-1,'FUNCIONARIO:'||f.id,'MANUAL','BAIXADO',CURRENT_DATE-1
 FROM funcionario f WHERE f.email LIKE 'demo.erp.%@example.invalid'
 AND NOT EXISTS(SELECT 1 FROM financeiro_titulo t WHERE t.origem='MANUAL' AND t.documento='DEMO-FOLHA-'||f.id);

\ir validar-dados-demonstracao-dev.sql
-- FINALIZAR
COMMIT;
