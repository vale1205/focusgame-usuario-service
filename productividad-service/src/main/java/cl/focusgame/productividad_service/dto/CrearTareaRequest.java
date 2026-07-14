package cl.focusgame.productividad_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearTareaRequest(
        @NotBlank(message = "El titulo es obligatorio")
        @Size(min = 3, max = 150, message = "El titulo debe tener entre 3 y 150 caracteres")
        String titulo,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(min = 5, max = 500, message = "La descripcion debe tener entre 5 y 500 caracteres")
        String descripcion,

        @NotNull(message = "El usuarioId es obligatorio")
        Long usuarioId
) {}
