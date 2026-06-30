package cl.focusgame.productividad_service.controller;

import cl.focusgame.productividad_service.dto.CrearTareaRequest;
import cl.focusgame.productividad_service.model.Tarea;
import cl.focusgame.productividad_service.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    private final TareaService service;

    public TareaController(TareaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Tarea>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Tarea>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.listarPorUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarea> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Tarea> crear(@Valid @RequestBody CrearTareaRequest req) {
        Tarea creada = service.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Tarea> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        return ResponseEntity.ok(service.actualizarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
