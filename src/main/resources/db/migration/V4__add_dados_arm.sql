-- ── Lote de Triagem: dados do cliente vindos da ARM ──────────
ALTER TABLE tb_lote_triagem
    ADD COLUMN protocolo             VARCHAR(50) UNIQUE,
    ADD COLUMN cnpj_cliente          VARCHAR(20),
    ADD COLUMN razao_social_cliente  VARCHAR(300),
    ADD COLUMN endereco_cliente      VARCHAR(500),
    ADD COLUMN contato_cliente       VARCHAR(500),
    ADD COLUMN quantidade_esperada   INT;

-- ── Produto: dados vindos do ROMANEIO ─────────────────────────
ALTER TABLE tb_produto
    ADD COLUMN nf_devolucao         VARCHAR(30),
    ADD COLUMN data_nf_devolucao    DATE,
    ADD COLUMN centro_distribuicao  VARCHAR(20);