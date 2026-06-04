package cl.focusgame.productividad_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearSesionRequest(
        @NotNull(message = "El usuarioId es obligatorio")
        Long usuarioId,

        @NotNull(message = "El tareaId es obligatorio")
        Long tareaId,

        @NotNull(message = "La duracion es obligatoria")
        @Positive(message = "La duracion debe ser mayor a 0")
        Integer duracionMinutos
) {}
