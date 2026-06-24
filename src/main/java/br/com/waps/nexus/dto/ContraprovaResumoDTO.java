package br.com.waps.nexus.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContraprovaResumoDTO {

    private Integer loteTriagemId;
    private int totalEsperado;
    private int totalRecebido;
    private int totalOk;
    private int totalRecebidoNaoPrevisto;
    private int totalPrevistoNaoRecebido;
    private int totalDivergenciaClassificacao;

    private List<ContraprovaItemDTO> itens;

    public ContraprovaResumoDTO() {

    }

    public ContraprovaResumoDTO(Integer loteTriagemId, List<ContraprovaItemDTO> itens) {
        this.loteTriagemId = loteTriagemId;
        this.itens = itens;
    }
}
