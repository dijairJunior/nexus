package br.com.waps.nexus.domain.lote.historico;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_lote_historico")
@Getter
@Setter
public class LoteHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lote_triagem_id", nullable = false)
    private Integer loteTriagemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 30)
    private TipoEventoHistorico tipoEvento;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(length = 100)
    private String usuario;

    @Column(name = "data_evento", updatable = false)
    @CreationTimestamp
    private LocalDateTime dataEvento;
}
