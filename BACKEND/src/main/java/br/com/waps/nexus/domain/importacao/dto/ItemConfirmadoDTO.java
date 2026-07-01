package br.com.waps.nexus.domain.importacao.dto;

import java.time.LocalDate;

public class ItemConfirmadoDTO {

    private String numeroSerie;
    private String modelo;
    private String nfDevolucao;
    private LocalDate dataNfDevolucao;
    private String centroDistribuicao;

    public String getCentroDistribuicao() {
        return centroDistribuicao;
    }

    public void setCentroDistribuicao(String centroDistribuicao) {
        this.centroDistribuicao = centroDistribuicao;
    }

    public LocalDate getDataNfDevolucao() {
        return dataNfDevolucao;
    }

    public void setDataNfDevolucao(LocalDate dataNfDevolucao) {
        this.dataNfDevolucao = dataNfDevolucao;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNfDevolucao() {
        return nfDevolucao;
    }

    public void setNfDevolucao(String nfDevolucao) {
        this.nfDevolucao = nfDevolucao;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }
}
