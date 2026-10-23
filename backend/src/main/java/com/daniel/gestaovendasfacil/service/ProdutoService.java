package com.daniel.gestaovendasfacil.service;

import com.daniel.gestaovendasfacil.model.Produto;
import com.daniel.gestaovendasfacil.repository.ProdutoRepository;
import com.daniel.gestaovendasfacil.service.normalizer.ProductNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProductNormalizer productNormalizer;

    public ProdutoService(ProdutoRepository produtoRepository, ProductNormalizer productNormalizer) {
        this.produtoRepository = produtoRepository;
        this.productNormalizer = productNormalizer;
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAllByOrderByNomeAsc();
    }

    @Transactional
    public Produto salvarOuObter(String nomeRaw) {
        String nomeNormalizado = productNormalizer.normalize(nomeRaw);
        return produtoRepository.findByNomeIgnoreCase(nomeNormalizado)
                .orElseGet(() -> produtoRepository.save(new Produto(nomeNormalizado)));
    }
}

