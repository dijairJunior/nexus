package br.com.waps.nexus.domain.lote.recebido;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_lote_recebido")
@Getter
@Setter
public class LoteRecebido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Conferência física ──────────────────────────
    private String numeroSerie;
    private String estetica;
    private Integer defeitoConstatadoId;
    private String classificacao;
    private String statusItem;
    private String resetado;

    // ── Lote (FK simples, mesmo padrão do Produto) ──
    private Integer loteTriagemId;

    // ── Controle ─────────────────────────────────────
    private String triador;

    @CreationTimestamp
    @Column(name = "data_conferencia", updatable = false)
    private LocalDateTime dataConferencia;

    private String statusConferencia;
    private String observacao;

    // ── Armazenamento ────────────────────────────────
    private Integer capacidadeArmazenamento;
    private Boolean possuiMarcasUso;
}
