package cl.triskeledu.usuarios.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import cl.triskeledu.usuarios.dto.UsuarioRequest;
import cl.triskeledu.usuarios.dto.UsuarioResponse;
import cl.triskeledu.common.exception.*;
import cl.triskeledu.usuarios.mapper.UsuarioMapper;
import cl.triskeledu.usuarios.model.Usuario;
import cl.triskeledu.usuarios.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Servicio encargado de aplicar las reglas de negocio de usuarios:
 * - Gestiona operaciones CRUD, validaciones de negocio y reglas de integridad.
 * - Valida que el email sea único (clave de negocio del dominio de usuarios).
 * - Lanza excepciones personalizadas del módulo common para casos de error específicos.
 * - Utiliza un mapper para convertir entre entidades y DTOs, manteniendo el código limpio y separado.
 * - La contraseña se hashea con BCrypt antes de guardarla (nunca se almacena en texto plano).
 * - La contraseña nunca se expone en la respuesta (se omite en UsuarioResponse).
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> findAll() {
        return usuarioMapper.toResponseList(usuarioRepository.findAll());
    }

    public UsuarioResponse findById(long id) {
        return usuarioMapper.toResponse(getUsuarioById(id));
    }

    public UsuarioResponse findByEmail(String email) {
        return usuarioMapper.toResponse(getUsuarioByEmail(email));
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        validateEmailUnico(request.getEmail());
        Usuario usuario = new Usuario();
        usuarioMapper.updateEntity(request, usuario);
        // Hashear la contraseña antes de guardarla
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setActivo(true);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse update(long id, UsuarioRequest request) {
        Usuario usuario = getUsuarioById(id);
        // Solo valida unicidad si cambió el email
        if (!usuario.getEmail().equalsIgnoreCase(request.getEmail())) {
            validateEmailUnico(request.getEmail());
        }
        usuarioMapper.updateEntity(request, usuario);
        // Hashear la contraseña antes de guardarla
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void deleteById(long id) {
        Usuario usuario = getUsuarioById(id);
        if (usuario != null) {
            usuarioRepository.delete(usuario);
        }
    }

    @Transactional
    public UsuarioResponse activar(long id) {
        Usuario usuario = getUsuarioById(id);
        usuario.setActivo(true);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse desactivar(long id) {
        Usuario usuario = getUsuarioById(id);
        usuario.setActivo(false);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    // ─── Métodos privados auxiliares ─────────────────────────────────────────

    private Usuario getUsuarioById(long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuarios", "ID", id));
    }

    private Usuario getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuarios", "email", email));
    }

    private void validateEmailUnico(String email) {
        usuarioRepository.findByEmail(email).ifPresent(u -> {
            throw new DuplicateResourceException(
                "Un Usuario", "email", email,
                u.getNombre() + " " + u.getApellido()
            );
        });
    }
}
