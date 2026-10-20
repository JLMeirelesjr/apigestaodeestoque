package br.com.apigestaodeestoque.dtos;


import br.com.apigestaodeestoque.entity.Usuario;
import br.com.apigestaodeestoque.enums.Role;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        Role role,
        LocalDateTime dataCriacao
) {
    // Construtor auxiliar para converter a Entidade para DTO de forma simples
    public UsuarioResponseDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getDataCriacao()
        );
    }
}
