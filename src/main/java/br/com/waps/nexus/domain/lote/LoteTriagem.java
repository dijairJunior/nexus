package br.com.waps.nexus.domain.lote;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tb_lote_triagem")
@Setter
@Getter
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
}