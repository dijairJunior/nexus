package br.com.waps.nexus.domain.contraprova;

import br.com.waps.nexus.dto.ContraprovaItemDTO;
import br.com.waps.nexus.dto.ContraprovaResumoDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contraprova")
public class ContraprovaController {

    private final ContraprovaService contraprovaService;

    public ContraprovaController(ContraprovaService contraprovaService) {
        this.contraprovaService = contraprovaService;
    }

    @GetMapping("/lote/{loteTriagemId}")
    public ContraprovaResumoDTO gerarContraprova(@PathVariable Integer loteTriagemId) {
        return contraprovaService.gerarContraprova(loteTriagemId);
    }
}
