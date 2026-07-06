package cl.focusgame.productividad_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sesiones")
public class Sesion {

    @Schema(description = "Identificador unico de la sesion", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Id del usuario que realiza la sesion", example = "10")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Schema(description = "Id de la tarea asociada a la sesion", example = "5")
    @Column(name = "tarea_id", nullable = false)
    private Long tareaId;

    @Schema(description = "Duracion planificada de la sesion en minutos", example = "25")
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Schema(description = "Estado actual de la sesion", example = "EN_CURSO",
            allowableValues = {"EN_CURSO", "COMPLETADA", "CANCELADA"})
    @Column(nullable = false)
    private String estado; // EN_CURSO, COMPLETADA, CANCELADA

    @Schema(description = "Fecha y hora en que se inicio la sesion", example = "2026-07-03T09:15:00")
    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Schema(description = "Fecha y hora en que finalizo (completada o cancelada) la sesion", example = "2026-07-03T09:40:00")
    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;
}
