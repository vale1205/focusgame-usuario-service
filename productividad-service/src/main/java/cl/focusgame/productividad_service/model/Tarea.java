package cl.focusgame.productividad_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tareas")
public class Tarea {

    @Schema(description = "Identificador unico de la tarea", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Titulo breve de la tarea", example = "Estudiar para el examen")
    @Column(nullable = false)
    private String titulo;

    @Schema(description = "Descripcion detallada de la tarea", example = "Repasar los capitulos 3 y 4 del libro")
    @Column(nullable = false)
    private String descripcion;

    @Schema(description = "Estado actual de la tarea", example = "PENDIENTE",
            allowableValues = {"PENDIENTE", "EN_PROCESO", "COMPLETADA"})
    @Column(nullable = false)
    private String estado; // PENDIENTE, EN_PROCESO, COMPLETADA

    @Schema(description = "Id del usuario propietario de la tarea", example = "10")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
}
