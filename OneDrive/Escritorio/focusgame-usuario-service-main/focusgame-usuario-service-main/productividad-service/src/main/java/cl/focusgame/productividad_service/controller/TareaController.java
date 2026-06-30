package cl.focusgame.productividad_service.controller;

import cl.focusgame.productividad_service.assembler.TareaModelAssembler;
import cl.focusgame.productividad_service.dto.CrearTareaRequest;
import cl.focusgame.productividad_service.model.Tarea;
import cl.focusgame.productividad_service.service.TareaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/tareas")
@Tag(name = "Tareas", description = "Gestion de tareas de los usuarios")
public class TareaController {

    private final TareaService service;
    private final TareaModelAssembler assembler;

    public TareaController(TareaService service, TareaModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @Operation(summary = "Lista todas las tareas")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Tarea>>> listar() {
        CollectionModel<EntityModel<Tarea>> modelo = assembler.toCollectionModel(service.listarTodas())
                .add(linkTo(methodOn(TareaController.class).listar()).withSelfRel());
        return ResponseEntity.ok(modelo);
    }

    @Operation(summary = "Lista las tareas de un usuario")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CollectionModel<EntityModel<Tarea>>> listarPorUsuario(@PathVariable Long usuarioId) {
        CollectionModel<EntityModel<Tarea>> modelo = assembler.toCollectionModel(service.listarPorUsuario(usuarioId))
                .add(linkTo(methodOn(TareaController.class).listarPorUsuario(usuarioId)).withSelfRel());
        return ResponseEntity.ok(modelo);
    }

    @Operation(summary = "Obtiene una tarea por id")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Tarea>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Crea una nueva tarea")
    @PostMapping
    public ResponseEntity<EntityModel<Tarea>> crear(@Valid @RequestBody CrearTareaRequest req) {
        Tarea creada = service.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(creada));
    }

    @Operation(summary = "Cambia el estado de una tarea")
    @PutMapping("/{id}/estado")
    public ResponseEntity<EntityModel<Tarea>> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        return ResponseEntity.ok(assembler.toModel(service.actualizarEstado(id, estado)));
    }

    @Operation(summary = "Elimina una tarea")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
