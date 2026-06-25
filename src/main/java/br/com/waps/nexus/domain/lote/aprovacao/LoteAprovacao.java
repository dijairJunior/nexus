package br.com.waps.nexus.domain.lote.aprovacao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_lote_aprovacao")
@Getter
@Setter
public class LoteAprovacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lote_triagem_id", nullable = false)
    private Integer loteTriagemId;

    @Column(nullable = false, length = 100)
    private String gestor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_aprovacao", nullable = false, length = 20)
    private StatusAprovacao statusAprovacao;

    @Column(length = 500)
    private String motivo;

    @CreationTimestamp
    @Column(name = "data_aprovacao", updatable = false)
    private LocalDateTime dataAprovacao;
}
