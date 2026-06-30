package cl.focusgame.usuario_service.dto;

// DTO de salida: NUNCA expone el passwordHash.
public record UsuarioResponse(Long id, String email, String username) {}
