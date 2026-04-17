package cl.focusgame.usuario_service.service;

import org.springframework.stereotype.Service;
import java.util.List;

import cl.focusgame.usuario_service.model.Tarea;
import cl.focusgame.usuario_service.repository.TareaRepository;

@Service
public class TareaService {

    private final TareaRepository tareaRepository;

    public TareaService(TareaRepository tareaRepository) {
        this.tareaRepository = tareaRepository;
    }

    public List<Tarea> listarTodas() {
        return tareaRepository.findAll();
    }

    public List<Tarea> listarPorUsuario(Long usuarioId) {
        return tareaRepository.findByUsuarioId(usuarioId);
    }

    public Tarea guardar(Tarea tarea) {
        return tareaRepository.save(tarea);
    }

    public Tarea buscarPorId(Long id) {
        return tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    public Tarea actualizar(Long id, Tarea tarea) {
        Tarea existente = buscarPorId(id);

        existente.setEstado(tarea.getEstado());
        existente.setUsuarioId(tarea.getUsuarioId());

        return tareaRepository.save(existente);
    }

    public void eliminar(Long id) {
        tareaRepository.deleteById(id);
    }
}