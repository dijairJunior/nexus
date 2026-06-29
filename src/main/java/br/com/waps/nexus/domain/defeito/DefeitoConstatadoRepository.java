package br.com.waps.nexus.domain.defeito;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DefeitoConstatadoRepository extends JpaRepository<DefeitoConstatado, Integer> {

    Optional<DefeitoConstatado> findByDescricao(String descricao);

}
