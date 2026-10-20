package br.com.apigestaodeestoque.service;

import br.com.apigestaodeestoque.dtos.ItemPedidoRequestDTO;
import br.com.apigestaodeestoque.dtos.PedidoHistoricoResponseDTO;
import br.com.apigestaodeestoque.dtos.PedidoRequestDTO;
import br.com.apigestaodeestoque.dtos.PedidoResponseDTO;
import br.com.apigestaodeestoque.entity.*;
import br.com.apigestaodeestoque.enums.StatusPedido;
import br.com.apigestaodeestoque.exception.RegraNegocioException;
import br.com.apigestaodeestoque.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoHistoricoRepository pedidoHistoricoRepository;
    private final EstoqueRepository estoqueRepository;
    private final EstoqueService estoqueService;

    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO requestDTO) {
        // 1. Validar e procurar o Utilizador
        Usuario usuario = usuarioRepository.findById(requestDTO.usuarioId())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado com o ID: " + requestDTO.usuarioId()));

        // 2. Instanciar o Pedido com o estado inicial e data
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setDataCriacao(LocalDateTime.now());

        BigDecimal valorTotalPedido = BigDecimal.ZERO;
        List<ItemPedido> itensPedido = new ArrayList<>();

        // 3. Processar cada item do pedido
        for (ItemPedidoRequestDTO itemDTO : requestDTO.itens()) {
            Produto produto = produtoRepository.findById(itemDTO.produtoId())
                    .orElseThrow(() -> new RegraNegocioException("Produto não encontrado com o ID: " + itemDTO.produtoId()));

            if (!Boolean.TRUE.equals(produto.getAtivo())) {
                throw new RegraNegocioException("O produto '" + produto.getNome() + "' está inativo e não pode ser comprado.");
            }

            // Abater a quantidade no estoque (se não houver saldo, lança exceção e faz rollback)
            estoqueService.debitarEstoque(produto.getId(), itemDTO.quantidade());

            // Calcular subtotal do item
            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(itemDTO.quantidade()));
            valorTotalPedido = valorTotalPedido.add(subtotal);

            // Montar o ItemPedido
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setPedido(pedido);
            itemPedido.setProduto(produto);
            itemPedido.setQuantidade(itemDTO.quantidade());
            itemPedido.setPrecoUnitario(produto.getPreco());
            itemPedido.setSubtotal(subtotal);

            itensPedido.add(itemPedido);
        }

        pedido.setValorTotal(valorTotalPedido);
        pedido.setItens(itensPedido);

        // 4. Guardar o Pedido no Banco
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // 5. Registar a auditoria no PedidoHistorico
        registrarHistorico(
                pedidoSalvo,
                usuario,
                StatusPedido.AGUARDANDO_PAGAMENTO,
                StatusPedido.AGUARDANDO_PAGAMENTO,
                "Pedido criado com sucesso."
        );

        return new PedidoResponseDTO(pedidoSalvo);
    }

    @Transactional
    public PedidoResponseDTO cancelarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com o ID: " + id));

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalStateException("O pedido já se encontra cancelado.");
        }

        if (pedido.getStatus() == StatusPedido.ENVIADO || pedido.getStatus() == StatusPedido.ENTREGUE) {
            throw new IllegalStateException("Não é possível cancelar um pedido que já foi enviado ou entregue.");
        }

        StatusPedido statusAnterior = pedido.getStatus();

        // 1. Devolver os itens ao estoque
        for (ItemPedido item : pedido.getItens()) {
            Estoque estoque = estoqueRepository.findByProdutoId(item.getProduto().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Estoque não encontrado para o produto: " + item.getProduto().getNome()));

            estoque.setQuantidadeDisponivel(estoque.getQuantidadeDisponivel() + item.getQuantidade());
            estoqueRepository.save(estoque);
        }

        // 2. Atualizar status do pedido
        pedido.setStatus(StatusPedido.CANCELADO);
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

        // 3. Registrar Histórico de Auditoria
        registrarHistorico(
                pedidoAtualizado,
                pedido.getUsuario(),
                statusAnterior,
                StatusPedido.CANCELADO,
                "Pedido cancelado com sucesso. Estoque recomposto."
        );

        return new PedidoResponseDTO(pedidoAtualizado);
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Pedido não encontrado com o ID: " + id));
        return new PedidoResponseDTO(pedido);
    }

    private void registrarHistorico(Pedido pedido, Usuario usuario, StatusPedido statusAnterior, StatusPedido statusNovo, String observacao) {
        PedidoHistorico historico = new PedidoHistorico();
        historico.setPedido(pedido);
        historico.setUsuario(usuario);
        historico.setStatus(statusNovo);
        historico.setStatusAnterior(statusAnterior);
        historico.setStatusNovo(statusNovo);
        historico.setObservacao(observacao);

        LocalDateTime agora = LocalDateTime.now();
        historico.setDataAlteracao(agora);
        historico.setDataAtualizacao(agora);

        pedidoHistoricoRepository.save(historico);
    }

    @Transactional(readOnly = true)
    public List<PedidoHistoricoResponseDTO> buscarHistoricoPorPedidoId(Long pedidoId) {
        // 1. Verificar se o pedido existe
        if (!pedidoRepository.existsById(pedidoId)) {
            throw new EntityNotFoundException("Pedido não encontrado com o ID: " + pedidoId);
        }

        // 2. Buscar a lista de histórico
        List<PedidoHistorico> historicoList = pedidoHistoricoRepository.findByPedidoIdOrderByDataAlteracaoDesc(pedidoId);

        // 3. Converter para DTO
        return historicoList.stream()
                .map(PedidoHistoricoResponseDTO::new)
                .toList();
    }

    @Transactional
    public PedidoResponseDTO pagarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com o ID: " + id));

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new IllegalStateException("Apenas pedidos no status AGUARDANDO_PAGAMENTO podem ser pagos. Status atual: " + pedido.getStatus());
        }

        StatusPedido statusAnterior = pedido.getStatus();
        pedido.setStatus(StatusPedido.PAGO);
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

        registrarHistorico(
                pedidoAtualizado,
                pedido.getUsuario(),
                statusAnterior,
                StatusPedido.PAGO,
                "Pagamento confirmado com sucesso."
        );

        return new PedidoResponseDTO(pedidoAtualizado);
    }

    @Transactional
    public PedidoResponseDTO enviarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com o ID: " + id));

        if (pedido.getStatus() != StatusPedido.PAGO) {
            throw new IllegalStateException("Apenas pedidos com status PAGO podem ser enviados. Status atual: " + pedido.getStatus());
        }

        StatusPedido statusAnterior = pedido.getStatus();
        pedido.setStatus(StatusPedido.ENVIADO);
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

        registrarHistorico(
                pedidoAtualizado,
                pedido.getUsuario(),
                statusAnterior,
                StatusPedido.ENVIADO,
                "Pedido despachado para envio."
        );

        return new PedidoResponseDTO(pedidoAtualizado);
    }
}