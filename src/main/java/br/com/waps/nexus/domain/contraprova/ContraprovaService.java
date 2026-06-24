package br.com.waps.nexus.domain.contraprova;

import br.com.waps.nexus.domain.lote.LoteRecebido;
import br.com.waps.nexus.domain.lote.LoteRecebidoRepository;
import br.com.waps.nexus.domain.lote.LoteTriagemRepository;
import br.com.waps.nexus.domain.produto.Produto;
import br.com.waps.nexus.domain.produto.ProdutoRepository;
import br.com.waps.nexus.dto.ContraprovaItemDTO;
import br.com.waps.nexus.dto.ContraprovaItemDTO.StatusContraprova;
import br.com.waps.nexus.dto.ContraprovaResumoDTO;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContraprovaService {

    private final ProdutoRepository produtoRepository;
    private final LoteRecebidoRepository loteRecebidoRepository;
    private final LoteTriagemRepository loteTriagemRepository;

    public ContraprovaService(ProdutoRepository produtoRepository,
                              LoteRecebidoRepository loteRecebidoRepository,
                              LoteTriagemRepository loteTriagemRepository) {
        this.produtoRepository = produtoRepository;
        this.loteRecebidoRepository = loteRecebidoRepository;
        this.loteTriagemRepository = loteTriagemRepository;
    }

    public ContraprovaResumoDTO gerarContraprova(Integer loteTriagemId) {
        if (!loteTriagemRepository.existsById(loteTriagemId)) {
            throw new RuntimeException("Lote de triagem não encontrado: " + loteTriagemId);
        }

        List<Produto> esperados = produtoRepository.findByLoteTriagemId(loteTriagemId);
        List<LoteRecebido> recebidos = loteRecebidoRepository.findByLoteTriagemId(loteTriagemId);

        Map<String, Produto> mapaEsperados = new HashMap<>();
        for (Produto p : esperados) {
            mapaEsperados.put(p.getNumeroSerie(), p);
        }

        Map<String, LoteRecebido> mapaRecebidos = new HashMap<>();
        for (LoteRecebido r : recebidos) {
            mapaRecebidos.put(r.getNumeroSerie(), r);
        }

        Set<String> todosNumerosSerie = new LinkedHashSet<>();
        todosNumerosSerie.addAll(mapaEsperados.keySet());
        todosNumerosSerie.addAll(mapaRecebidos.keySet());

        List<ContraprovaItemDTO> itens = new ArrayList<>();
        int totalOk = 0, totalRecebidoNaoPrevisto = 0, totalPrevistoNaoRecebido = 0, totalDivergencia = 0;

        for (String numeroSerie : todosNumerosSerie) {
            Produto produto = mapaEsperados.get(numeroSerie);
            LoteRecebido recebido = mapaRecebidos.get(numeroSerie);

            String classificacaoEsperada = produto != null ? produto.getClassificacao() : null;
            String classificacaoRecebida = recebido != null ? recebido.getClassificacao() : null;

            StatusContraprova status;

            if (produto == null) {
                status = StatusContraprova.RECEBIDO_NAO_PREVISTO;
                totalRecebidoNaoPrevisto++;
            } else if (recebido == null) {
                status = StatusContraprova.PREVISTO_NAO_RECEBIDO;
                totalPrevistoNaoRecebido++;
            } else if (!Objects.equals(classificacaoEsperada, classificacaoRecebida)) {
                status = StatusContraprova.DIVERGENCIA_CLASSIFICACAO;
                totalDivergencia++;
            } else {
                status = StatusContraprova.OK;
                totalOk++;
            }

            itens.add(new ContraprovaItemDTO(numeroSerie, status, classificacaoEsperada, classificacaoRecebida));
        }

        ContraprovaResumoDTO resumo = new ContraprovaResumoDTO(loteTriagemId, itens);
        resumo.setTotalEsperado(esperados.size());
        resumo.setTotalRecebido(recebidos.size());
        resumo.setTotalOk(totalOk);
        resumo.setTotalRecebidoNaoPrevisto(totalRecebidoNaoPrevisto);
        resumo.setTotalPrevistoNaoRecebido(totalPrevistoNaoRecebido);
        resumo.setTotalDivergenciaClassificacao(totalDivergencia);

        return resumo;
    }
}