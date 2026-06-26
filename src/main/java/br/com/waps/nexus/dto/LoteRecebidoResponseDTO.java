package br.com.waps.nexus.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LoteRecebidoResponseDTO {

    private Long id;

    private String numeroSerie;
    private String estetica;
    private Integer defeitoConstatadoId;
    private String classificacao;
    private String statusItem;
    private String resetado;

    private Integer loteTriagemId;

    private String triador;
    private LocalDateTime dataConferencia;

    private String statusConferencia;
    private String observacao;
}
