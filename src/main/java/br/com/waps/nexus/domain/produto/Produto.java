package br.com.waps.nexus.domain.produto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_produto")
@Getter
@Setter
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Identificação ──────────────────────────────
    private Integer numero;
    private String preenchimentoObrig;
    private String codigoSap;
    private String descricao;
    private Integer quantidade;
    private String modelo;

    // ── Identificação ──────────────────────────────
    private String numeroPatrimonio;
    private String numeroSerie;
    private String sgp;
    private String numeroImobilizado;
    private String subNumeroImobilizado;

    // ── Situação ────────────────────────────────────
    private String condicaoBem;
    private String alienarOuArmazenar;
    private String statusProcesso;
    private String statusVenda;
    private String status2;
    private String statusCadastro;

    // ── Financeiro ──────────────────────────────────
    private String custoAquisicao;
    private String classificacao;
    private BigDecimal saldoContabil;

    // ── Localização / fiscal ────────────────────────
    private String uf;
    private String nfVenda;
    private String reversaClaro;

    // ── Datas ───────────────────────────────────────
    private LocalDate dataCompra;
    private LocalDate dataSeparacao;
    private LocalDate dataEntradaNajason;

    // ── Lotes (FK simples por enquanto) ─────────────
    private Integer loteTriagemId;
    private Integer loteAprovacaoId;

    // ── Campos extras da Triagem (planilha MODELO) ──
    private String cor;
    private String capacidade;
    private String estetica;
    private Boolean realizadoRecovery;
    private String triador;
    private Boolean validaImei;
    private String tipoTriagem;

    private String nfDevolucao;
    private LocalDate dataNfDevolucao;
    private String centroDistribuicao;

    public String getCentroDistribuicao() {
        return centroDistribuicao;
    }

    // ── Controle ─────────────────────────────────────
    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private LocalDateTime atualizadoEm;

    // ── Campos extras da Triagem (planilha MODELO) ──
    private String statusItem;
    private Integer defeitoConstatadoId;

    // ── Resetado ─────────────────────────────────────
    private String resetado;
}