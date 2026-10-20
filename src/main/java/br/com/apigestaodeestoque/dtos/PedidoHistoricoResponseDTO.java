package br.com.apigestaodeestoque.dtos;

import br.com.apigestaodeestoque.entity.PedidoHistorico;
import br.com.apigestaodeestoque.enums.StatusPedido;

import java.time.LocalDateTime;

public record PedidoHistoricoResponseDTO(
        Long id,
        Long pedidoId,
        Long usuarioId,
        String usuarioNome,
        StatusPedido statusAnterior,
        StatusPedido statusNovo,
        StatusPedido status,
        String observacao,
        LocalDateTime dataAlteracao
) {
    public PedidoHistoricoResponseDTO(PedidoHistorico historico) {
        this(
                historico.getId(),
                historico.getPedido().getId(),
                historico.getUsuario() != null ? historico.getUsuario().getId() : null,
                historico.getUsuario() != null ? historico.getUsuario().getNome() : null,
                historico.getStatusAnterior(),
                historico.getStatusNovo(),
                historico.getStatus(),
                historico.getObservacao(),
                historico.getDataAlteracao()
        );
    }
}