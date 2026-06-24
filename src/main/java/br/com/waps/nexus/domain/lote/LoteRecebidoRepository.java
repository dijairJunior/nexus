package br.com.waps.nexus.domain.lote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoteRecebidoRepository  extends JpaRepository<LoteRecebido, Long> {

    List<LoteRecebido> findByLoteTriagemId(Integer loteTriagem);

    boolean existsByNumeroSerieAndLoteTriagemId(String numeroSerir, Integer  loteTriagem);

    Optional<LoteRecebido> findByNumeroSerieAndLoteTriagemId(String numeroSerie, Integer loteTriagemId);

    List<LoteRecebido> findByLoteTriagemIdAndStatusConferencia(Integer loteTriagemId, String statusConferencia);
}
