package br.com.apigestaodeestoque.service;

import br.com.apigestaodeestoque.dtos.ProdutoRequestDTO;
import br.com.apigestaodeestoque.dtos.ProdutoResponseDTO;
import br.com.apigestaodeestoque.entity.Produto;
import br.com.apigestaodeestoque.exception.RegraNegocioException;
import br.com.apigestaodeestoque.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Transactional
    public ProdutoResponseDTO criaProduto(ProdutoRequestDTO requestDTO) {
        Produto produto = toEntity(requestDTO);
        produto.setAtivo(true); // Todo novo produto começa ativo
        Produto produtoSalvo = produtoRepository.save(produto);
        return toResponseDTO(produtoSalvo);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listaProduto() {
        return produtoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscaPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Produto não encontrado pelo ID: " + id));
        return toResponseDTO(produto);
    }

    @Transactional
    public ProdutoResponseDTO atualizaProduto(Long id, ProdutoRequestDTO dadosAtualizados) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Produto não encontrado pelo ID: " + id));

        produto.setNome(dadosAtualizados.nome());
        produto.setDescricao(dadosAtualizados.descricao());
        produto.setPreco(dadosAtualizados.preco());

        Produto produtoAtualizado = produtoRepository.save(produto);
        return toResponseDTO(produtoAtualizado);
    }

    @Transactional
    public void deletaProduto(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RegraNegocioException("Produto não encontrado pelo ID: " + id);
        }
        produtoRepository.deleteById(id);
    }

    // --- Métodos Privados de Conversão (Mappers) ---

    private Produto toEntity(ProdutoRequestDTO dto) {
        Produto produto = new Produto();
        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        return produto;
    }

    private ProdutoResponseDTO toResponseDTO(Produto produto) {
        return new ProdutoResponseDTO(produto);
    }
}