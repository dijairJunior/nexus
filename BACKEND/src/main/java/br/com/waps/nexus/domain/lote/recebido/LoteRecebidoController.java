package br.com.waps.nexus.domain.lote.recebido;

import br.com.waps.nexus.dto.LoteRecebidoRequestDTO;
import br.com.waps.nexus.dto.LoteRecebidoResponseDTO;
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
    public ResponseEntity<LoteRecebidoResponseDTO> registrarConferencia(@RequestBody LoteRecebidoRequestDTO dto) {
        LoteRecebidoResponseDTO salvo = loteRecebidoService.registrarConferencia(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping("/lote/{loteTriagemId}")
    public ResponseEntity<List<LoteRecebidoResponseDTO>> listarPorLote(@PathVariable Integer loteTriagemId) {
        List<LoteRecebidoResponseDTO> lista = loteRecebidoService.listarPorLote(loteTriagemId);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoteRecebidoResponseDTO> buscaPorId(@PathVariable Long id) {
        LoteRecebidoResponseDTO loteRecebido = loteRecebidoService.buscarPorId(id);
        return ResponseEntity.ok(loteRecebido);
    }
}