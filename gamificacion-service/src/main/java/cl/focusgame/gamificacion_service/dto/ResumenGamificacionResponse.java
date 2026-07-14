package cl.focusgame.gamificacion_service.dto;

import java.util.List;

// Resumen devuelto al procesar una sesion: XP otorgado, nivel resultante y recompensas otorgadas.
public record ResumenGamificacionResponse(Integer xpOtorgado, Integer nivelActual, List<String> recompensasOtorgadas) {}
