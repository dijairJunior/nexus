package br.com.waps.nexus.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoteRecebidoRequestDTO {

    private String numeroSerie;
    private String estetica;
    private Integer defeitoConstatadoId;
    // classificacao NÂO entra: é sempre calculada pelo LoteRecebidoService
    private String statusItem;
    private String resetado;

    private Integer loteTriagemId;

    private String triador;
    // dataConferencia NÂO entra: gerada via @CreationTimestamp.

    private String statusConferencia;
    private String observacao;
}
