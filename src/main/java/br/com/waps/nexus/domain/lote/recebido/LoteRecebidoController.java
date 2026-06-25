package br.com.waps.nexus.domain.lote.recebido;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lote-recebido")
public class LoteRecebidoController {

    private final LoteRecebidoService loteRecebidoService;

    public LoteRecebidoController(LoteRecebidoService loteRecebidoService) {
        this.loteRecebidoService = loteRecebidoService;
    }

    @PostMapping("/registrar-conferencia")
    public ResponseEntity<LoteRecebido> registrarConferencia(@RequestBody LoteRecebido loteRecebido) {
        LoteRecebido salvo = loteRecebidoService.registrarConferencia(loteRecebido);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping("/lote/{loteTriagemId}")
    public ResponseEntity<List<LoteRecebido>> listarPorLote(@PathVariable Integer loteTriagemId) {
        List<LoteRecebido> lista = loteRecebidoService.listarPorLote(loteTriagemId);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoteRecebido> buscaPorId(@PathVariable Long id) {
        LoteRecebido loteRecebido = loteRecebidoService.buscarPorId(id);
        return ResponseEntity.ok(loteRecebido);
    }
}
