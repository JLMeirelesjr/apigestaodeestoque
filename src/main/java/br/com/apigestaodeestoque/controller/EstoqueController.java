package br.com.apigestaodeestoque.controller;

import br.com.apigestaodeestoque.dtos.EstoqueEntradaDTO;
import br.com.apigestaodeestoque.service.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;

    @PostMapping
    public ResponseEntity<Void> registrarOuAtualizar(@RequestBody @Valid EstoqueEntradaDTO dto) {
        estoqueService.registrarOuAtualizarEstoque(dto);
        return ResponseEntity.ok().build(); // Retorna 200 OK sem corpo
    }
}