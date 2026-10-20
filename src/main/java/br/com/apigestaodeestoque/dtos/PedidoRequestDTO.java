package br.com.apigestaodeestoque.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoRequestDTO(
        @NotNull(message = "O ID do usuário é obrigatório")
        Long usuarioId,

        @NotEmpty(message = "O pedido deve conter pelo menos um item")
        List<ItemPedidoRequestDTO> itens
) {}