package cl.focusgame.productividad_service.repository;

import cl.focusgame.productividad_service.model.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
    List<Sesion> findByUsuarioId(Long usuarioId);
    List<Sesion> findByTareaId(Long tareaId);
}
