package br.com.waps.nexus.domain.lote.aprovacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lote-aprovacao")
public class LoteAprovacaoController {

    private final LoteAprovacaoService loteAprovacaoService;

    public LoteAprovacaoController(LoteAprovacaoService loteAprovacaoService) {
        this.loteAprovacaoService = loteAprovacaoService;
    }

    @PostMapping
    public ResponseEntity<LoteAprovacao> registrar(@RequestBody LoteAprovacao decisao) {
        LoteAprovacao salvo = loteAprovacaoService.registrarDecisao(decisao);
        return ResponseEntity.ok(salvo);
    }

    @GetMapping("/lote/{loteTriagemId}")
    public ResponseEntity<List<LoteAprovacao>> listarPorLote(@PathVariable Integer loteTriagemId) {
        List<LoteAprovacao> historico = loteAprovacaoService.listarPorLote(loteTriagemId);
        return ResponseEntity.ok(historico);
    }
}
