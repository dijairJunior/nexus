package br.com.waps.nexus.domain.produto;

import br.com.waps.nexus.dto.ProdutoRequestDTO;
import br.com.waps.nexus.dto.ProdutoResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listarTodos(){
        return ResponseEntity.ok(produtoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id){
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(@RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO salvo = produtoService.criar(dto);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Long id,
                                                        @RequestBody ProdutoRequestDTO dto){

        ProdutoResponseDTO atualizado = produtoService.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id){
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lote/{loteTriagemId}")
    public ResponseEntity<List<ProdutoResponseDTO>> listarPorLote(@PathVariable Integer loteTriagemId) {
        return ResponseEntity.ok(produtoService.listarPorLoteTriagemId(loteTriagemId));
    }

    // [BUSCA-POR-SERIE] início — usado pra autopreencher o modelo na Conferência
    @GetMapping("/serie/{numeroSerie}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorNumeroSerie(@PathVariable String numeroSerie) {
        return ResponseEntity.ok(produtoService.buscarPorNumeroSerie(numeroSerie));
    }
    // [BUSCA-EM-LOTE] início — usado pra autopreencher modelo dos itens conferidos de uma vez
    @PostMapping("/buscar-por-series")
    public ResponseEntity<List<ProdutoResponseDTO>> buscarPorSeries(@RequestBody List<String> numerosSerie) {
        return ResponseEntity.ok(produtoService.buscarPorNumerosSerie(numerosSerie));
    }
}
