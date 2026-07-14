package cl.focusgame.usuario_service.model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Schema(description = "Identificador unico del usuario", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Correo electronico unico del usuario", example = "ana@focusgame.cl")
    @Column(unique = true, nullable = false)
    private String email;

    @Schema(description = "Nombre de usuario unico", example = "ana")
    @Column(unique = true, nullable = false)
    private String username;

    @Schema(description = "Hash de la contrasena del usuario (nunca se expone en las respuestas)",
            example = "$2a$10$D9y1v6l3wq5j6z...", hidden = true)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
}
