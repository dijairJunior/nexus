package br.com.waps.nexus.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FiltroLoteDTO {

    private String status;
    private String protocolo;
    private LocalDate dataInicio;
    private LocalDate dataFim;
}
