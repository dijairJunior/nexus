package br.com.waps.nexus.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ContraprovaItemDTO {

    public enum StatusContraprova {
        OK,
        RECEBIDO_NAO_PREVISTO,
        PREVISTO_NAO_RECEBIDO,
        DIVERGENCIA_CLASSIFICACAO
    }

    private String numeroSerie;
    private StatusContraprova status;
    private String classificacaoEsperada; // vem do tb_produto (planilha ARM)
    private String classificacaoRecebida; // vem do tb_lote_recebido (conferencia física)

    public ContraprovaItemDTO() {
    }

    public ContraprovaItemDTO(String numeroSerie, StatusContraprova status,
                              String classificacaoEsperada, String classificacaoRecebida) {

        this.numeroSerie = numeroSerie;
        this.status = status;
        this.classificacaoEsperada = classificacaoEsperada;
        this.classificacaoRecebida = classificacaoRecebida;
    }
}
