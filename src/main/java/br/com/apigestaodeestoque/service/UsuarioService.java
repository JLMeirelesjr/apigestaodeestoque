package br.com.apigestaodeestoque.service;

import br.com.apigestaodeestoque.dtos.UsuarioRequestDTO;
import br.com.apigestaodeestoque.dtos.UsuarioResponseDTO;
import br.com.apigestaodeestoque.entity.Usuario;
import br.com.apigestaodeestoque.exception.RegraNegocioException;
import br.com.apigestaodeestoque.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public UsuarioResponseDTO criaUsuario(@Valid UsuarioRequestDTO requestDTO) {
        // Regra de negócio: E-mail não pode ser duplicado
        if (usuarioRepository.existsByEmail(requestDTO.email())) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com o e-mail: " + requestDTO.email());
        }

        Usuario entity = toEntity(requestDTO);
        Usuario usuarioSalvo = usuarioRepository.save(entity);
        return toResponseDTO(usuarioSalvo);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listaUsuario() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscaPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado pelo ID: " + id));
        return toResponseDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuario(Long id, UsuarioRequestDTO dadosAtualizados) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado pelo ID: " + id));

        // Regra de negócio: Se alterou o e-mail, verificar se o novo e-mail já pertence a outro usuário
        if (!usuario.getEmail().equals(dadosAtualizados.email()) && usuarioRepository.existsByEmail(dadosAtualizados.email())) {
            throw new RegraNegocioException("O e-mail " + dadosAtualizados.email() + " já está em uso por outro usuário.");
        }

        usuario.setNome(dadosAtualizados.nome());
        usuario.setEmail(dadosAtualizados.email());
        usuario.setRole(dadosAtualizados.role());
        usuario.setSenha(dadosAtualizados.senha()); // Futuramente aplicaremos o BCryptPasswordEncoder aqui

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
        return toResponseDTO(usuarioAtualizado);
    }

    @Transactional
    public void deletaUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RegraNegocioException("Usuário não encontrado pelo ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // --- Métodos Privados de Conversão (Mappers) ---

    private Usuario toEntity(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());
        usuario.setRole(dto.role());
        return usuario;
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(usuario);
    }
}