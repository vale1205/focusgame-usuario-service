package cl.focusgame.productividad_service.dto;

// Representacion del usuario remoto recibido desde usuario-service via Feign.
public record UsuarioDTO(Long id, String email, String username) {}
