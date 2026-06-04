package cl.focusgame.productividad_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearTareaRequest(
        @NotBlank(message = "El titulo es obligatorio")
        String titulo,

        @NotBlank(message = "La descripcion es obligatoria")
        String descripcion,

        @NotNull(message = "El usuarioId es obligatorio")
        Long usuarioId
) {}
