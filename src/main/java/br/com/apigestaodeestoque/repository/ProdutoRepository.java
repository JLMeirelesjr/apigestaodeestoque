package br.com.apigestaodeestoque.repository;

import br.com.apigestaodeestoque.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Retorna apenas produtos ativos paginados (ideal para o catálogo)
    Page<Produto> findByAtivoTrue(Pageable pageable);

    // Busca produtos ativos por nome (case insensitive)
    Page<Produto> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome, Pageable pageable);
}