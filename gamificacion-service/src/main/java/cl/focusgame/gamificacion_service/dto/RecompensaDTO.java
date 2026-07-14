package cl.focusgame.gamificacion_service.dto;

// Representacion de la recompensa remota recibida desde recompensa-service via Feign.
public record RecompensaDTO(Long id, Long usuarioId, String nombre, String descripcion) {}
