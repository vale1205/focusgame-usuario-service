package cl.focusgame.usuario_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import cl.focusgame.usuario_service.model.Tarea; 

public interface TareaRepository extends JpaRepository<Tarea, Long> {
    List<Tarea> findByUsuarioId(Long usuarioId);
}