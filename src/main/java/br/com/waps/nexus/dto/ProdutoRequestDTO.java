package br.com.waps.nexus.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProdutoRequestDTO {

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
    private String Status2;
    private String statusCadastro;

    // ── Financeiro ──────────────────────────────────
    // classificacao NÃO entra aqui: é sempre calculada pelo ProdutoService,
    // nunca recebida do client.
    private String custoAquisicao;
    private BigDecimal saldoContabil;

    // ── Localização / fiscal ────────────────────────
    private String uf;
    private String nfVenda;
    private String reversaClaro;

    // ── Datas ───────────────────────────────────────
    private LocalDate dataCompra;
    private LocalDate dataSeparacao;
    private LocalDate dataEntradaNasajon;

    // ── Lotes ────────────────────────────────────────
    private Integer loteTriagemId;

    // ── Campos extras da Triagem (planilha MODELO) ──
    private String cor;
    private String capacidade;
    private String estetica;
    private Boolean realizadoRecovery;
    // triador NÃO entra aqui: preenchido automaticamente via
    // SecurityContextHolder no momento da criação, não vem do client.
    private Boolean validaImei;
    private String tipoTriagem;

    private String nfDevolucao;
    private LocalDate dataNfDevolucao;
    private String centroDistribuicao;

    // ── Campos extras da Triagem (planilha MODELO) ──
    private String statusItem;
    private Integer defeitoConstatadoId;

    // ── Resetado ─────────────────────────────────────
    private String resetado;
}
