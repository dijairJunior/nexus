package br.com.waps.nexus.domain.lote.aprovacao;

import br.com.waps.nexus.domain.lote.historico.LoteHistoricoService;
import br.com.waps.nexus.domain.lote.historico.TipoEventoHistorico;
import br.com.waps.nexus.domain.lote.triagem.LoteTriagemRepository;
import br.com.waps.nexus.exception.BusinessException;
import br.com.waps.nexus.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoteAprovacaoService {

    private final LoteAprovacaoRepository loteAprovacaoRepository;
    private final LoteTriagemRepository loteTriagemRepository;
    private final LoteHistoricoService loteHistoricoService;

    public LoteAprovacaoService(LoteAprovacaoRepository loteAprovacaoRepository,
                                LoteTriagemRepository loteTriagemRepository,
                                LoteHistoricoService loteHistoricoService) {

        this.loteAprovacaoRepository = loteAprovacaoRepository;
        this.loteTriagemRepository = loteTriagemRepository;
        this.loteHistoricoService = loteHistoricoService;
    }

    public LoteAprovacao registrarDecisao(LoteAprovacao decisao) {

        if (!loteTriagemRepository.existsById(decisao.getLoteTriagemId())) {
            throw new ResourceNotFoundException("Lote de triagem não encontrado: " + decisao.getLoteTriagemId());
        }

        if (decisao.getStatusAprovacao() == StatusAprovacao.REPROVADO && (decisao.getMotivo() == null || decisao.getMotivo().isBlank())) {
            throw new BusinessException("Motivo é obrigatório para reprovação");
        }

        LoteAprovacao salvo = loteAprovacaoRepository.save(decisao);

        loteHistoricoService.registrar(
                salvo.getLoteTriagemId(),
                TipoEventoHistorico.APROVACAO_REGISTRADA,
                "Decisão registrada: " + salvo.getStatusAprovacao() + " por " + salvo.getGestor()
        );

        return loteAprovacaoRepository.save(decisao);
    }

    public List<LoteAprovacao> listarPorLote(Integer loteTriagemId) {
        return loteAprovacaoRepository.findByLoteTriagemId(loteTriagemId);
    }
}
