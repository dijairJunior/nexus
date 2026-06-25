CREATE TABLE tb_lote_aprovacao (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   lote_triagem_id INT NOT NULL,
                                   gestor VARCHAR(100) NOT NULL,
                                   status_aprovacao VARCHAR(20) NOT NULL,
                                   motivo VARCHAR(500),
                                   data_aprovacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   CONSTRAINT fk_lote_aprovacao_lote_triagem
                                       FOREIGN KEY (lote_triagem_id) REFERENCES tb_lote_triagem(id)
);