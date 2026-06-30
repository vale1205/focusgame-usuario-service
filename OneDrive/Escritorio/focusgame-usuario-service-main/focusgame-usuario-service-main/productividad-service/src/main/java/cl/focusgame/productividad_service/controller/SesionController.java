package cl.focusgame.productividad_service.controller;

import cl.focusgame.productividad_service.assembler.SesionModelAssembler;
import cl.focusgame.productividad_service.dto.CrearSesionRequest;
import cl.focusgame.productividad_service.model.Sesion;
import cl.focusgame.productividad_service.service.SesionService;
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
@RequestMapping("/api/sesiones")
@Tag(name = "Sesiones", description = "Gestion de sesiones de enfoque")
public class SesionController {

    private final SesionService service;
    private final SesionModelAssembler assembler;

    public SesionController(SesionService service, SesionModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @Operation(summary = "Lista todas las sesiones")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Sesion>>> listar() {
        CollectionModel<EntityModel<Sesion>> modelo = assembler.toCollectionModel(service.listarTodas())
                .add(linkTo(methodOn(SesionController.class).listar()).withSelfRel());
        return ResponseEntity.ok(modelo);
    }

    @Operation(summary = "Lista las sesiones de un usuario")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CollectionModel<EntityModel<Sesion>>> listarPorUsuario(@PathVariable Long usuarioId) {
        CollectionModel<EntityModel<Sesion>> modelo = assembler.toCollectionModel(service.listarPorUsuario(usuarioId))
                .add(linkTo(methodOn(SesionController.class).listarPorUsuario(usuarioId)).withSelfRel());
        return ResponseEntity.ok(modelo);
    }

    @Operation(summary = "Obtiene una sesion por id")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Sesion>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Inicia una nueva sesion de enfoque")
    @PostMapping
    public ResponseEntity<EntityModel<Sesion>> iniciar(@Valid @RequestBody CrearSesionRequest req) {
        Sesion creada = service.iniciar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(creada));
    }

    @Operation(summary = "Marca una sesion como completada")
    @PutMapping("/{id}/completar")
    public ResponseEntity<EntityModel<Sesion>> completar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.completar(id)));
    }

    @Operation(summary = "Cancela una sesion en curso")
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<EntityModel<Sesion>> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.cancelar(id)));
    }

    @Operation(summary = "Elimina una sesion")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
