-- ── Lookup: tipos de defeito constatado ──────────────────
CREATE TABLE IF NOT EXISTS tb_defeito_constatado (
                                                     id          INT             NOT NULL AUTO_INCREMENT,
                                                     descricao   VARCHAR(100)    NOT NULL UNIQUE,
                                                     PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO tb_defeito_constatado (descricao) VALUES
                                                  ('SEM DEFEITO'),
                                                  ('NÃO LIGA / DESLIGA / NÃO CARREGA'),
                                                  ('DISPLAY / TOUCHSCREEN'),
                                                  ('TAMPA/ CARCAÇA / ESTÉTICA'),
                                                  ('BLOQUEADO'),
                                                  ('TECLADO / BOTÕES'),
                                                  ('BATERIA / CARGA'),
                                                  ('CHIP / SIM / GAVETA / REDE'),
                                                  ('AUDIO / MICROFONE'),
                                                  ('VIBRACALL'),
                                                  ('BIOMETRIA / SENSORES'),
                                                  ('CÂMERA/ VÍDEO/ FOTO'),
                                                  ('CONEXÃO E SINCRONIZAÇÃO'),
                                                  ('TRAVANDO / SOFTWARE / APPS');

-- ── Produto: vínculo com o defeito + status do item ──────
ALTER TABLE tb_produto
    ADD COLUMN defeito_constatado_id  INT,
    ADD COLUMN status_item            VARCHAR(20),
    ADD CONSTRAINT fk_produto_defeito
        FOREIGN KEY (defeito_constatado_id) REFERENCES tb_defeito_constatado(id);