package br.com.apigestaodeestoque.controller;

import br.com.apigestaodeestoque.dtos.PedidoHistoricoResponseDTO;
import br.com.apigestaodeestoque.dtos.PedidoRequestDTO;
import br.com.apigestaodeestoque.dtos.PedidoResponseDTO;
import br.com.apigestaodeestoque.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criar(@RequestBody @Valid PedidoRequestDTO dto) {
        PedidoResponseDTO response = pedidoService.criarPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // Retorna 201 Created
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        PedidoResponseDTO response = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable Long id) {
        PedidoResponseDTO dto = pedidoService.cancelarPedido(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<PedidoHistoricoResponseDTO>> buscarHistorico(@PathVariable Long id) {
        List<PedidoHistoricoResponseDTO> historico = pedidoService.buscarHistoricoPorPedidoId(id);
        return ResponseEntity.ok(historico);
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<PedidoResponseDTO> pagar(@PathVariable Long id) {
        PedidoResponseDTO dto = pedidoService.pagarPedido(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/enviar")
    public ResponseEntity<PedidoResponseDTO> enviar(@PathVariable Long id) {
        PedidoResponseDTO dto = pedidoService.enviarPedido(id);
        return ResponseEntity.ok(dto);
    }
}