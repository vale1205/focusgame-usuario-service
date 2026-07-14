package cl.focusgame.gamificacion_service.dto;

// Representacion del progreso remoto recibido desde progreso-service via Feign.
public record ProgresoDTO(Long id, Long usuarioId, Integer puntosXp, Integer nivel, Integer totalSesiones) {}
