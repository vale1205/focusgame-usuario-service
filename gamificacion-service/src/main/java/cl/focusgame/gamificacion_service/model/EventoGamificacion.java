package cl.focusgame.gamificacion_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "eventos_gamificacion")
public class EventoGamificacion {

    @Schema(description = "Identificador unico del evento de gamificacion", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Id de la sesion de enfoque que origino el evento", example = "5")
    @Column(name = "sesion_id", nullable = false)
    private Long sesionId;

    @Schema(description = "Id del usuario que recibio el XP", example = "10")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Schema(description = "Cantidad de XP otorgada por la sesion", example = "25")
    @Column(name = "xp_otorgado", nullable = false)
    private Integer xpOtorgado;

    @Schema(description = "Fecha y hora en que se proceso el evento", example = "2026-07-03T09:40:00")
    @Column(nullable = false)
    private LocalDateTime fecha;
}
