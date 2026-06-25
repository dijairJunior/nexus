package br.com.waps.nexus.domain.lote.aprovacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoteAprovacaoRepository  extends JpaRepository<LoteAprovacao, Long> {

    List<LoteAprovacao> findByLoteTriagemId(Integer loteTriagemId);
}
