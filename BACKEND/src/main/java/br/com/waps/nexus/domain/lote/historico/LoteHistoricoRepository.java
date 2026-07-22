package br.com.waps.nexus.domain.lote.historico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoteHistoricoRepository extends JpaRepository<LoteHistorico, Long> {

    List<LoteHistorico> findByLoteTriagemIdOrderByDataEventoDesc(Integer loteTriagemId);
}
