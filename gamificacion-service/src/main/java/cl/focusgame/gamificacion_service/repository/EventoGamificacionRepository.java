package cl.focusgame.gamificacion_service.repository;

import cl.focusgame.gamificacion_service.model.EventoGamificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoGamificacionRepository extends JpaRepository<EventoGamificacion, Long> {
    List<EventoGamificacion> findByUsuarioId(Long usuarioId);
}
