package br.com.waps.nexus.domain.relatorio.lote;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatorio/lote")
public class RelatorioLoteController {

    private final RelatorioLoteService relatorioLoteService;

    public RelatorioLoteController(RelatorioLoteService relatorioLoteService) {
        this.relatorioLoteService = relatorioLoteService;
    }

    @GetMapping("/{loteTriagemId}/excel")
    public ResponseEntity<byte[]> exporatExcel(
            @PathVariable Integer loteTriagemId,
            @RequestParam(required = false) Boolean produto,
            @RequestParam(required = false) Boolean loteRecebido,
            @RequestParam(required = false) Boolean contraprova,
            @RequestParam(required = false) Boolean aprovacao) {

        // Comportamento padrão: nenhum param informado -> gerar as 4 abas
        boolean nenhumParamInformado = produto == null && loteRecebido == null
                && contraprova == null && aprovacao == null;

        boolean incluirProduto = nenhumParamInformado || Boolean.TRUE.equals(produto);
        boolean incluirLoteRecebido = nenhumParamInformado || Boolean.TRUE.equals(loteRecebido);
        boolean incluiContraprova = nenhumParamInformado || Boolean.TRUE.equals(contraprova);
        boolean incluirAprovacao = nenhumParamInformado || Boolean.TRUE.equals(aprovacao);

        byte[] arquivo = relatorioLoteService.gerarRelatorioExcel(
                loteTriagemId, incluirProduto, incluirLoteRecebido, incluiContraprova, incluirAprovacao
        );

        String nomeArquivo = relatorioLoteService.gerarNomeArquivo(
                loteTriagemId, incluirProduto, incluirLoteRecebido, incluiContraprova, incluirAprovacao
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }
}
