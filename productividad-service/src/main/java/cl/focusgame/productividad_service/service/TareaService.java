package cl.focusgame.productividad_service.service;

import cl.focusgame.productividad_service.client.UsuarioClient;
import cl.focusgame.productividad_service.dto.CrearTareaRequest;
import cl.focusgame.productividad_service.exception.RecursoNoEncontradoException;
import cl.focusgame.productividad_service.exception.ServicioRemotoException;
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
        log.info("Listando todas las tareas");
        List<Tarea> tareas = repo.findAll();
        log.debug("Tareas encontradas cantidad={}", tareas.size());
        return tareas;
    }

    @Transactional(readOnly = true)
    public List<Tarea> listarPorUsuario(Long usuarioId) {
        log.info("Listando tareas usuarioId={}", usuarioId);
        List<Tarea> tareas = repo.findByUsuarioId(usuarioId);
        log.debug("Tareas encontradas usuarioId={} cantidad={}", usuarioId, tareas.size());
        return tareas;
    }

    @Transactional(readOnly = true)
    public Tarea buscarPorId(Long id) {
        log.info("Buscando tarea id={}", id);
        return repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tarea no encontrada id={}", id);
                    return new RecursoNoEncontradoException("Tarea con id " + id + " no existe");
                });
    }

    @Transactional
    public Tarea crear(CrearTareaRequest req) {
        log.info("Creando tarea titulo={} usuarioId={}", req.titulo(), req.usuarioId());
        validarUsuarioExiste(req.usuarioId());

        Tarea tarea = new Tarea();
        tarea.setTitulo(req.titulo());
        tarea.setDescripcion(req.descripcion());
        tarea.setEstado("PENDIENTE");
        tarea.setUsuarioId(req.usuarioId());
        Tarea guardada = repo.save(tarea);
        log.debug("Tarea creada exitosamente id={} usuarioId={}", guardada.getId(), guardada.getUsuarioId());
        return guardada;
    }

    @Transactional
    public Tarea actualizarEstado(Long id, String estado) {
        log.info("Actualizando estado tarea id={} estado={}", id, estado);
        Tarea tarea = buscarPorId(id);
        tarea.setEstado(estado);
        Tarea actualizada = repo.save(tarea);
        log.debug("Tarea id={} actualizada a estado={}", id, estado);
        return actualizada;
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando tarea id={}", id);
        if (!repo.existsById(id)) {
            log.warn("Eliminacion rechazada: tarea no existe id={}", id);
            throw new RecursoNoEncontradoException("Tarea con id " + id + " no existe");
        }
        repo.deleteById(id);
        log.debug("Tarea eliminada exitosamente id={}", id);
    }

    // Integridad referencial entre servicios: se valida en la capa de aplicacion via Feign.
    private void validarUsuarioExiste(Long usuarioId) {
        try {
            usuarioClient.obtenerPorId(usuarioId);
        } catch (FeignException.NotFound e) {
            log.warn("Usuario no encontrado en usuario-service usuarioId={}", usuarioId);
            throw new RecursoNoEncontradoException("El usuario " + usuarioId + " no existe");
        } catch (FeignException e) {
            log.error("Error de comunicacion con usuario-service usuarioId={} status={}", usuarioId, e.status(), e);
            throw new ServicioRemotoException("No se pudo validar el usuario " + usuarioId + " en usuario-service", e);
        }
    }
}
