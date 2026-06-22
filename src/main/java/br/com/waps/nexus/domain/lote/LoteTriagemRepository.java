package br.com.waps.nexus.domain.lote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoteTriagemRepository extends JpaRepository<LoteTriagem, Integer> {

    Optional<LoteTriagem> findByProtocolo(String protocolo);

    boolean existsByProtocolo(String protocolo);
}
