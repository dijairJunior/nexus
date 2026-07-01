CREATE TABLE tb_lote_recebido (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  numero_serie VARCHAR(20) NOT NULL,
                                  estetica VARCHAR(30),
                                  defeito_constatado_id INT,
                                  classificacao VARCHAR(5),
                                  lote_triagem_id INT NOT NULL,
                                  triador VARCHAR(100),
                                  data_conferencia DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  status_conferencia VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
                                  observacao VARCHAR(255),

                                  PRIMARY KEY (id),

                                  CONSTRAINT fk_lote_recebido_lote_triagem
                                      FOREIGN KEY (lote_triagem_id) REFERENCES tb_lote_triagem(id),

                                  CONSTRAINT fk_lote_recebido_defeito_constatado
                                      FOREIGN KEY (defeito_constatado_id) REFERENCES tb_defeito_constatado(id)
);