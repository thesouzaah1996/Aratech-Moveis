-- Cinco chegadas FICTICIAS compartilhadas pela Portaria e Recebimento do almoxarifado.
-- Nao altera registros existentes nem repete cargas ao executar novamente.
BEGIN;
INSERT INTO registro_chegada (nota_fiscal,empresa,nome_motorista,placa,descricao_carga,setor_responsavel,status,chave_operacao,
                              data_chegada,data_autorizacao,data_entrada,data_finalizacao)
VALUES
('900000001','Fornecedor Demo Chapas','Motorista Demo Um','DEM1A01','Chapas de MDF para producao','ALMOXARIFADO','AGUARDANDO','b0a11111-2222-4333-8444-000000000001',CURRENT_TIMESTAMP - INTERVAL '50 minutes',NULL,NULL,NULL),
('900000002','Fornecedor Demo Ferragens','Motorista Demo Dois','DEM1A02','Dobradicas e corredicas telescopicas','ALMOXARIFADO','AGUARDANDO','b0a11111-2222-4333-8444-000000000002',CURRENT_TIMESTAMP - INTERVAL '40 minutes',NULL,NULL,NULL),
('900000003','Fornecedor Demo Bordas','Motorista Demo Tres','DEM1A03','Fitas de borda em PVC','ALMOXARIFADO','AUTORIZADO','b0a11111-2222-4333-8444-000000000003',CURRENT_TIMESTAMP - INTERVAL '30 minutes',CURRENT_TIMESTAMP - INTERVAL '28 minutes',NULL,NULL),
('900000004','Fornecedor Demo Embalagens','Motorista Demo Quatro','DEM1A04','Caixas de papelao e plastico de embalagem','ALMOXARIFADO','AUTORIZADO','b0a11111-2222-4333-8444-000000000004',CURRENT_TIMESTAMP - INTERVAL '20 minutes',CURRENT_TIMESTAMP - INTERVAL '18 minutes',NULL,NULL),
('900000005','Fornecedor Demo Acabamentos','Motorista Demo Cinco','DEM1A05','Tintas e vernizes para acabamento','ALMOXARIFADO','EM_RECEBIMENTO','b0a11111-2222-4333-8444-000000000005',CURRENT_TIMESTAMP - INTERVAL '10 minutes',CURRENT_TIMESTAMP - INTERVAL '8 minutes',CURRENT_TIMESTAMP - INTERVAL '6 minutes',NULL)
ON CONFLICT DO NOTHING;
COMMIT;
