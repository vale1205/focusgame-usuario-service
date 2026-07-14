package cl.focusgame.gamificacion_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Cuerpo enviado a recompensa-service para otorgar una recompensa via Feign.
public record CrearRecompensaRequest(
        @NotNull(message = "El usuarioId es obligatorio")
        Long usuarioId,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(min = 5, max = 255, message = "La descripcion debe tener entre 5 y 255 caracteres")
        String descripcion
) {}
