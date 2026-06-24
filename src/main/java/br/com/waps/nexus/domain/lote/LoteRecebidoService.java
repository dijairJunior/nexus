package br.com.waps.nexus.domain.lote;

import br.com.waps.nexus.domain.produto.Produto;
import br.com.waps.nexus.domain.produto.ProdutoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class LoteRecebidoService {

    private static final Pattern IMEI_PATTERN = Pattern.compile("\\d{15}");

    private final LoteRecebidoRepository loteRecebidoRepository;
    private final LoteTriagemRepository loteTriagemRepository;
    private final ProdutoService produtoService;

    public LoteRecebidoService(LoteRecebidoRepository loteRecebidoRepository,
                               LoteTriagemRepository loteTriagemRepository,
                               ProdutoService produtoService) {

        this.loteRecebidoRepository = loteRecebidoRepository;
        this.loteTriagemRepository = loteTriagemRepository;
        this.produtoService = produtoService;
    }

    public LoteRecebido registrarConferencia(LoteRecebido loteRecebido) {

        validarNumeroSerie(loteRecebido.getNumeroSerie());
        validarLoteTriagemExiste(loteRecebido.getLoteTriagemId());
        validarDuplicidade(loteRecebido.getNumeroSerie(), loteRecebido.getLoteTriagemId());

        String classificacao = calcularClassificacao(loteRecebido);
        loteRecebido.setClassificacao(classificacao);

        if (loteRecebido.getStatusConferencia() == null || loteRecebido.getStatusConferencia().isBlank()) {
            loteRecebido.setStatusConferencia("PENDENTE");
        }

        return loteRecebidoRepository.save(loteRecebido);
    }

    public List<LoteRecebido> listarPorLote(Integer loteTriagemId) {
        return loteRecebidoRepository.findByLoteTriagemId(loteTriagemId);
    }

    private String calcularClassificacao(LoteRecebido loteRecebido) {
        Produto produtoTemp = new Produto();
        produtoTemp.setStatusItem(loteRecebido.getStatusItem());
        produtoTemp.setDefeitoConstatadoId(loteRecebido.getDefeitoConstatadoId());
        produtoTemp.setResetado(loteRecebido.getResetado());
        produtoTemp.setEstetica(loteRecebido.getEstetica());

        return produtoService.calcularClassificacao(produtoTemp);
    }

    private void validarNumeroSerie(String numeroSerie) {
        if (numeroSerie == null || numeroSerie.isBlank()) {
            throw new RuntimeException("Número de série (IMEI) é obrigatório.");
        }
        if (!IMEI_PATTERN.matcher(numeroSerie).matches()) {
            throw new RuntimeException("IMEI inválido: deve conter exatamente 15 dígitos numéricos.");
        }
    }

    private void validarLoteTriagemExiste(Integer loteTriagemId) {
        if (loteTriagemId == null || !loteTriagemRepository.existsById(loteTriagemId)) {
            throw new RuntimeException("Lote de triagem não encontrado para o ID informado.");
        }
    }

    private void validarDuplicidade(String numeroSerie, Integer loteTriagemId) {
        if (loteRecebidoRepository.existsByNumeroSerieAndLoteTriagemId(numeroSerie, loteTriagemId)) {
            throw new RuntimeException("Essa IMEI já foi cadastrada neste lote.");
        }
    }

    public LoteRecebido buscarPorId(Long id) {
        return loteRecebidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lote recebido não encontrado para o ID: " + id));
    }
}
