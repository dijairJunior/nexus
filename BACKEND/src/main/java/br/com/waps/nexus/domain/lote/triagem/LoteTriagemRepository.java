package br.com.waps.nexus.domain.lote.triagem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LoteTriagemRepository
        extends JpaRepository<LoteTriagem, Integer>, JpaSpecificationExecutor<LoteTriagem> {

    Optional<LoteTriagem> findByProtocolo(String protocolo);

    boolean existsByProtocolo(String protocolo);
}
