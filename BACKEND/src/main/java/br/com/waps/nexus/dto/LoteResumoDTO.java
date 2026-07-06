package br.com.waps.nexus.dto;

import br.com.waps.nexus.domain.lote.triagem.StatusLote;

import java.time.LocalDate;

public record LoteResumoDTO(

    Integer id,
    LocalDate dataLote,
    StatusLote status,
    Long qtdItens,
    Long qtdConferidos
) {}
