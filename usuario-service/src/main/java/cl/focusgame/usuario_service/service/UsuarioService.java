package cl.focusgame.usuario_service.service;

import cl.focusgame.usuario_service.dto.ActualizarUsuarioRequest;
import cl.focusgame.usuario_service.dto.UsuarioResponse;
import cl.focusgame.usuario_service.exception.RecursoNoEncontradoException;
import cl.focusgame.usuario_service.model.Usuario;
import cl.focusgame.usuario_service.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioRepository repo;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        log.info("Listando todos los usuarios");
        List<UsuarioResponse> usuarios = repo.findAll().stream()
                .map(this::aResponse)
                .toList();
        log.debug("Usuarios encontrados cantidad={}", usuarios.size());
        return usuarios;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        log.info("Buscando usuario id={}", id);
        Usuario u = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado id={}", id);
                    return new RecursoNoEncontradoException("Usuario con id " + id + " no existe");
                });
        log.debug("Usuario encontrado id={}", id);
        return aResponse(u);
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest req) {
        log.info("Actualizando usuario id={} email={} username={}", id, req.email(), req.username());
        Usuario existente = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Actualizacion rechazada: usuario no existe id={}", id);
                    return new RecursoNoEncontradoException("Usuario con id " + id + " no existe");
                });
        existente.setEmail(req.email());
        existente.setUsername(req.username());
        Usuario guardado = repo.save(existente);
        log.debug("Usuario actualizado exitosamente id={}", guardado.getId());
        return aResponse(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando usuario id={}", id);
        if (!repo.existsById(id)) {
            log.warn("Eliminacion rechazada: usuario no existe id={}", id);
            throw new RecursoNoEncontradoException("Usuario con id " + id + " no existe");
        }
        repo.deleteById(id);
        log.debug("Usuario eliminado exitosamente id={}", id);
    }

    private UsuarioResponse aResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getEmail(), u.getUsername());
    }
}
