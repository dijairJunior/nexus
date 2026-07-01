CREATE TABLE IF NOT EXISTS tb_usuario (
    id      BIGINT          NOT NULL AUTO_INCREMENT,
    login   VARCHAR(100)    NOT NULL UNIQUE,
    senha   VARCHAR(255)    NOT NULL,
    nome    VARCHAR(150)    NOT NULL,
    ativo   TINYINT(1)      DEFAULT 1,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tb_lote_triagem (
    id          INT             NOT NULL AUTO_INCREMENT,
    numero      INT             NOT NULL UNIQUE,
    descricao   VARCHAR(100),
    data_lote   DATE,
    criado_em   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tb_lote_aprovacao (
    id          INT             NOT NULL AUTO_INCREMENT,
    numero      INT             NOT NULL UNIQUE,
    descricao   VARCHAR(100),
    data_aprov  DATE,
    criado_em   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tb_produto (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    numero                  INT,
    preenchimento_obrig     VARCHAR(100),
    codigo_sap              VARCHAR(50),
    descricao               VARCHAR(500),
    quantidade              INT,
    modelo                  VARCHAR(300),
    numero_patrimonio       VARCHAR(100),
    numero_serie            VARCHAR(50),
    sgp                     VARCHAR(50),
    numero_imobilizado      VARCHAR(50),
    sub_numero_imobilizado  VARCHAR(200),
    custo_aquisicao         VARCHAR(100),
    classificacao           CHAR(1),
    saldo_contabil          DECIMAL(15,3),
    condicao_bem            VARCHAR(100),
    alienar_ou_armazenar    VARCHAR(100),
    status_processo         VARCHAR(80),
    status_venda            VARCHAR(80),
    status2                 VARCHAR(80),
    status_cadastro         VARCHAR(80),
    uf                      CHAR(2),
    nf_venda                VARCHAR(30),
    reversa_claro           VARCHAR(200),
    data_compra             DATE,
    data_separacao          DATE,
    data_entrada_najason    DATE,
    lote_triagem_id         INT,
    lote_aprovacao_id       INT,
    criado_em               TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    atualizado_em           TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_produto_lote_triagem
    FOREIGN KEY (lote_triagem_id) REFERENCES tb_lote_triagem(id),
    CONSTRAINT fk_produto_lote_aprov
    FOREIGN KEY (lote_aprovacao_id) REFERENCES tb_lote_aprovacao(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_produto_numero          ON tb_produto(numero);
CREATE INDEX idx_produto_serie           ON tb_produto(numero_serie);
CREATE INDEX idx_produto_sap             ON tb_produto(codigo_sap);
CREATE INDEX idx_produto_status          ON tb_produto(status_processo);
CREATE INDEX idx_produto_status_cadastro ON tb_produto(status_cadastro);
CREATE INDEX idx_produto_lote_triagem    ON tb_produto(lote_triagem_id);
CREATE INDEX idx_produto_lote_aprov      ON tb_produto(lote_aprovacao_id);
CREATE INDEX idx_produto_uf              ON tb_produto(uf);