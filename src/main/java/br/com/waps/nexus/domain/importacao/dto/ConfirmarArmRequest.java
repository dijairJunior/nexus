package br.com.waps.nexus.domain.importacao.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class ConfirmarArmRequest {

    @NotBlank(message = "Protocolo é obrigatório")
    private String protocolo;

    private String cnpjCliente;
    private String razaoSocialCliente;
    private String enderecoCliente;
    private String contatoCliente;

    private List<ItemConfirmadoDTO> itens;

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

    public String getEnderecoCliente() {
        return enderecoCliente;
    }

    public void setEnderecoCliente(String enderecoCliente) {
        this.enderecoCliente = enderecoCliente;
    }

    public List<ItemConfirmadoDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemConfirmadoDTO> itens) {
        this.itens = itens;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getRazaoSocialCliente() {
        return razaoSocialCliente;
    }

    public void setRazaoSocialCliente(String razaoSocialCliente) {
        this.razaoSocialCliente = razaoSocialCliente;
    }
}
