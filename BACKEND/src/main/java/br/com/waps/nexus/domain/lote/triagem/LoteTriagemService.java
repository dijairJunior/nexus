package br.com.waps.nexus.domain.lote.triagem;

import br.com.waps.nexus.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoteTriagemService {

    private final LoteTriagemRepository loteTriagemRepository;

    public LoteTriagemService(LoteTriagemRepository loteTriagemRepository) {
        this.loteTriagemRepository = loteTriagemRepository;
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
}
