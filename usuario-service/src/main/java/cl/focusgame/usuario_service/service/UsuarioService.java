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
        return repo.findAll().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario con id " + id + " no existe"));
        return aResponse(u);
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest req) {
        Usuario existente = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario con id " + id + " no existe"));
        existente.setEmail(req.email());
        existente.setUsername(req.username());
        Usuario guardado = repo.save(existente);
        log.info("Usuario actualizado id={}", guardado.getId());
        return aResponse(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario con id " + id + " no existe");
        }
        repo.deleteById(id);
        log.info("Usuario eliminado id={}", id);
    }

    private UsuarioResponse aResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getEmail(), u.getUsername());
    }
}
