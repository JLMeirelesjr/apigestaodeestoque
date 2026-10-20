package br.com.apigestaodeestoque.repository;

import br.com.apigestaodeestoque.entity.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    // Busca a posição de estoque pelo ID do Produto
    Optional<Estoque> findByProdutoId(Long produtoId);

    // Consulta customizada JPQL para listar itens que precisam de reposição
    @Query("SELECT e FROM Estoque e WHERE e.quantidadeDisponivel <= e.quantidadeMinima")
    List<Estoque> findItensComEstoqueBaixo();
}