ALTER TABLE recebimentos
    ADD COLUMN data_recebimento TIMESTAMP NOT NULL DEFAULT now();
