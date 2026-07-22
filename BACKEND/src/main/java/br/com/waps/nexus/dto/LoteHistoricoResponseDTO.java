package br.com.waps.nexus.dto;

import br.com.waps.nexus.domain.lote.historico.TipoEventoHistorico;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LoteHistoricoResponseDTO {

    private Long id;
    private TipoEventoHistorico tipoEvento;
    private String descricao;
    private String usuario;
    private LocalDateTime dataEvento;

    public LoteHistoricoResponseDTO() {
    }

    public LoteHistoricoResponseDTO(Long id, TipoEventoHistorico tipoEvento, String descricao,
                                    String usuario, LocalDateTime dataEvento) {

        this.id = id;
        this.tipoEvento = tipoEvento;
        this.descricao = descricao;
        this.usuario = usuario;
        this.dataEvento = dataEvento;
    }
}
