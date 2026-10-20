package br.com.apigestaodeestoque.repository;

import br.com.apigestaodeestoque.entity.PedidoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoHistoricoRepository extends JpaRepository<PedidoHistorico, Long> {

    // Procura todos os registos do histórico de um pedido ordenados por data (mais recente primeiro)
    List<PedidoHistorico> findByPedidoIdOrderByDataAlteracaoDesc(Long pedidoId);
}