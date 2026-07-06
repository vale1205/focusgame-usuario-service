package cl.focusgame.productividad_service.controller;

import cl.focusgame.productividad_service.assembler.SesionModelAssembler;
import cl.focusgame.productividad_service.dto.CrearSesionRequest;
import cl.focusgame.productividad_service.model.Sesion;
import cl.focusgame.productividad_service.service.SesionService;
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
@RequestMapping("/api/sesiones")
@Tag(name = "Sesiones", description = "Gestion de sesiones de enfoque")
public class SesionController {

    private final SesionService service;
    private final SesionModelAssembler assembler;

    public SesionController(SesionService service, SesionModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @Operation(summary = "Lista todas las sesiones",
            description = "Devuelve la coleccion completa de sesiones de enfoque registradas en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Se encontraron sesiones y se devuelve la coleccion"),
            @ApiResponse(responseCode = "204", description = "No existen sesiones registradas"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Sesion>>> listar() {
        List<Sesion> sesiones = service.listarTodas();
        if (sesiones.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        CollectionModel<EntityModel<Sesion>> modelo = assembler.toCollectionModel(sesiones)
                .add(linkTo(methodOn(SesionController.class).listar()).withSelfRel());
        return ResponseEntity.ok(modelo);
    }

    @Operation(summary = "Lista las sesiones de un usuario",
            description = "Devuelve todas las sesiones de enfoque asociadas al identificador de usuario indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Se encontraron sesiones del usuario y se devuelve la coleccion"),
            @ApiResponse(responseCode = "204", description = "El usuario no tiene sesiones registradas"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CollectionModel<EntityModel<Sesion>>> listarPorUsuario(@PathVariable Long usuarioId) {
        List<Sesion> sesiones = service.listarPorUsuario(usuarioId);
        if (sesiones.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        CollectionModel<EntityModel<Sesion>> modelo = assembler.toCollectionModel(sesiones)
                .add(linkTo(methodOn(SesionController.class).listarPorUsuario(usuarioId)).withSelfRel());
        return ResponseEntity.ok(modelo);
    }

    @Operation(summary = "Obtiene una sesion por id",
            description = "Busca y devuelve una sesion de enfoque especifica a partir de su identificador unico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "La sesion fue encontrada y se devuelve en el cuerpo de la respuesta"),
            @ApiResponse(responseCode = "404", description = "No existe una sesion con el id indicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Sesion>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Inicia una nueva sesion de enfoque",
            description = "Crea una sesion en estado EN_CURSO para el usuario y la tarea indicados, validando previamente que ambos existan.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "La sesion fue creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Los datos de la sesion son invalidos (usuarioId, tareaId o duracion faltantes o incorrectos)"),
            @ApiResponse(responseCode = "404", description = "El usuario o la tarea indicados no existen"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Sesion>> iniciar(@Valid @RequestBody CrearSesionRequest req) {
        Sesion creada = service.iniciar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(creada));
    }

    @Operation(summary = "Marca una sesion como completada",
            description = "Cambia el estado de una sesion en curso a COMPLETADA y registra la fecha de fin.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "La sesion fue marcada como completada"),
            @ApiResponse(responseCode = "404", description = "No existe una sesion con el id indicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @PutMapping("/{id}/completar")
    public ResponseEntity<EntityModel<Sesion>> completar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.completar(id)));
    }

    @Operation(summary = "Cancela una sesion en curso",
            description = "Cambia el estado de una sesion en curso a CANCELADA y registra la fecha de fin.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "La sesion fue cancelada"),
            @ApiResponse(responseCode = "404", description = "No existe una sesion con el id indicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<EntityModel<Sesion>> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.cancelar(id)));
    }

    @Operation(summary = "Elimina una sesion",
            description = "Elimina definitivamente la sesion con el id indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "La sesion fue eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe una sesion con el id indicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
