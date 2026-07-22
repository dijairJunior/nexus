package br.com.waps.nexus.domain.importacao;

import br.com.waps.nexus.domain.importacao.dto.ConfirmarArmRequest;
import br.com.waps.nexus.domain.importacao.dto.ItemConfirmadoDTO;
import br.com.waps.nexus.domain.importacao.dto.ItemPreviewDTO;
import br.com.waps.nexus.domain.importacao.dto.PreviewArmResponse;
import br.com.waps.nexus.domain.lote.historico.LoteHistoricoService;
import br.com.waps.nexus.domain.lote.historico.TipoEventoHistorico;
import br.com.waps.nexus.domain.lote.triagem.LoteTriagem;
import br.com.waps.nexus.domain.lote.triagem.LoteTriagemRepository;
import br.com.waps.nexus.domain.lote.triagem.StatusLote;
import br.com.waps.nexus.domain.produto.Produto;
import br.com.waps.nexus.domain.produto.ProdutoRepository;
import br.com.waps.nexus.exception.BusinessException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class ImportacaoService {

    private final ProdutoRepository produtoRepository;
    private final LoteTriagemRepository loteTriagemRepository;
    private final LoteHistoricoService loteHistoricoService;

    public ImportacaoService(LoteTriagemRepository loteTriagemRepository,
                             ProdutoRepository produtoRepository,
                             LoteHistoricoService loteHistoricoService) {
        this.loteTriagemRepository = loteTriagemRepository;
        this.produtoRepository = produtoRepository;
        this.loteHistoricoService = loteHistoricoService;
    }

    public PreviewArmResponse gerarPreview(MultipartFile arquivo, String protocolo) throws IOException {

        try (InputStream is = arquivo.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet abaArm = workbook.getSheet("ARM");
            Sheet abaRomaneio = workbook.getSheet("ROMANEIO");

            if (abaArm ==null || abaRomaneio == null) {
                throw new RuntimeException("Arquivo inválido: abas ARM ou ROMANEIO não encontrado");
            }

            PreviewArmResponse response = new PreviewArmResponse();
            response.setProtocolo(protocolo);
            response.setRazaoSocialCliente(lerTexto(abaArm, "F12"));
            response.setCnpjCliente(lerTexto(abaArm, "F14"));
            response.setEnderecoCliente(lerTexto(abaArm, "F18") + " - " + lerTexto(abaArm, "F20"));
            response.setContatoCliente(lerTexto(abaArm, "F22") + " | " + lerTexto(abaArm, "F24"));

            List<ItemPreviewDTO> itens = lerItensRomaneio(abaRomaneio);
            response.setItens(itens);
            response.setQuantidadeEsperada(itens.size());

            long comErro = itens.stream().filter(i -> i.getErro() != null).count();
            response.setQuantidadeComErro((int) comErro);

            return response;
        }
    }

    private List<ItemPreviewDTO> lerItensRomaneio(Sheet aba) {
        List<ItemPreviewDTO> itens = new ArrayList<>();

        Row cabecalho = aba.getRow(0);
        Map<String, Integer> colunas = mapearColunas(cabecalho);

        Integer colImei = obterPrimeiraExistente(colunas,
                "IMEI", "NUMERO DE SERIE", "NÚMERO DE SÉRIE");

        Integer colModelo = obterPrimeiraExistente(colunas,
                "MARCA/MODELO", "MARCA / MODELO", "MATERIAL", "MODELO");

        Integer colNf = obterPrimeiraExistente(colunas,
                "NF", "NOTA FISCAL", "NF DEVOLUCAO", "NF DEVOLUÇÃO");

        Integer colDataNf = obterPrimeiraExistente(colunas,
                "EMISSAO NF", "EMISSÃO NF", "DATA NF", "DATA DA NF");

        Integer colCd = obterPrimeiraExistente(colunas,
                "CD", "CENTRO DE DISTRIBUICAO", "CENTRO DE DISTRIBUIÇÃO");

        validarColunasObrigatorias(colImei, colModelo, colNf);

        if (colImei == null) {
            throw new RuntimeException("Coluna IMEI não encontrada na aba Romaneio");
        }

        for (int i = 1; i <= aba.getLastRowNum(); i++) {
            Row row = aba.getRow(i);
            if (row == null || isLinhaVazia(row)) continue;

            ItemPreviewDTO item = new ItemPreviewDTO();
            item.setLinha(i + 1);
            item.setModelo(colModelo != null ? lerTexto(row.getCell(colModelo)) : null);
            item.setNfDevolucao(colNf != null ? lerTexto(row.getCell(colNf)) : null);
            item.setDataNfDevolucao(colDataNf != null ? lerData(row.getCell(colDataNf)) : null);
            item.setCentroDistribuicao(colCd != null ? lerTexto(row.getCell(colCd)) : null);

            String imei = lerTexto(row.getCell(colImei));
            item.setNumeroSerie(imei);
            item.setErro(validarImei(imei));

            itens.add(item);
        }
        return itens;
    }

    private String validarImei(String imei) {
        if (imei == null || imei.isBlank()) {
            return "IMEI não informado";
        }
        if (imei.length() != 15) {
            return "IMEI deve ter 15 dígitos (encontrado: " + imei.length() + ")";
        }
        return null;
    }

    private boolean isLinhaVazia(Row row) {
        Cell primeiraCell = row.getCell(0);
        return primeiraCell == null || primeiraCell.getCellType() == CellType.BLANK;
    }

    private String lerTexto(Sheet aba, String endereco) {
        CellReference ref = new CellReference(endereco);
        Row row = aba.getRow(ref.getRow());
        if (row ==null) return null;
        return lerTexto(row.getCell(ref.getCol()));
    }

    private String lerTexto(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private LocalDate lerData(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.NUMERIC) return null;
        if (!DateUtil.isCellDateFormatted(cell)) return null;
        return cell.getDateCellValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public LoteTriagem confirmarImportacao (ConfirmarArmRequest request) {

        if (loteTriagemRepository.existsByProtocolo(request.getProtocolo())) {
            throw new RuntimeException("Já existe um lote com o protocolo: " + request.getProtocolo());
        }

        LoteTriagem lote = new LoteTriagem();
        lote.setProtocolo(request.getProtocolo());
        lote.setCnpjCliente(request.getCnpjCliente());
        lote.setRazaoSocialCliente(request.getRazaoSocialCliente());
        lote.setEnderecoCliente(request.getEnderecoCliente());
        lote.setContatoCliente(request.getContatoCliente());
        lote.setQuantidadeEsperada(request.getItens().size());
        lote.setNumero(proximoNumeroSequencial());
        lote.setDataLote(LocalDate.now());
        lote.setStatus(StatusLote.RECEBIDO);

        LoteTriagem loteSalvo = loteTriagemRepository.save(lote);

        loteHistoricoService.registrar(
                loteSalvo.getId(),
                TipoEventoHistorico.LOTE_CRIADO,
                "Lote criado via importação ARM - protocolo " + loteSalvo.getProtocolo()
        );

        List<Produto> produtos = new ArrayList<>();
        for (ItemConfirmadoDTO item : request.getItens()) {
            Produto produto = new Produto();
            produto.setNumeroSerie(item.getNumeroSerie());
            produto.setModelo(item.getModelo());
            produto.setNfDevolucao(item.getNfDevolucao());
            produto.setDataNfDevolucao(item.getDataNfDevolucao());
            produto.setCentroDistribuicao(item.getCentroDistribuicao());
            produto.setStatusCadastro("Pendente");
            produto.setLoteTriagemId(loteSalvo.getId());
            produtos.add(produto);
        }

        produtoRepository.saveAll(produtos);

        return loteSalvo;
    }

    private Integer proximoNumeroSequencial() {
        return loteTriagemRepository.findAll().stream()
                .map(LoteTriagem::getNumero)
                .filter(n -> n != null)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    private Map<String, Integer> mapearColunas(Row cabecalho) {
        Map<String, Integer> mapa = new HashMap<>();
        if (cabecalho == null) return mapa;

        for (int c = 0; c < cabecalho.getLastCellNum(); c++) {
            String nome = normalizarCabecalho(lerTexto(cabecalho.getCell(c)));
            if (nome != null && !nome.isBlank()) {
                mapa.put(nome, c);
            }
        }
        return mapa;
    }

    private Integer obterPrimeiraExistente(Map<String, Integer> colunas, String... nomes) {
        for (String nome : nomes) {
            Integer idx = colunas.get(normalizarCabecalho(nome));
            if (idx != null) return idx;
        }
        return null;
    }

    private String normalizarCabecalho(String valor) {
        if (valor == null) return null;
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private void validarColunasObrigatorias(Integer colImei, Integer colModelo, Integer colNf) {
        List<String> ausentes = new ArrayList<>();

        if (colImei == null) ausentes.add("IMEI");
        if (colModelo == null) ausentes.add("MATERIAL ou MARCA/MODELO");
        if (colNf == null) ausentes.add("NF");

        if (!ausentes.isEmpty()) {
            throw new BusinessException(
                    "Colunas obrigatórias não encontradas na aba Romaneio: " + String.join(", ", ausentes)
            );
        }
    }

}
