package br.com.apigestaodeestoque.repository;

import br.com.apigestaodeestoque.entity.Pedido;
import br.com.apigestaodeestoque.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Lista os pedidos de um usuário específico (histórico de compras do cliente)
    Page<Pedido> findByUsuarioId(Long usuarioId, Pageable pageable);

    // Lista pedidos por status (útil para o painel do estoquista/admin)
    List<Pedido> findByStatus(StatusPedido status);
}