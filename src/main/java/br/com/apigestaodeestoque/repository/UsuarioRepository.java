package br.com.apigestaodeestoque.repository;

import br.com.apigestaodeestoque.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca o usuário pelo e-mail para autenticação e validações
    Optional<Usuario> findByEmail(String email);

    // Verifica se um e-mail já está cadastrado
    boolean existsByEmail(String email);
}