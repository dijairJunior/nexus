package br.com.waps.nexus.domain.relatorio.lote;

import br.com.waps.nexus.domain.lote.aprovacao.LoteAprovacao;
import br.com.waps.nexus.domain.lote.aprovacao.StatusAprovacao;
import br.com.waps.nexus.dto.ContraprovaItemDTO;
import br.com.waps.nexus.dto.ContraprovaResumoDTO;
import br.com.waps.nexus.dto.LoteRecebidoResponseDTO;
import br.com.waps.nexus.dto.ProdutoResponseDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Responsável exclusivamente por matar o Workbook (.xlxs) e gera os bytes finais
 * Não acessa Repository/Service - recebe os dados já prontos do RelatorioLoteService.
 * Separada do Service para permitir troca futura por outro formato (PDF/CSV)
 * sem alterar a lógica de busca de dados.
 */

public class RelatorioExcelBuilder {

    private final Workbook workbook;

    public RelatorioExcelBuilder() {
        this.workbook = new XSSFWorkbook();
    }

    /**
     * Gera os bytes finais do arquivo .xlsx montado.
     * Chama por último, depois todas as abas adicionadas.
     */
    public byte[] gerarBytes() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao gerar arquivo Excel do relatório", e);
        }
    }

    /**
     * Adiciona a aba "Info", presente em todo relatório gerado.
     * Contém metadados de geração: tipo do relatório, lote, data/hora,
     * usuário e abas incluídas. Nunca inclui dados de negócio.
     */
    public void adicionarAbaInfo(Integer loteTriageId,
                                 String usuarioGerador,
                                 List<String> abasIncluidas) {

        Sheet sheet = workbook.createSheet("Info");

        CellStyle labelStyle = workbook.createCellStyle();
        Font labelFont = workbook.createFont();
        labelFont.setBold(true);
        labelStyle.setFont(labelFont);

        String dataHora = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        String tipoRelatorio = abasIncluidas.size() == 4 ? "Compilado" : "Parcial";

        int rowIdx = 0;
        rowIdx = adicionarLinhaInfo(sheet, labelStyle, rowIdx, "Tipo de Relatório", tipoRelatorio);
        rowIdx = adicionarLinhaInfo(sheet, labelStyle, rowIdx, "Lote", String.valueOf(loteTriageId));
        rowIdx = adicionarLinhaInfo(sheet, labelStyle, rowIdx, "Data/Hora de Geração", dataHora);
        rowIdx = adicionarLinhaInfo(sheet, labelStyle, rowIdx, "Usuário", usuarioGerador != null ? usuarioGerador : "-");
        adicionarLinhaInfo(sheet, labelStyle, rowIdx, "Abas Incluídas", String.join(", ", abasIncluidas));

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private int adicionarLinhaInfo(Sheet sheet, CellStyle labelStyle, int rowIdx, String label, String valor) {
        Row row = sheet.createRow(rowIdx);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        row.createCell(1).setCellValue(valor);
        return rowIdx + 1;
    }

    /**
     * Adiciona a aba "Produto" com os itens triados do lote.
     * Colunas focadas no essencial para avaliação gerencial rápida.
     */
    public void adicionarAbaProduto(List<ProdutoResponseDTO> produto) {
        Sheet sheet = workbook.createSheet("Produto");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] colunas = {
                "Número Série", "Modelo", "Classificação", "Estética",
                "Status Item", "Condição do Bem", "Triador", "Data Entrada"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < colunas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(colunas[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (ProdutoResponseDTO p : produto) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(nullSafe(p.getNumeroSerie()));
            row.createCell(1).setCellValue(nullSafe(p.getModelo()));
            row.createCell(2).setCellValue(nullSafe(p.getClassificacao()));
            row.createCell(3).setCellValue(nullSafe(p.getEstetica()));
            row.createCell(4).setCellValue(nullSafe(p.getStatusItem()));
            row.createCell(5).setCellValue(nullSafe(p.getCondicaoBem()));
            row.createCell(6).setCellValue(nullSafe(p.getTriador()));
            row.createCell(7).setCellValue(p.getDataEntradaNajason() != null ? p.getDataEntradaNajason().toString() : "-");
        }

        for (int i = 0; i < colunas.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Adiciona a aba "LoteRecebido" com os itens conferidos fisicamente do lote.
     */
    public void adicionarAbaLoteRecebido(List<LoteRecebidoResponseDTO> itens) {
        Sheet sheet = workbook.createSheet("LoteRecebido");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] colunas = {
                "Número Série", "Classificação", "Estética", "Status Item",
                "Status Conferência", "Triador", "Observação"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < colunas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(colunas[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (LoteRecebidoResponseDTO item : itens) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(nullSafe(item.getNumeroSerie()));
            row.createCell(1).setCellValue(nullSafe(item.getClassificacao()));
            row.createCell(2).setCellValue(nullSafe(item.getEstetica()));
            row.createCell(3).setCellValue(nullSafe(item.getStatusItem()));
            row.createCell(4).setCellValue(nullSafe(item.getStatusConferencia()));
            row.createCell(5).setCellValue(nullSafe(item.getTriador()));
            row.createCell(6).setCellValue(nullSafe(item.getObservacao()));
        }

        for (int i = 0; i < colunas.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Adiciona a aba "Contraprova" com o resumo comparativo entre
     * itens esperados (Produto) e recebidos (LoteRecebido).
     */
    public void adicionarAbaContraprova(ContraprovaResumoDTO resumo) {

        Sheet sheet = workbook.createSheet("Contraprova");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        CellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(headerFont);

        // Bloco de totais no topo
        int rowIdx = 0;
        rowIdx = adicionarLinhaTotal(sheet, boldStyle, rowIdx, "Total esperado", resumo.getTotalEsperado());
        rowIdx = adicionarLinhaTotal(sheet, boldStyle, rowIdx, "Total recebido", resumo.getTotalRecebido());
        rowIdx = adicionarLinhaTotal(sheet, boldStyle, rowIdx, "Total Ok", resumo.getTotalOk());
        rowIdx = adicionarLinhaTotal(sheet, boldStyle, rowIdx, "Recebido / previsto", resumo.getTotalRecebidoNaoPrevisto());
        rowIdx = adicionarLinhaTotal(sheet, boldStyle, rowIdx, "Previsto / recebido", resumo.getTotalPrevistoNaoRecebido());
        rowIdx = adicionarLinhaTotal(sheet, boldStyle, rowIdx, "Divergência de classificação", resumo.getTotalDivergenciaClassificacao());

        rowIdx++; // linha em branco antes da tabela de itens

        // Cabeçalho da tabela de itens
        String[] colunas = {"Número Série", "Status", "Classificação Esperada", "Classificação Recebida"};
        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < colunas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(colunas[i]);
            cell.setCellStyle(headerStyle);
        }

        for (ContraprovaItemDTO item : resumo.getItens()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(nullSafe(item.getNumeroSerie()));
            row.createCell(1).setCellValue(item.getStatus() != null ? item.getStatus().name() : "-");
            row.createCell(2).setCellValue(nullSafe(item.getClassificacaoEsperada()));
            row.createCell(3).setCellValue(nullSafe(item.getClassificacaoRecebida()));
        }

        for (int i = 0; i < colunas.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private int adicionarLinhaTotal(Sheet sheet, CellStyle labelStyle, int rowIdx, String Label, int valor) {
        Row row = sheet.createRow(rowIdx);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(Label);
        labelCell.setCellStyle(labelStyle);
        row.createCell(1).setCellValue(valor);
        return rowIdx + 1;
    }

    /**
     * Adiciona a aba "Aprovacao" com o histórico de decisões de aprovação/reprovação do lote.
     */
    public void adicionarAbaAprovacao(List<LoteAprovacao> historico) {

        Sheet sheet = workbook.createSheet("Aprovação");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        long totalAprovados = historico.stream()
                .filter(d -> d.getStatusAprovacao() == StatusAprovacao.APROVADO)
                .count();
        long totalReprovados = historico.stream()
                .filter(d -> d.getStatusAprovacao() == StatusAprovacao.REPROVADO)
                .count();

        int rowIdx = 0;
        rowIdx = adicionarLinhaTotal(sheet, headerStyle, rowIdx, "Total de Decisões", historico.size());
        rowIdx = adicionarLinhaTotal(sheet, headerStyle, rowIdx, "Total Aprovado", (int) totalAprovados);
        rowIdx = adicionarLinhaTotal(sheet, headerStyle, rowIdx, "Total Reprovado", (int) totalReprovados);

        rowIdx++; // linha em branco antes da tabela

        String[] colunas = {"Gestor", "Status", "Motivo", "Data/Hora"};

        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < colunas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(colunas[i]);
            cell.setCellStyle(headerStyle);
        }

        for (LoteAprovacao decisao : historico) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(nullSafe(decisao.getGestor()));
            row.createCell(1).setCellValue(decisao.getStatusAprovacao() != null ? decisao.getStatusAprovacao().name() : "-");
            row.createCell(2).setCellValue(nullSafe(decisao.getMotivo()));
            row.createCell(3).setCellValue(decisao.getDataAprovacao() != null ? decisao.getDataAprovacao().format(DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss")) : "-");
        }

        for (int i = 0; i < colunas.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String nullSafe(String valor) {
        return valor != null ? valor : "-";
    }


    // Próximos métodos (ainda não implementados nesta etapa — PoC primeiro):
    // adicionarAbaProduto(List<ProdutoResponseDTO> produtos)
    // adicionarAbaLoteRecebido(List<LoteRecebidoResponseDTO> itens)
    // adicionarAbaContraprova(ContraprovaResumoDTO resumo)
    // adicionarAbaAprovacao(List<LoteAprovacaoResponseDTO> historico)
}
