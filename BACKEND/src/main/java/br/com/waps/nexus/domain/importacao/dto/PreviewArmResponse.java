package br.com.waps.nexus.domain.importacao.dto;

import java.util.List;

public class PreviewArmResponse {

    private String protocolo;
    private String cnpjCliente;
    private String razaoSocialCliente;
    private String enderecoCliente;
    private String contatoCliente;
    private Integer quantidadeEsperada;
    private Integer quantidadeComErro;
    private List<ItemPreviewDTO> itens;

    public String getCnpjCliente() {
        return cnpjCliente;
    }

    public void setCnpjCliente(String cnpjCliente) {
        this.cnpjCliente = cnpjCliente;
    }

    public String getEnderecoCliente() {
        return enderecoCliente;
    }

    public void setEnderecoCliente(String enderecoCliente) {
        this.enderecoCliente = enderecoCliente;
    }

    public List<ItemPreviewDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPreviewDTO> itens) {
        this.itens = itens;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public Integer getQuantidadeComErro() {
        return quantidadeComErro;
    }

    public void setQuantidadeComErro(Integer quantidadeComErro) {
        this.quantidadeComErro = quantidadeComErro;
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

    public String getContatoCliente() {
        return contatoCliente;
    }

    public void setContatoCliente(String contatoCliente) {
        this.contatoCliente = contatoCliente;
    }


}
