package br.com.waps.nexus.domain.produto;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_produto")
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

    // ── Controle ─────────────────────────────────────
    @Column(name = "criado_em", insertable= false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private LocalDateTime atualizadoEm;

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getPreenchimentoObrig() {
        return preenchimentoObrig;
    }

    public void setPreenchimentoObrig(String preenchimentoObrig) {
        this.preenchimentoObrig = preenchimentoObrig;
    }

    public String getCodigoSap() {
        return codigoSap;
    }

    public void setCodigoSap(String codigoSap) {
        this.codigoSap = codigoSap;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumeroPatrimonio() {
        return numeroPatrimonio;
    }

    public void setNumeroPatrimonio(String numeroPatrimonio) {
        this.numeroPatrimonio = numeroPatrimonio;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getSgp() {
        return sgp;
    }

    public void setSgp(String sgp) {
        this.sgp = sgp;
    }

    public String getNumeroImobilizado() {
        return numeroImobilizado;
    }

    public void setNumeroImobilizado(String numeroImobilizado) {
        this.numeroImobilizado = numeroImobilizado;
    }

    public String getSubNumeroImobilizado() {
        return subNumeroImobilizado;
    }

    public void setSubNumeroImobilizado(String subNumeroImobilizado) {
        this.subNumeroImobilizado = subNumeroImobilizado;
    }

    public String getCondicaoBem() {
        return condicaoBem;
    }

    public void setCondicaoBem(String condicaoBem) {
        this.condicaoBem = condicaoBem;
    }

    public String getAlienarOuArmazenar() {
        return alienarOuArmazenar;
    }

    public void setAlienarOuArmazenar(String alienarOuArmazenar) {
        this.alienarOuArmazenar = alienarOuArmazenar;
    }

    public String getStatusProcesso() {
        return statusProcesso;
    }

    public void setStatusProcesso(String statusProcesso) {
        this.statusProcesso = statusProcesso;
    }

    public String getStatusVenda() {
        return statusVenda;
    }

    public void setStatusVenda(String statusVenda) {
        this.statusVenda = statusVenda;
    }

    public String getStatus2() {
        return status2;
    }

    public void setStatus2(String status2) {
        this.status2 = status2;
    }

    public String getStatusCadastro() {
        return statusCadastro;
    }

    public void setStatusCadastro(String statusCadastro) {
        this.statusCadastro = statusCadastro;
    }

    public String getCustoAquisicao() {
        return custoAquisicao;
    }

    public void setCustoAquisicao(String custoAquisicao) {
        this.custoAquisicao = custoAquisicao;
    }

    public String getClassificacao() {
        return classificacao;
    }

    public void setClassificacao(String classificacao) {
        this.classificacao = classificacao;
    }

    public BigDecimal getSaldoContabil() {
        return saldoContabil;
    }

    public void setSaldoContabil(BigDecimal saldoContabil) {
        this.saldoContabil = saldoContabil;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getNfVenda() {
        return nfVenda;
    }

    public void setNfVenda(String nfVenda) {
        this.nfVenda = nfVenda;
    }

    public String getReversaClaro() {
        return reversaClaro;
    }

    public void setReversaClaro(String reversaClaro) {
        this.reversaClaro = reversaClaro;
    }

    public LocalDate getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(LocalDate dataCompra) {
        this.dataCompra = dataCompra;
    }

    public LocalDate getDataSeparacao() {
        return dataSeparacao;
    }

    public void setDataSeparacao(LocalDate dataSeparacao) {
        this.dataSeparacao = dataSeparacao;
    }

    public LocalDate getDataEntradaNajason() {
        return dataEntradaNajason;
    }

    public void setDataEntradaNajason(LocalDate dataEntradaNajason) {
        this.dataEntradaNajason = dataEntradaNajason;
    }

    public Integer getLoteTriagemId() {
        return loteTriagemId;
    }

    public void setLoteTriagemId(Integer loteTriagemId) {
        this.loteTriagemId = loteTriagemId;
    }

    public Integer getLoteAprovacaoId() {
        return loteAprovacaoId;
    }

    public void setLoteAprovacaoId(Integer loteAprovacaoId) {
        this.loteAprovacaoId = loteAprovacaoId;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(String capacidade) {
        this.capacidade = capacidade;
    }

    public String getEstetica() {
        return estetica;
    }

    public void setEstetica(String estetica) {
        this.estetica = estetica;
    }

    public Boolean getRealizadoRecovery() {
        return realizadoRecovery;
    }

    public void setRealizadoRecovery(Boolean realizadoRecovery) {
        this.realizadoRecovery = realizadoRecovery;
    }

    public String getTriador() {
        return triador;
    }

    public void setTriador(String triador) {
        this.triador = triador;
    }

    public Boolean getValidaImei() {
        return validaImei;
    }

    public void setValidaImei(Boolean validaImei) {
        this.validaImei = validaImei;
    }

    public String getTipoTriagem() {
        return tipoTriagem;
    }

    public void setTipoTriagem(String tipoTriagem) {
        this.tipoTriagem = tipoTriagem;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
