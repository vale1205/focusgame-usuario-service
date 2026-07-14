package cl.focusgame.productividad_service.service;

import cl.focusgame.productividad_service.client.UsuarioClient;
import cl.focusgame.productividad_service.dto.CrearSesionRequest;
import cl.focusgame.productividad_service.exception.RecursoNoEncontradoException;
import cl.focusgame.productividad_service.exception.ServicioRemotoException;
import cl.focusgame.productividad_service.model.Sesion;
import cl.focusgame.productividad_service.repository.SesionRepository;
import cl.focusgame.productividad_service.repository.TareaRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SesionService {

    private static final Logger log = LoggerFactory.getLogger(SesionService.class);

    private final SesionRepository repo;
    private final TareaRepository tareaRepository;
    private final UsuarioClient usuarioClient;

    public SesionService(SesionRepository repo, TareaRepository tareaRepository, UsuarioClient usuarioClient) {
        this.repo = repo;
        this.tareaRepository = tareaRepository;
        this.usuarioClient = usuarioClient;
    }

    @Transactional(readOnly = true)
    public List<Sesion> listarTodas() {
        log.info("Listando todas las sesiones");
        List<Sesion> sesiones = repo.findAll();
        log.debug("Sesiones encontradas cantidad={}", sesiones.size());
        return sesiones;
    }

    @Transactional(readOnly = true)
    public List<Sesion> listarPorUsuario(Long usuarioId) {
        log.info("Listando sesiones usuarioId={}", usuarioId);
        List<Sesion> sesiones = repo.findByUsuarioId(usuarioId);
        log.debug("Sesiones encontradas usuarioId={} cantidad={}", usuarioId, sesiones.size());
        return sesiones;
    }

    @Transactional(readOnly = true)
    public Sesion buscarPorId(Long id) {
        log.info("Buscando sesion id={}", id);
        return repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Sesion no encontrada id={}", id);
                    return new RecursoNoEncontradoException("Sesion con id " + id + " no existe");
                });
    }

    @Transactional
    public Sesion iniciar(CrearSesionRequest req) {
        log.info("Iniciando sesion usuarioId={} tareaId={}", req.usuarioId(), req.tareaId());
        validarUsuarioExiste(req.usuarioId());
        if (!tareaRepository.existsById(req.tareaId())) {
            log.warn("Inicio de sesion rechazado: tarea no existe tareaId={}", req.tareaId());
            throw new RecursoNoEncontradoException("La tarea " + req.tareaId() + " no existe");
        }
        Sesion sesion = new Sesion();
        sesion.setUsuarioId(req.usuarioId());
        sesion.setTareaId(req.tareaId());
        sesion.setDuracionMinutos(req.duracionMinutos());
        sesion.setEstado("EN_CURSO");
        sesion.setFechaInicio(LocalDateTime.now());
        Sesion guardada = repo.save(sesion);
        log.debug("Sesion iniciada exitosamente id={} usuarioId={} tareaId={}",
                guardada.getId(), guardada.getUsuarioId(), guardada.getTareaId());
        return guardada;
    }

    @Transactional
    public Sesion completar(Long id) {
        log.info("Completando sesion id={}", id);
        Sesion sesion = buscarPorId(id);
        sesion.setEstado("COMPLETADA");
        sesion.setFechaFin(LocalDateTime.now());
        Sesion completada = repo.save(sesion);
        log.debug("Sesion completada exitosamente id={}", id);
        return completada;
    }

    @Transactional
    public Sesion cancelar(Long id) {
        log.info("Cancelando sesion id={}", id);
        Sesion sesion = buscarPorId(id);
        sesion.setEstado("CANCELADA");
        sesion.setFechaFin(LocalDateTime.now());
        Sesion cancelada = repo.save(sesion);
        log.debug("Sesion cancelada exitosamente id={}", id);
        return cancelada;
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando sesion id={}", id);
        if (!repo.existsById(id)) {
            log.warn("Eliminacion rechazada: sesion no existe id={}", id);
            throw new RecursoNoEncontradoException("Sesion con id " + id + " no existe");
        }
        repo.deleteById(id);
        log.debug("Sesion eliminada exitosamente id={}", id);
    }

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
