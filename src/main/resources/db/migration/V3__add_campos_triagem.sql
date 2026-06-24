ALTER TABLE tb_produto
    ADD COLUMN cor                 VARCHAR(50),
    ADD COLUMN capacidade          VARCHAR(30),
    ADD COLUMN estetica            VARCHAR(50),
    ADD COLUMN realizado_recovery  TINYINT(1),
    ADD COLUMN triador             VARCHAR(100),
    ADD COLUMN valida_imei         TINYINT(1),
    ADD COLUMN tipo_triagem        VARCHAR(50);