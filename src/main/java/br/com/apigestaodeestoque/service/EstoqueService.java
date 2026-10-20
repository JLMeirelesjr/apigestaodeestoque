package br.com.apigestaodeestoque.service;

import br.com.apigestaodeestoque.dtos.EstoqueEntradaDTO;
import br.com.apigestaodeestoque.entity.Estoque;
import br.com.apigestaodeestoque.entity.Produto;
import br.com.apigestaodeestoque.exception.RegraNegocioException;
import br.com.apigestaodeestoque.repository.EstoqueRepository;
import br.com.apigestaodeestoque.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional
    public void registrarOuAtualizarEstoque(EstoqueEntradaDTO dto) {
        Produto produto = produtoRepository.findById(dto.produtoId())
                .orElseThrow(() -> new RegraNegocioException("Produto não encontrado com o ID: " + dto.produtoId()));

        Estoque estoque = estoqueRepository.findByProdutoId(dto.produtoId())
                .orElse(new Estoque());

        estoque.setProduto(produto);
        estoque.setQuantidadeDisponivel(dto.quantidadeDisponivel());
        estoque.setQuantidadeMinima(dto.quantidadeMinima());

        estoqueRepository.save(estoque);
    }

    @Transactional
    public void debitarEstoque(Long produtoId, Integer quantidade) {
        Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new RegraNegocioException("Estoque não encontrado para o produto ID: " + produtoId));

        if (estoque.getQuantidadeDisponivel() < quantidade) {
            throw new RegraNegocioException("Estoque insuficiente para o produto ID: " + produtoId);
        }

        estoque.setQuantidadeDisponivel(estoque.getQuantidadeDisponivel() - quantidade);
        estoqueRepository.save(estoque);
    }

    @Transactional
    public void reporEstoque(Long produtoId, Integer quantidade) {
        Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new RegraNegocioException("Estoque não encontrado para o produto ID: " + produtoId));

        estoque.setQuantidadeDisponivel(estoque.getQuantidadeDisponivel() + quantidade);
        estoqueRepository.save(estoque);
    }
}