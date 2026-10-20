package br.com.apigestaodeestoque.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EstoqueEntradaDTO(
        @NotNull(message = "O ID do produto é obrigatório")
        Long produtoId,

        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 0, message = "A quantidade não pode ser negativa")
        Integer quantidadeDisponivel,

        @NotNull(message = "A quantidade mínima é obrigatória")
        @Min(value = 0, message = "A quantidade mínima não pode ser negativa")
        Integer quantidadeMinima
) {}