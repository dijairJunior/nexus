package br.com.waps.nexus.domain.importacao;

import br.com.waps.nexus.domain.importacao.dto.ConfirmarArmRequest;
import br.com.waps.nexus.domain.importacao.dto.PreviewArmResponse;
import br.com.waps.nexus.domain.lote.triagem.LoteTriagem;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/produtos")
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @PostMapping("/importar-arm/preview")
    public ResponseEntity<PreviewArmResponse> preview(
            @RequestParam("arquivo")MultipartFile arquivo,
            @RequestParam("protocolo") String Protocolo) throws IOException {

        PreviewArmResponse resultado = importacaoService.gerarPreview(arquivo, Protocolo);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/importar-arm/confirmar")
    public ResponseEntity<LoteTriagem> confirma(@Valid @RequestBody ConfirmarArmRequest request) {
        LoteTriagem lote = importacaoService.confirmarImportacao(request);
        return ResponseEntity.ok(lote);
    }
}
