package br.com.waps.nexus.domain.relatorio.lote;

import br.com.waps.nexus.domain.contraprova.ContraprovaService;
import br.com.waps.nexus.domain.lote.aprovacao.LoteAprovacaoService;
import br.com.waps.nexus.domain.lote.recebido.LoteRecebidoService;
import br.com.waps.nexus.domain.lote.triagem.LoteTriagemRepository;
import br.com.waps.nexus.domain.produto.ProdutoService;
import br.com.waps.nexus.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquestra a geração do relatório de lote em Excel.
 * Responsabilidade: buscar dados via os Services já existentes (Produto, LoteRecebido,
 * Contraprova, LoteAprovacao) e delegar a montagem do arquivo ao RelatorioExcelBuilder.
 * Não reimplementa nenhuma regra de negócio — apenas consome o que já existe.
 */
@Service
@RequiredArgsConstructor
public class RelatorioLoteService {

    private final LoteTriagemRepository loteTriagemRepository;
    private final ProdutoService produtoService;
    private final LoteRecebidoService loteRecebidoService;
    private final ContraprovaService contraprovaService;
    private final LoteAprovacaoService loteAprovacaoService;

    // Dependências dos 4 Services existentes serão injetadas aqui
    // conforme cada um for integrado (etapas 4 a 7).
    // Ainda não injetado nesta etapa (PoC primeiro, sem dependências externas).

    public byte[] gerarRelatorioExcel(Integer loteTriageId,
                                      boolean incluirProduto,
                                      boolean incluirLoteRecebido,
                                      boolean incluirContraprova,
                                      boolean incluirAprovacao) {

        validarLoteExiste(loteTriageId);

        RelatorioExcelBuilder builder = new RelatorioExcelBuilder();

        List<String> abasIncluidas = new ArrayList<>();
        if (incluirProduto) abasIncluidas.add("Produto");
        if (incluirLoteRecebido) abasIncluidas.add("LoteRecebido");
        if (incluirContraprova) abasIncluidas.add("Contraprova");
        if (incluirAprovacao) abasIncluidas.add("Aprovação");

        builder.adicionarAbaInfo(loteTriageId, obterUsuarioAutenticado(), abasIncluidas);

        if (incluirProduto) {
            builder.adicionarAbaProduto(produtoService.listarPorLoteTriagemId(loteTriageId));
        }

        if (incluirLoteRecebido) {
            builder.adicionarAbaLoteRecebido(loteRecebidoService.listarPorLote(loteTriageId));
        }

        if (incluirContraprova) {
            builder.adicionarAbaContraprova(contraprovaService.gerarContraprova(loteTriageId));
        }

        if (incluirAprovacao) {
            builder.adicionarAbaAprovacao(loteAprovacaoService.listarPorLote(loteTriageId));
        }

        // PoC (etapa 3): nenhuma aba real ainda, só confirma que o fluxo
        // Controller → Service → Builder → bytes funciona de ponta a ponta.

        return builder.gerarBytes();
    }

    /**
     * Garante que o lote de triagem existe antes de gerar qualquer aba.
     * Um relatório de lote inexistente não é "relatório vazio" — é recurso
     * inexistente, e deve retornar 404, não um .xlsx vazio.
     */
    private void validarLoteExiste(Integer loteTriagemId) {
        if (!loteTriagemRepository.existsById(loteTriagemId)) {
            throw new ResourceNotFoundException("Lote de triagem não encontrado: " + loteTriagemId);
        }
    }

    /**
     * Monta o nome do arquivo padronizado (sem data — data/hora de geração
     * fica dentro do arquivo, na aba Info):
     * - Relatório completo (todas as 4 abas): Compilado_Triagem_Lote_{id}.xlsx
     * - Relatório parcial (qualquer subconjunto): Relatorio_Lote_{id}.xlsx
     */
    public String gerarNomeArquivo(Integer loteTriagemId,
                                   boolean incluirProduto,
                                   boolean incluirLoteRecebido,
                                   boolean incluirContraprova,
                                   boolean incluirAprovacao) {

        boolean completo = incluirAprovacao && incluirContraprova
                && incluirLoteRecebido && incluirProduto;

        if (completo) {
            return "Compilado_Triagem_Lote_" + loteTriagemId + ".xlsx";
        }
        return "Relatorio_Lote_" + loteTriagemId + ".xlsx";
    }

    /**
     * Obtém o login do usuário autenticado via JWT (SecurityContext).
     * Retorna null se não houver autenticação (não deve ocorrer em endpoint protegido).
     */
    private String obterUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
}