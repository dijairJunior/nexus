package br.com.waps.nexus.domain.produto;

import br.com.waps.nexus.domain.defeito.DefeitoConstatado;
import br.com.waps.nexus.domain.defeito.DefeitoConstatadoRepository;
import br.com.waps.nexus.dto.ProdutoRequestDTO;
import br.com.waps.nexus.dto.ProdutoResponseDTO;
import br.com.waps.nexus.exception.BusinessException;
import br.com.waps.nexus.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final DefeitoConstatadoRepository defeitoConstatadoRepository;

    public ProdutoService(ProdutoRepository produtoRepository,
                          DefeitoConstatadoRepository defeitoConstatadoRepository) {

        this.produtoRepository = produtoRepository;
        this.defeitoConstatadoRepository = defeitoConstatadoRepository;
    }

    // ── Métodos públicos (usados pelo Controller, falam em DTO) ──────────

    private void validarCapacidadeArmazenamento(Produto produto) {
        Integer capacidade = produto.getCapacidadeArmazenamento();

        if (capacidade == null) {
            throw new BusinessException("Capacidade de armazenamento é obrigatória.");
        }

        List<Integer> valoresValidos = List.of(64, 128, 256, 512, 1024);
        if (!valoresValidos.contains(capacidade)) {
            throw new BusinessException("Capacidade de armazenamento inválida. Valores aceitos: 64, 128, 256, 512, 1024.");
        }

        if ("NOVO".equalsIgnoreCase(produto.getStatusItem()) && produto.getPossuiMarcasUso() == null) {
            throw new BusinessException("É obrigatório informar se o item possui marcas de uso.");
        }
    }

    public List<ProdutoResponseDTO> listarTodos() {
        return produtoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = buscarEntidadePorId(id);
        return toResponseDTO(produto);
    }

    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        Produto produto = toEntity(dto);
        Produto salvo = salvar(produto);
        return toResponseDTO(salvo);
    }

    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        Produto existente = buscarEntidadePorId(id);
        Produto produto = toEntity(dto);
        produto.setId(id);
        produto.setTriador(existente.getTriador()); // preserva quem fez a triagem original
        Produto atualizado = salvar(produto);
        return toResponseDTO(atualizado);
    }

    public void deletar(Long id) {
        produtoRepository.deleteById(id);
    }

    // ── Lógica de negócio (trabalha só com Entity, igual antes) ──────────
    private Produto buscarEntidadePorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Produto não encontrado!"));
    }

    private Produto salvar(Produto produto) {
        validarCapacidadeArmazenamento(produto);
        String classificacao = calcularClassificacao(produto);
        produto.setClassificacao(classificacao);

        if (produto.getId() == null) {

            if (produto.getNumeroSerie() == null || produto.getNumeroSerie().isBlank()) {
                throw new BusinessException("IMEI é obrigatório");
            }
            if (produto.getNumeroSerie().length() != 15) {
                throw new BusinessException("IMEI deve ter exatamente 15 dígitos");
            }
            if (produtoRepository.existsByNumeroSerie(produto.getNumeroSerie())) {
                throw new BusinessException("Já existe um produto cadastrado com este IMEI: " + produto.getNumeroSerie());
            }

            String usuarioLogado = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();
            produto.setTriador(usuarioLogado);
        }

        // Classificação automática sempre que salvar (criação ou atualização)
        produto.setClassificacao((calcularClassificacao(produto)));

        return produtoRepository.save(produto);
    }

    public String calcularClassificacao(Produto produto) {

        if ("NOVO".equalsIgnoreCase(produto.getStatusItem())) {
            boolean possuiMarcas = Boolean.TRUE.equals(produto.getPossuiMarcasUso());

            if (!possuiMarcas) {
                return "A";
            }

            return avaliarPorEstetica(produto.getEstetica());
        }

        //OBSOLETO, TRIADO ou não informado → avalia defeito e estetica
        if (produto.getDefeitoConstatadoId() != null) {
            DefeitoConstatado defeito = defeitoConstatadoRepository.findById(produto.getDefeitoConstatadoId())
                    .orElse(null);

            if (defeito != null && !"SEM DEFEITO".equalsIgnoreCase(defeito.getDescricao())) {
                return "D";
            }
        }

        if (isResetadoNao(produto.getResetado())) {
            return "D";
        }

        return avaliarPorEstetica(produto.getEstetica());
    }

    private String avaliarPorEstetica(String estetica) {
        if ("BOM".equalsIgnoreCase(estetica)) {
            return "A";
        } else if ("RISCOS LEVES".equalsIgnoreCase(estetica)) {
            return "B";
        } else if ("RISCOS PROFUNDOS".equalsIgnoreCase(estetica)) {
            return "C";
        } else {
            return "D";
        }
    }

    private boolean isResetadoNao(String resetado) {
        if (resetado == null) return false;
        String semAcento = Normalizer.normalize(resetado.trim(), Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return "NAO".equalsIgnoreCase(semAcento);
    }

    // ── Conversão Entity ↔ DTO ────────────────────────────────────────────
    private Produto toEntity(ProdutoRequestDTO dto){
        Produto produto = new Produto();
        produto.setNumero(dto.getNumero());
        produto.setPreenchimentoObrig(dto.getPreenchimentoObrig());
        produto.setCodigoSap(dto.getCodigoSap());
        produto.setDescricao(dto.getDescricao());
        produto.setQuantidade(dto.getQuantidade());
        produto.setModelo(dto.getModelo());

        produto.setNumeroPatrimonio(dto.getNumeroPatrimonio());
        produto.setNumeroSerie(dto.getNumeroSerie());
        produto.setSgp(dto.getSgp());
        produto.setNumeroImobilizado(dto.getNumeroImobilizado());
        produto.setSubNumeroImobilizado(dto.getSubNumeroImobilizado());

        produto.setCondicaoBem(dto.getCondicaoBem());
        produto.setAlienarOuArmazenar(dto.getAlienarOuArmazenar());
        produto.setStatusProcesso(dto.getStatusProcesso());
        produto.setStatusVenda(dto.getStatusVenda());
        produto.setStatus2(dto.getStatus2());
        produto.setStatusCadastro(dto.getStatusCadastro());

        produto.setCustoAquisicao(dto.getCustoAquisicao());
        produto.setSaldoContabil(dto.getSaldoContabil());

        produto.setUf(dto.getUf());
        produto.setNfVenda(dto.getNfVenda());
        produto.setReversaClaro(dto.getReversaClaro());

        produto.setDataCompra(dto.getDataCompra());
        produto.setDataSeparacao(dto.getDataSeparacao());
        produto.setDataEntradaNasajon(dto.getDataEntradaNasajon());

        produto.setLoteTriagemId(dto.getLoteTriagemId());

        produto.setCor(dto.getCor());
        produto.setCapacidade(dto.getCapacidade());
        produto.setEstetica(dto.getEstetica());
        produto.setRealizadoRecovery(dto.getRealizadoRecovery());
        produto.setValidaImei(dto.getValidaImei());
        produto.setTipoTriagem(dto.getTipoTriagem());

        produto.setNfDevolucao(dto.getNfDevolucao());
        produto.setDataNfDevolucao(dto.getDataNfDevolucao());
        produto.setCentroDistribuicao(dto.getCentroDistribuicao());

        produto.setStatusItem(dto.getStatusItem());
        produto.setDefeitoConstatadoId(dto.getDefeitoConstatadoId());

        produto.setResetado(dto.getResetado());

        return produto;
    }

    private ProdutoResponseDTO toResponseDTO(Produto produto) {
        ProdutoResponseDTO dto = new ProdutoResponseDTO();
        dto.setId(produto.getId());

        dto.setNumero(produto.getNumero());
        dto.setPreenchimentoObrig(produto.getPreenchimentoObrig());
        dto.setCodigoSap(produto.getCodigoSap());
        dto.setDescricao(produto.getDescricao());
        dto.setQuantidade(produto.getQuantidade());
        dto.setModelo(produto.getModelo());

        dto.setNumeroPatrimonio(produto.getNumeroPatrimonio());
        dto.setNumeroSerie(produto.getNumeroSerie());
        dto.setSgp(produto.getSgp());
        dto.setNumeroImobilizado(produto.getNumeroImobilizado());
        dto.setSubNumeroImobilizado(produto.getSubNumeroImobilizado());

        dto.setCondicaoBem(produto.getCondicaoBem());
        dto.setAlienarOuArmazenar(produto.getAlienarOuArmazenar());
        dto.setStatusProcesso(produto.getStatusProcesso());
        dto.setStatusVenda(produto.getStatusVenda());
        dto.setStatus2(produto.getStatus2());
        dto.setStatusCadastro(produto.getStatusCadastro());

        dto.setCustoAquisicao(produto.getCustoAquisicao());
        dto.setClassificacao(produto.getClassificacao());
        dto.setSaldoContabil(produto.getSaldoContabil());

        dto.setUf(produto.getUf());
        dto.setNfVenda(produto.getNfVenda());
        dto.setReversaClaro(produto.getReversaClaro());

        dto.setDataCompra(produto.getDataCompra());
        dto.setDataSeparacao(produto.getDataSeparacao());
        dto.setDataEntradaNajason(produto.getDataEntradaNasajon());

        dto.setLoteTriagemId(produto.getLoteTriagemId());

        dto.setCor(produto.getCor());
        dto.setCapacidade(produto.getCapacidade());
        dto.setEstetica(produto.getEstetica());
        dto.setRealizadoRecovery(produto.getRealizadoRecovery());
        dto.setTriador(produto.getTriador());
        dto.setValidaImei(produto.getValidaImei());
        dto.setTipoTriagem(produto.getTipoTriagem());

        dto.setNfDevolucao(produto.getNfDevolucao());
        dto.setDataNfDevolucao(produto.getDataNfDevolucao());
        dto.setCentroDistribuicao(produto.getCentroDistribuicao());

        dto.setCriadoEm(produto.getCriadoEm());
        dto.setAtualizadoEm(produto.getAtualizadoEm());

        dto.setStatusItem(produto.getStatusItem());
        dto.setDefeitoConstatadoId(produto.getDefeitoConstatadoId());

        dto.setResetado(produto.getResetado());

        return dto;
    }

    public List<ProdutoResponseDTO> listarPorLoteTriagemId(Integer loteTriagemId) {
        return produtoRepository.findByLoteTriagemId(loteTriagemId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ProdutoResponseDTO buscarPorNumeroSerie(String numeroSerie) {
        Produto produto = produtoRepository.findByNumeroSerie(numeroSerie)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado para este IMEI: " + numeroSerie));
        return toResponseDTO(produto);
    }

    public List<ProdutoResponseDTO> buscarPorNumerosSerie(List<String> numerosSerie) {
        return produtoRepository.findByNumeroSerieIn(numerosSerie)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }
}
