package br.com.apigestaodeestoque.service;

import br.com.apigestaodeestoque.dtos.ItemPedidoRequestDTO;
import br.com.apigestaodeestoque.dtos.PedidoRequestDTO;
import br.com.apigestaodeestoque.dtos.PedidoResponseDTO;
import br.com.apigestaodeestoque.entity.*;
import br.com.apigestaodeestoque.enums.StatusPedido;
import br.com.apigestaodeestoque.exception.RegraNegocioException;
import br.com.apigestaodeestoque.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private PedidoHistoricoRepository pedidoHistoricoRepository;

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private EstoqueService estoqueService;

    @InjectMocks
    private PedidoService pedidoService;

    private Usuario usuario;
    private Produto produto;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("José Lúcio");

        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Teclado Mecânico");
        produto.setPreco(new BigDecimal("150.00"));
        produto.setAtivo(true);

        pedido = new Pedido();
        pedido.setId(10L);
        pedido.setUsuario(usuario);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setValorTotal(new BigDecimal("150.00"));
        pedido.setItens(new ArrayList<>());
    }

    @Nested
    @DisplayName("Testes do Método criarPedido")
    class CriarPedidoTests {

        @Test
        @DisplayName("Deve criar pedido com sucesso quando os dados forem válidos")
        void deveCriarPedidoComSucesso() {
            // Arrange
            ItemPedidoRequestDTO itemDTO = new ItemPedidoRequestDTO(1L, 1);
            PedidoRequestDTO requestDTO = new PedidoRequestDTO(1L, List.of(itemDTO));

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
            when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
                Pedido p = invocation.getArgument(0);
                p.setId(10L);
                return p;
            });

            // Act
            PedidoResponseDTO resposta = pedidoService.criarPedido(requestDTO);

            // Assert
            assertNotNull(resposta);
            assertEquals(10L, resposta.id());
            assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, resposta.status());

            verify(estoqueService, times(1)).debitarEstoque(1L, 1);
            verify(pedidoHistoricoRepository, times(1)).save(any(PedidoHistorico.class));
            verify(pedidoRepository, times(1)).save(any(Pedido.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando o usuário não for encontrado")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
            // Arrange
            PedidoRequestDTO requestDTO = new PedidoRequestDTO(99L, List.of());
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            RegraNegocioException exception = assertThrows(
                    RegraNegocioException.class,
                    () -> pedidoService.criarPedido(requestDTO)
            );

            assertTrue(exception.getMessage().contains("Usuário não encontrado"));
            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes do Método cancelarPedido")
    class CancelarPedidoTests {

        @Test
        @DisplayName("Deve cancelar pedido e recompor o estoque com sucesso")
        void deveCancelarPedidoComSucesso() {
            // Arrange
            Estoque estoque = new Estoque();
            estoque.setProduto(produto);
            estoque.setQuantidadeDisponivel(5);

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(2);
            pedido.getItens().add(item);

            when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));
            when(estoqueRepository.findByProdutoId(1L)).thenReturn(Optional.of(estoque));
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

            // Act
            PedidoResponseDTO resposta = pedidoService.cancelarPedido(10L);

            // Assert
            assertNotNull(resposta);
            assertEquals(StatusPedido.CANCELADO, resposta.status());
            assertEquals(7, estoque.getQuantidadeDisponivel()); // 5 + 2 devolvidos

            verify(estoqueRepository, times(1)).save(estoque);
            verify(pedidoHistoricoRepository, times(1)).save(any(PedidoHistorico.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar cancelar um pedido já cancelado")
        void deveLancarExcecaoAoCancelarPedidoJaCancelado() {
            // Arrange
            pedido.setStatus(StatusPedido.CANCELADO);
            when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> pedidoService.cancelarPedido(10L)
            );

            assertEquals("O pedido já se encontra cancelado.", exception.getMessage());
            verify(estoqueRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes do Fluxo de Pagamento e Envio")
    class TransicaoStatusTests {

        @Test
        @DisplayName("Deve alterar o status para PAGO com sucesso")
        void devePagarPedidoComSucesso() {
            // Arrange
            when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

            // Act
            PedidoResponseDTO resposta = pedidoService.pagarPedido(10L);

            // Assert
            assertEquals(StatusPedido.PAGO, resposta.status());
            verify(pedidoHistoricoRepository, times(1)).save(any(PedidoHistorico.class));
        }

        @Test
        @DisplayName("Deve alterar o status para ENVIADO com sucesso")
        void deveEnviarPedidoComSucesso() {
            // Arrange
            pedido.setStatus(StatusPedido.PAGO);
            when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

            // Act
            PedidoResponseDTO resposta = pedidoService.enviarPedido(10L);

            // Assert
            assertEquals(StatusPedido.ENVIADO, resposta.status());
            verify(pedidoHistoricoRepository, times(1)).save(any(PedidoHistorico.class));
        }
    }
}