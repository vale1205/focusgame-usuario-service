package cl.focusgame.productividad_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tareas")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String estado; // PENDIENTE, EN_PROCESO, COMPLETADA

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
}
