package br.com.apigestaodeestoque.controller;

import br.com.apigestaodeestoque.dtos.ProdutoRequestDTO;
import br.com.apigestaodeestoque.dtos.ProdutoResponseDTO;
import br.com.apigestaodeestoque.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos") // Boas práticas REST: utilizar o substantivo no plural
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService; // Adicionado 'final' para o @RequiredArgsConstructor injetar

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(@RequestBody @Valid ProdutoRequestDTO dto) {
        ProdutoResponseDTO response = produtoService.criaProduto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listarTodos() {
        List<ProdutoResponseDTO> lista = produtoService.listaProduto();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        ProdutoResponseDTO response = produtoService.buscaPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid ProdutoRequestDTO dto) {
        ProdutoResponseDTO response = produtoService.atualizaProduto(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        produtoService.deletaProduto(id);
        return ResponseEntity.noContent().build(); // Retorna HTTP 204 No Content
    }
}