package br.com.waps.nexus.domain.lote.aprovacao;

import br.com.waps.nexus.domain.lote.triagem.LoteTriagemRepository;
import br.com.waps.nexus.exception.BusinessException;
import br.com.waps.nexus.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoteAprovacaoService {

    private final LoteAprovacaoRepository loteAprovacaoRepository;
    private final LoteTriagemRepository loteTriagemRepository;

    public LoteAprovacaoService(LoteAprovacaoRepository loteAprovacaoRepository,
                                LoteTriagemRepository loteTriagemRepository) {

        this.loteAprovacaoRepository = loteAprovacaoRepository;
        this.loteTriagemRepository = loteTriagemRepository;
    }

    public LoteAprovacao registrarDecisao(LoteAprovacao decisao) {

        if (!loteTriagemRepository.existsById(decisao.getLoteTriagemId())) {
            throw new ResourceNotFoundException("Lote de triagem não encontrado: " + decisao.getLoteTriagemId());
        }

        if (decisao.getStatusAprovacao() == StatusAprovacao.REPROVADO && (decisao.getMotivo() == null || decisao.getMotivo().isBlank())) {
            throw new BusinessException("Motivo é obrigatório para reprovação");
        }

        return loteAprovacaoRepository.save(decisao);
    }

    public List<LoteAprovacao> listarPorLote(Integer loteTriagemId) {
        return loteAprovacaoRepository.findByLoteTriagemId(loteTriagemId);
    }
}
