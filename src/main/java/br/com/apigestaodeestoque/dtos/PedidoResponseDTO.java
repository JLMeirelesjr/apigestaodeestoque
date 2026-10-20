package br.com.apigestaodeestoque.dtos;

import br.com.apigestaodeestoque.entity.Pedido;
import br.com.apigestaodeestoque.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        Long usuarioId,
        String nomeUsuario,
        BigDecimal valorTotal,
        StatusPedido status,
        LocalDateTime dataCriacao,
        List<ItemPedidoResponseDTO> itens
) {
    public PedidoResponseDTO(Pedido pedido) {
        this(
                pedido.getId(),
                pedido.getUsuario().getId(),
                pedido.getUsuario().getNome(),
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getDataCriacao(),
                pedido.getItens() != null ?
                        pedido.getItens().stream().map(ItemPedidoResponseDTO::new).toList() : List.of()
        );
    }
}