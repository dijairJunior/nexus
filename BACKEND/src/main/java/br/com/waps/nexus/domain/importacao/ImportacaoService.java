package br.com.waps.nexus.domain.importacao;

import br.com.waps.nexus.domain.importacao.dto.ConfirmarArmRequest;
import br.com.waps.nexus.domain.importacao.dto.ItemConfirmadoDTO;
import br.com.waps.nexus.domain.importacao.dto.ItemPreviewDTO;
import br.com.waps.nexus.domain.importacao.dto.PreviewArmResponse;
import br.com.waps.nexus.domain.lote.triagem.LoteTriagem;
import br.com.waps.nexus.domain.lote.triagem.LoteTriagemRepository;
import br.com.waps.nexus.domain.produto.Produto;
import br.com.waps.nexus.domain.produto.ProdutoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ImportacaoService {

    private final ProdutoRepository produtoRepository;
    private final LoteTriagemRepository loteTriagemRepository;

    public ImportacaoService(LoteTriagemRepository loteTriagemRepository,
                             ProdutoRepository produtoRepository) {
        this.loteTriagemRepository = loteTriagemRepository;
        this.produtoRepository = produtoRepository;
    }

    public PreviewArmResponse gerarPreview(MultipartFile arquivo, String protocolo) throws IOException {

        try (InputStream is = arquivo.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

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

        for (int i =1; i <= aba.getLastRowNum(); i++) {
            Row row = aba.getRow(i);
            if (row == null || isLinhaVazia(row)) continue;

            ItemPreviewDTO item = new ItemPreviewDTO();
            item.setLinha(i + 1);
            item.setModelo(lerTexto(row.getCell(3)));
            item.setNfDevolucao(lerTexto(row.getCell(4)));
            item.setDataNfDevolucao(lerData(row.getCell(5)));
            item.setCentroDistribuicao(lerTexto(row.getCell(6)));

            String imei = lerTexto(row.getCell(2));
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

        LoteTriagem loteSalvo = loteTriagemRepository.save(lote);

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

}
