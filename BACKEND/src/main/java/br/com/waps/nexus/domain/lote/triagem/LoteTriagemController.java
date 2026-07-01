package br.com.waps.nexus.domain.lote.triagem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
