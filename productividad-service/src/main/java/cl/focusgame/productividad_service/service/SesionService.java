package cl.focusgame.productividad_service.service;

import cl.focusgame.productividad_service.client.UsuarioClient;
import cl.focusgame.productividad_service.dto.CrearSesionRequest;
import cl.focusgame.productividad_service.exception.RecursoNoEncontradoException;
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
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Sesion> listarPorUsuario(Long usuarioId) {
        return repo.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Sesion buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesion con id " + id + " no existe"));
    }

    @Transactional
    public Sesion iniciar(CrearSesionRequest req) {
        validarUsuarioExiste(req.usuarioId());
        if (!tareaRepository.existsById(req.tareaId())) {
            throw new RecursoNoEncontradoException("La tarea " + req.tareaId() + " no existe");
        }
        Sesion sesion = new Sesion();
        sesion.setUsuarioId(req.usuarioId());
        sesion.setTareaId(req.tareaId());
        sesion.setDuracionMinutos(req.duracionMinutos());
        sesion.setEstado("EN_CURSO");
        sesion.setFechaInicio(LocalDateTime.now());
        Sesion guardada = repo.save(sesion);
        log.info("Sesion iniciada id={} usuarioId={} tareaId={}",
                guardada.getId(), guardada.getUsuarioId(), guardada.getTareaId());
        return guardada;
    }

    @Transactional
    public Sesion completar(Long id) {
        Sesion sesion = buscarPorId(id);
        sesion.setEstado("COMPLETADA");
        sesion.setFechaFin(LocalDateTime.now());
        log.info("Sesion completada id={}", id);
        return repo.save(sesion);
    }

    @Transactional
    public Sesion cancelar(Long id) {
        Sesion sesion = buscarPorId(id);
        sesion.setEstado("CANCELADA");
        sesion.setFechaFin(LocalDateTime.now());
        log.info("Sesion cancelada id={}", id);
        return repo.save(sesion);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new RecursoNoEncontradoException("Sesion con id " + id + " no existe");
        }
        repo.deleteById(id);
        log.info("Sesion eliminada id={}", id);
    }

    private void validarUsuarioExiste(Long usuarioId) {
        try {
            usuarioClient.obtenerPorId(usuarioId);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El usuario " + usuarioId + " no existe");
        }
    }
}
