package br.com.waps.nexus.domain.lote.recebido;

import br.com.waps.nexus.domain.lote.triagem.LoteTriagemRepository;
import br.com.waps.nexus.domain.produto.Produto;
import br.com.waps.nexus.domain.produto.ProdutoService;
import br.com.waps.nexus.dto.LoteRecebidoRequestDTO;
import br.com.waps.nexus.dto.LoteRecebidoResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class LoteRecebidoService {

    private static final Pattern IMEI_PATTERN = Pattern.compile("\\d{15}");

    private final LoteRecebidoRepository loteRecebidoRepository;
    private final LoteTriagemRepository loteTriagemRepository;
    private final ProdutoService produtoService;

    public LoteRecebidoService (LoteRecebidoRepository LoteRecebidoRepository,
                                LoteTriagemRepository LoteTriagemRepository,
                                ProdutoService ProdutoService) {

        this.loteRecebidoRepository = LoteRecebidoRepository;
        this.loteTriagemRepository = LoteTriagemRepository;
        this.produtoService = ProdutoService;
    }

    // ── Métodos públicos (usados pelo Controller, falam em DTO) ──────────

    public LoteRecebidoResponseDTO registrarConferencia(LoteRecebidoRequestDTO dto) {
        LoteRecebido loteRecebido = toEntity(dto);
        LoteRecebido salvo = salvarConferencia(loteRecebido);
        return toResponseDTO(salvo);
    }

    public List<LoteRecebidoResponseDTO> listarPorLote(Integer LoteTriagemId) {
        return loteRecebidoRepository.findByLoteTriagemId(LoteTriagemId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public LoteRecebidoResponseDTO buscarPorId(Long id) {
        LoteRecebido loteRecebido = buscarEntidadePorId(id);
        return toResponseDTO(loteRecebido);
    }

    // ── Lógica de negócio (trabalha só com Entity, igual antes) ──────────
    private LoteRecebido salvarConferencia(LoteRecebido loteRecebido) {

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

    private LoteRecebido buscarEntidadePorId(Long id) {
        return loteRecebidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lote recebido não encontrado para o ID: " + id));
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
            throw new RuntimeException("Número de Série (IMEI) é obrigatório");
        }
        if (!IMEI_PATTERN.matcher(numeroSerie).matches()) {
            throw new RuntimeException("IEMI inválido: deve conter exatamente 15 dígitos numéricos.");
        }
    }

    private void validarLoteTriagemExiste(Integer LoteTriagemId) {
        if (LoteTriagemId == null || !loteTriagemRepository.existsById(LoteTriagemId)) {
            throw new RuntimeException("Lote de triagem não encontrado para o ID informado.");
        }
    }

    private void validarDuplicidade(String numeroSerie, Integer loteTriagemId) {
        if (loteRecebidoRepository.existsByNumeroSerieAndLoteTriagemId(numeroSerie, loteTriagemId)) {
            throw new RuntimeException("Essa IMEI já foi cadastrada neste lote.");
        }
    }


    // ── Conversão Entity ↔ DTO ────────────────────────────────────────────

    private LoteRecebido toEntity(LoteRecebidoRequestDTO dto) {
        LoteRecebido loteRecebido = new LoteRecebido();

        loteRecebido.setNumeroSerie(dto.getNumeroSerie());
        loteRecebido.setEstetica(dto.getEstetica());
        loteRecebido.setDefeitoConstatadoId(dto.getDefeitoConstatadoId());
        loteRecebido.setStatusItem(dto.getStatusItem());
        loteRecebido.setResetado(dto.getResetado());

        loteRecebido.setLoteTriagemId(dto.getLoteTriagemId());

        loteRecebido.setTriador(dto.getTriador());

        loteRecebido.setStatusConferencia(dto.getStatusConferencia());
        loteRecebido.setObservacao(dto.getObservacao());

        return loteRecebido;
    }

    private LoteRecebidoResponseDTO toResponseDTO(LoteRecebido loteRecebido) {
        LoteRecebidoResponseDTO dto = new LoteRecebidoResponseDTO();

        dto.setId(loteRecebido.getId());

        dto.setNumeroSerie(loteRecebido.getNumeroSerie());
        dto.setEstetica(loteRecebido.getEstetica());
        dto.setDefeitoConstatadoId(loteRecebido.getDefeitoConstatadoId());
        dto.setClassificacao(loteRecebido.getClassificacao());
        dto.setStatusItem(loteRecebido.getStatusItem());
        dto.setResetado(loteRecebido.getResetado());

        dto.setLoteTriagemId(loteRecebido.getLoteTriagemId());

        dto.setTriador(loteRecebido.getTriador());
        dto.setDataConferencia(loteRecebido.getDataConferencia());

        dto.setStatusConferencia(loteRecebido.getStatusConferencia());
        dto.setObservacao(loteRecebido.getObservacao());

        return dto;
    }

}
