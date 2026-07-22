package br.com.waps.nexus.domain.lote.historico;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoteHistoricoService {
    private final LoteHistoricoRepository loteHistoricoRespository;

    public void registrar(Integer loteTriagemId, TipoEventoHistorico tipo, String descricao) {
        LoteHistorico historico = new LoteHistorico();
        historico.setLoteTriagemId(loteTriagemId);
        historico.setTipoEvento(tipo);
        historico.setDescricao(descricao);
        loteHistoricoRespository.save(historico);
    }

    public List<LoteHistorico> listarPorLote(Integer loteTriagemId) {
        return loteHistoricoRespository.findByLoteTriagemIdOrderByDataEventoDesc(loteTriagemId);
    }

}
