package cl.focusgame.gamificacion_service.service;

import cl.focusgame.gamificacion_service.client.ProgresoClient;
import cl.focusgame.gamificacion_service.client.RecompensaClient;
import cl.focusgame.gamificacion_service.client.SesionClient;
import cl.focusgame.gamificacion_service.dto.CrearRecompensaRequest;
import cl.focusgame.gamificacion_service.dto.ProgresoDTO;
import cl.focusgame.gamificacion_service.dto.ResumenGamificacionResponse;
import cl.focusgame.gamificacion_service.dto.SesionDTO;
import cl.focusgame.gamificacion_service.exception.ConflictoException;
import cl.focusgame.gamificacion_service.exception.RecursoNoEncontradoException;
import cl.focusgame.gamificacion_service.exception.ServicioRemotoException;
import cl.focusgame.gamificacion_service.model.EventoGamificacion;
import cl.focusgame.gamificacion_service.repository.EventoGamificacionRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GamificacionService {

    private static final String ESTADO_COMPLETADA = "COMPLETADA";
    private static final int SESIONES_POR_RACHA = 5;

    private static final Logger log = LoggerFactory.getLogger(GamificacionService.class);

    private final EventoGamificacionRepository repo;
    private final SesionClient sesionClient;
    private final ProgresoClient progresoClient;
    private final RecompensaClient recompensaClient;

    public GamificacionService(EventoGamificacionRepository repo, SesionClient sesionClient,
                                ProgresoClient progresoClient, RecompensaClient recompensaClient) {
        this.repo = repo;
        this.sesionClient = sesionClient;
        this.progresoClient = progresoClient;
        this.recompensaClient = recompensaClient;
    }

    @Transactional
    public ResumenGamificacionResponse procesarSesion(Long sesionId) {
        log.info("Procesando sesion sesionId={}", sesionId);
        SesionDTO sesion = obtenerSesion(sesionId);
        if (!ESTADO_COMPLETADA.equals(sesion.estado())) {
            log.warn("Sesion no completada sesionId={} estado={}", sesionId, sesion.estado());
            throw new ConflictoException(
                    "La sesion " + sesionId + " no esta completada, no se puede otorgar XP");
        }

        int xpOtorgado = sesion.duracionMinutos();
        int nivelAnterior = obtenerProgreso(sesion.usuarioId()).nivel();
        ProgresoDTO progresoActualizado = sumarXp(sesion.usuarioId(), xpOtorgado);

        List<String> recompensasOtorgadas = new ArrayList<>();
        if (progresoActualizado.totalSesiones() % SESIONES_POR_RACHA == 0) {
            String nombre = "Racha de " + SESIONES_POR_RACHA + " sesiones";
            otorgarRecompensa(new CrearRecompensaRequest(sesion.usuarioId(), nombre,
                    "Completaste " + progresoActualizado.totalSesiones() + " sesiones de enfoque"));
            recompensasOtorgadas.add(nombre);
        }
        if (progresoActualizado.nivel() > nivelAnterior) {
            String nombre = "Subida de nivel " + progresoActualizado.nivel();
            otorgarRecompensa(new CrearRecompensaRequest(sesion.usuarioId(), nombre,
                    "Alcanzaste el nivel " + progresoActualizado.nivel()));
            recompensasOtorgadas.add(nombre);
        }

        EventoGamificacion evento = new EventoGamificacion();
        evento.setSesionId(sesionId);
        evento.setUsuarioId(sesion.usuarioId());
        evento.setXpOtorgado(xpOtorgado);
        evento.setFecha(LocalDateTime.now());
        repo.save(evento);

        log.debug("Sesion procesada exitosamente sesionId={} usuarioId={} xpOtorgado={} nivelActual={} recompensas={}",
                sesionId, sesion.usuarioId(), xpOtorgado, progresoActualizado.nivel(), recompensasOtorgadas);

        return new ResumenGamificacionResponse(xpOtorgado, progresoActualizado.nivel(), recompensasOtorgadas);
    }

    @Transactional(readOnly = true)
    public List<EventoGamificacion> historialPorUsuario(Long usuarioId) {
        log.info("Consultando historial de gamificacion usuarioId={}", usuarioId);
        List<EventoGamificacion> eventos = repo.findByUsuarioId(usuarioId);
        log.debug("Eventos encontrados usuarioId={} cantidad={}", usuarioId, eventos.size());
        return eventos;
    }

    private SesionDTO obtenerSesion(Long sesionId) {
        try {
            return sesionClient.obtenerPorId(sesionId);
        } catch (FeignException.NotFound e) {
            log.warn("Sesion no encontrada en productividad-service sesionId={}", sesionId);
            throw new RecursoNoEncontradoException("La sesion " + sesionId + " no existe");
        } catch (FeignException e) {
            log.error("Error de comunicacion con productividad-service sesionId={} status={}", sesionId, e.status(), e);
            throw new ServicioRemotoException("No se pudo obtener la sesion " + sesionId + " de productividad-service", e);
        }
    }

    private ProgresoDTO obtenerProgreso(Long usuarioId) {
        try {
            return progresoClient.obtenerPorUsuario(usuarioId);
        } catch (FeignException.NotFound e) {
            log.warn("Progreso no encontrado en progreso-service usuarioId={}", usuarioId);
            throw new RecursoNoEncontradoException("El usuario " + usuarioId + " no tiene progreso registrado");
        } catch (FeignException e) {
            log.error("Error de comunicacion con progreso-service usuarioId={} status={}", usuarioId, e.status(), e);
            throw new ServicioRemotoException("No se pudo obtener el progreso del usuario " + usuarioId, e);
        }
    }

    private ProgresoDTO sumarXp(Long usuarioId, int xp) {
        try {
            return progresoClient.sumarXp(usuarioId, xp);
        } catch (FeignException.NotFound e) {
            log.warn("Progreso no encontrado al sumar XP en progreso-service usuarioId={}", usuarioId);
            throw new RecursoNoEncontradoException("El usuario " + usuarioId + " no tiene progreso registrado");
        } catch (FeignException e) {
            log.error("Error de comunicacion con progreso-service al sumar XP usuarioId={} status={}", usuarioId, e.status(), e);
            throw new ServicioRemotoException("No se pudo sumar XP al usuario " + usuarioId, e);
        }
    }

    private void otorgarRecompensa(CrearRecompensaRequest req) {
        try {
            recompensaClient.otorgar(req);
        } catch (FeignException e) {
            log.error("Error de comunicacion con recompensa-service usuarioId={} status={}", req.usuarioId(), e.status(), e);
            throw new ServicioRemotoException("No se pudo otorgar la recompensa al usuario " + req.usuarioId(), e);
        }
    }
}
