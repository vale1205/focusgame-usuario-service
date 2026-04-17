
package cl.focusgame.usuario_service.model;

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
    private String estado; // PENDIENTE, EN_PROCESO, COMPLETO

    @Column(nullable = false)
    private Long usuarioId;
}