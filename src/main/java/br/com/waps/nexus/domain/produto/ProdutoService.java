package br.com.waps.nexus.domain.produto;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
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
            String usuarioLogado = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();
            produto.setTriador(usuarioLogado);
        }
        return produtoRepository.save(produto);
    }

    public void deletar(Long id) {
        produtoRepository.deleteById(id);
    }
}
