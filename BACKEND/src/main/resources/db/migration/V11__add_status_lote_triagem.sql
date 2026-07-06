ALTER TABLE tb_lote_triagem
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'RECEBIDO';

UPDATE tb_lote_triagem lt
SET status = CASE
                 WHEN EXISTS (SELECT 1 FROM tb_produto p WHERE p.lote_triagem_id = lt.id)
                     THEN 'EM_TRIAGEM'
                 ELSE 'RECEBIDO'
    END;