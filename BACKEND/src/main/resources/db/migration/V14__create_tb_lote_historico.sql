CREATE TABLE tb_lote_historico (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   lote_triagem_id INT NOT NULL,
                                   tipo_evento VARCHAR(30) NOT NULL,
                                   descricao VARCHAR(500) NOT NULL,
                                   usuario VARCHAR(100),
                                   data_evento DATETIME NOT NULL,
                                   CONSTRAINT fk_historico_lote FOREIGN KEY (lote_triagem_id) REFERENCES tb_lote_triagem(id)
);