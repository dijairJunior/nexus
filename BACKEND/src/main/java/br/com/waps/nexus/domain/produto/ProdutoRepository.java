package br.com.waps.nexus.domain.produto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findByNumeroSerie(String numeroSerie);
    List<Produto> findByLoteTriagemId(Integer loteTriagemId);
    List<Produto> findByStatusCadastro(String statusCadastro);
    boolean existsByNumeroSerie(String numeroSerie);

    long countByLoteTriagemId(Integer loteTriagemId);
}
