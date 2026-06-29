package br.com.waps.nexus.domain.defeito;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_defeito_constatado")
@Getter
@Setter
public class DefeitoConstatado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String descricao;
}
