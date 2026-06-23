package br.com.waps.nexus.domain.produto;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final DefeitoConstatadoRepository defeitoConstatadoRepository;

    public ProdutoService(ProdutoRepository produtoRepository, DefeitoConstatadoRepository defeitoConstatadoRepository) {
        this.produtoRepository = produtoRepository;
        this.defeitoConstatadoRepository = defeitoConstatadoRepository;
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    /*public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }*/

    public Produto salvar(Produto produto) {
        if (produto.getId() == null) {

            if (produto.getNumeroSerie() == null || produto.getNumeroSerie().isBlank()) {
                throw new RuntimeException("IMEI é obrigatório");
            }
            if (produto.getNumeroSerie().length() != 15) {
                throw new RuntimeException("IMEI deve ter exatamente 15 dígitos");
            }
            if (produtoRepository.existsByNumeroSerie(produto.getNumeroSerie())) {
                throw new RuntimeException("Já existe um produto cadastrado com este IMEI: " + produto.getNumeroSerie());
            }

            String usuarioLogado = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();
            produto.setTriador(usuarioLogado);
        }

        // Classificação automática sempre que salvar (criação ou atualização
        produto.setClassificacao(calcularClassificacao(produto));

        return produtoRepository.save(produto);
    }

    public void deletar(Long id) {
        produtoRepository.deleteById(id);
    }

    public String calcularClassificacao(Produto produto) {

        String statusItem = produto.getStatusItem();

        if ("NOVO".equalsIgnoreCase(statusItem)) {
            return "A";
        }
        if ("OBSOLETO".equalsIgnoreCase(statusItem)) {
            return "D";
        }

        //TRIADO ou não informado -> avalia defeito e estetica
        if (produto.getDefeitoConstatadoId() != null) {
            DefeitoConstatado defeito = defeitoConstatadoRepository.findById(produto.getDefeitoConstatadoId())
                    .orElse(null);

            if (defeito != null && !"SEM DEFEITO".equalsIgnoreCase(defeito.getDescricao())) {
                return "D";
            }
        }

        String estetica = produto.getEstetica();

        if ("BOM".equalsIgnoreCase(estetica)) {
            return "A";
        }
        if ("RISCOS LEVES".equalsIgnoreCase(estetica)) {
            return "B";
        }
        if ("RISCOS PROFUNTOS".equalsIgnoreCase(estetica)) {
            return "C";
        }

        return "D";
    }
}
