package cl.focusgame.productividad_service.service;

import cl.focusgame.productividad_service.client.UsuarioClient;
import cl.focusgame.productividad_service.dto.CrearTareaRequest;
import cl.focusgame.productividad_service.exception.RecursoNoEncontradoException;
import cl.focusgame.productividad_service.model.Tarea;
import cl.focusgame.productividad_service.repository.TareaRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TareaService {

    private static final Logger log = LoggerFactory.getLogger(TareaService.class);

    private final TareaRepository repo;
    private final UsuarioClient usuarioClient;

    public TareaService(TareaRepository repo, UsuarioClient usuarioClient) {
        this.repo = repo;
        this.usuarioClient = usuarioClient;
    }

    @Transactional(readOnly = true)
    public List<Tarea> listarTodas() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Tarea> listarPorUsuario(Long usuarioId) {
        return repo.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Tarea buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tarea con id " + id + " no existe"));
    }

    @Transactional
    public Tarea crear(CrearTareaRequest req) {
        validarUsuarioExiste(req.usuarioId());

        Tarea tarea = new Tarea();
        tarea.setTitulo(req.titulo());
        tarea.setDescripcion(req.descripcion());
        tarea.setEstado("PENDIENTE");
        tarea.setUsuarioId(req.usuarioId());
        Tarea guardada = repo.save(tarea);
        log.info("Tarea creada id={} usuarioId={}", guardada.getId(), guardada.getUsuarioId());
        return guardada;
    }

    @Transactional
    public Tarea actualizarEstado(Long id, String estado) {
        Tarea tarea = buscarPorId(id);
        tarea.setEstado(estado);
        log.info("Tarea id={} cambia a estado={}", id, estado);
        return repo.save(tarea);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new RecursoNoEncontradoException("Tarea con id " + id + " no existe");
        }
        repo.deleteById(id);
        log.info("Tarea eliminada id={}", id);
    }

    // Integridad referencial entre servicios: se valida en la capa de aplicacion via Feign.
    private void validarUsuarioExiste(Long usuarioId) {
        try {
            usuarioClient.obtenerPorId(usuarioId);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El usuario " + usuarioId + " no existe");
        }
    }
}
