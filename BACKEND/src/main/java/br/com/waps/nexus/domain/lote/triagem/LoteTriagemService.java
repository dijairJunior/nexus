package br.com.waps.nexus.domain.lote.triagem;

import br.com.waps.nexus.domain.lote.recebido.LoteRecebidoRepository;
import br.com.waps.nexus.domain.produto.ProdutoRepository;
import br.com.waps.nexus.dto.LoteResumoDTO;
import br.com.waps.nexus.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoteTriagemService {

    private final LoteTriagemRepository loteTriagemRepository;
    private final ProdutoRepository produtoRepository;
    private final LoteRecebidoRepository loteRecebidoRepository;

    public LoteTriagemService(LoteTriagemRepository loteTriagemRepository,
                              ProdutoRepository produtoRepository,
                              LoteRecebidoRepository loteRecebidoRepository) {
        this.loteTriagemRepository = loteTriagemRepository;
        this.produtoRepository = produtoRepository;
        this.loteRecebidoRepository = loteRecebidoRepository;
    }

    public List<LoteTriagem> listarTodos() {
        return loteTriagemRepository.findAll();
    }

    public LoteTriagem buscarPorId(Integer id) {
        return loteTriagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado"));
    }

    public LoteTriagem salvar(LoteTriagem lote) {
        return loteTriagemRepository.save(lote);
    }

    public Page<LoteResumoDTO> listar(FiltroLote filtro, Pageable pageable) {
        return loteTriagemRepository
                .findAll(LoteTriagemSpecification.comFiltro(filtro), pageable)
                .map(this::toResumoDTO);
    }

    private LoteResumoDTO toResumoDTO(LoteTriagem lote) {
        long qtdItens = produtoRepository.countByLoteTriagemId(lote.getId());
        long qtdConferidos = loteRecebidoRepository.countByLoteTriagemId(lote.getId());

        return new LoteResumoDTO(
                lote.getId(),
                lote.getDataLote(),
                lote.getStatus(),
                qtdItens,
                qtdConferidos
        );
    }
}
