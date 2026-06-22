package br.com.waps.nexus.domain.lote;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_lote_triagem")
public class LoteTriagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer numero;
    private String descricao;
    private LocalDate dataLote;

    // ── Dados vindos da ARM ──────────────────────────
    private String protocolo;
    private String cnpjCliente;
    private String razaoSocialCliente;
    private String enderecoCliente;
    private String contatoCliente;
    private Integer quantidadeEsperada;

    public String getCnpjCliente() {
        return cnpjCliente;
    }

    public void setCnpjCliente(String cnpjCliente) {
        this.cnpjCliente = cnpjCliente;
    }

    public String getContatoCliente() {
        return contatoCliente;
    }

    public void setContatoCliente(String contatoCliente) {
        this.contatoCliente = contatoCliente;
    }

    public LocalDate getDataLote() {
        return dataLote;
    }

    public void setDataLote(LocalDate dataLote) {
        this.dataLote = dataLote;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getEnderecoCliente() {
        return enderecoCliente;
    }

    public void setEnderecoCliente(String enderecoCliente) {
        this.enderecoCliente = enderecoCliente;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public Integer getQuantidadeEsperada() {
        return quantidadeEsperada;
    }

    public void setQuantidadeEsperada(Integer quantidadeEsperada) {
        this.quantidadeEsperada = quantidadeEsperada;
    }

    public String getRazaoSocialCliente() {
        return razaoSocialCliente;
    }

    public void setRazaoSocialCliente(String razaoSocialCliente) {
        this.razaoSocialCliente = razaoSocialCliente;
    }
}
