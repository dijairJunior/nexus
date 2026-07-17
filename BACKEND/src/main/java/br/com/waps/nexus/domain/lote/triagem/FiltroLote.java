package br.com.waps.nexus.domain.lote.triagem;

import java.time.LocalDate;

public record FiltroLote(
        StatusLote status,
        String search,
        String protocolo,
        LocalDate dataInicio,
        LocalDate dataFim
) {}
