package cl.focusgame.gamificacion_service.dto;

// Representacion de la sesion remota recibida desde productividad-service via Feign.
public record SesionDTO(Long id, Long usuarioId, Integer duracionMinutos, String estado) {}
