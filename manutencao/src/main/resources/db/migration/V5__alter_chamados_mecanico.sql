ALTER TABLE chamados DROP COLUMN mecanico;
ALTER TABLE chamados ADD COLUMN mecanico_id BIGINT REFERENCES mecanicos(id);
