package br.com.waps.nexus.domain.lote.triagem;

import br.com.waps.nexus.dto.LoteResumoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lotes-triagem")
public class LoteTriagemController {

    private final LoteTriagemService loteTriagemService;

    public LoteTriagemController(LoteTriagemService loteTriagemService) {
        this.loteTriagemService = loteTriagemService;
    }

    @GetMapping
    public ResponseEntity<List<LoteTriagem>> listarTodos() {
        return ResponseEntity.ok(loteTriagemService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoteTriagem> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(loteTriagemService.buscarPorId(id));
    }

    @GetMapping("/paginado")
    public Page<LoteResumoDTO> listar(
            @RequestParam(required = false) StatusLote status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        FiltroLote filtro = new FiltroLote(status, search);
        return loteTriagemService.listar(filtro, pageable);
    }
}
