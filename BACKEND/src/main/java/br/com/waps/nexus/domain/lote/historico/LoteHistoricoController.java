package br.com.waps.nexus.domain.lote.historico;

import br.com.waps.nexus.dto.LoteHistoricoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/lote-historico")
@RequiredArgsConstructor
public class LoteHistoricoController {

    private final LoteHistoricoService loteHistoricoService;

    @GetMapping("/{loteTriagemId}")
    public List<LoteHistoricoResponseDTO> listarPorLote(@PathVariable Integer loteTriagemId) {
        return loteHistoricoService.listarPorLote(loteTriagemId).stream()
                .map(h -> new LoteHistoricoResponseDTO(
                        h.getId(),
                        h.getTipoEvento(),
                        h.getDescricao(),
                        h.getUsuario(),
                        h.getDataEvento()))
                .toList();
    }
}
