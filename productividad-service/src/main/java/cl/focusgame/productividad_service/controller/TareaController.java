package cl.focusgame.productividad_service.controller;

import cl.focusgame.productividad_service.assembler.TareaModelAssembler;
import cl.focusgame.productividad_service.dto.CrearTareaRequest;
import cl.focusgame.productividad_service.model.Tarea;
import cl.focusgame.productividad_service.service.TareaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "Lista todas las tareas",
            description = "Devuelve la coleccion completa de tareas registradas en el sistema, sin filtrar por usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Se encontraron tareas y se devuelve la coleccion"),
            @ApiResponse(responseCode = "204", description = "No existen tareas registradas"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Tarea>>> listar() {
        List<Tarea> tareas = service.listarTodas();
        if (tareas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        CollectionModel<EntityModel<Tarea>> modelo = assembler.toCollectionModel(tareas)
                .add(linkTo(methodOn(TareaController.class).listar()).withSelfRel());
        return ResponseEntity.ok(modelo);
    }

    @Operation(summary = "Lista las tareas de un usuario",
            description = "Devuelve todas las tareas asociadas al identificador de usuario indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Se encontraron tareas del usuario y se devuelve la coleccion"),
            @ApiResponse(responseCode = "204", description = "El usuario no tiene tareas registradas"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CollectionModel<EntityModel<Tarea>>> listarPorUsuario(@PathVariable Long usuarioId) {
        List<Tarea> tareas = service.listarPorUsuario(usuarioId);
        if (tareas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        CollectionModel<EntityModel<Tarea>> modelo = assembler.toCollectionModel(tareas)
                .add(linkTo(methodOn(TareaController.class).listarPorUsuario(usuarioId)).withSelfRel());
        return ResponseEntity.ok(modelo);
    }

    @Operation(summary = "Obtiene una tarea por id",
            description = "Busca y devuelve una tarea especifica a partir de su identificador unico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "La tarea fue encontrada y se devuelve en el cuerpo de la respuesta"),
            @ApiResponse(responseCode = "404", description = "No existe una tarea con el id indicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Tarea>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Crea una nueva tarea",
            description = "Registra una nueva tarea en estado PENDIENTE para el usuario indicado, validando previamente que el usuario exista.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "La tarea fue creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Los datos de la tarea son invalidos (titulo, descripcion o usuarioId faltantes)"),
            @ApiResponse(responseCode = "404", description = "El usuario indicado no existe"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Tarea>> crear(@Valid @RequestBody CrearTareaRequest req) {
        Tarea creada = service.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(creada));
    }

    @Operation(summary = "Cambia el estado de una tarea",
            description = "Actualiza el estado de una tarea existente (por ejemplo a EN_PROCESO o COMPLETADA).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El estado de la tarea fue actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe una tarea con el id indicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @PutMapping("/{id}/estado")
    public ResponseEntity<EntityModel<Tarea>> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        return ResponseEntity.ok(assembler.toModel(service.actualizarEstado(id, estado)));
    }

    @Operation(summary = "Elimina una tarea",
            description = "Elimina definitivamente la tarea con el id indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "La tarea fue eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe una tarea con el id indicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
