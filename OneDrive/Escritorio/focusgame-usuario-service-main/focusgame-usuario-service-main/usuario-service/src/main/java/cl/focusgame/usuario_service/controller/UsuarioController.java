package cl.focusgame.usuario_service.controller;

import cl.focusgame.usuario_service.assembler.UsuarioModelAssembler;
import cl.focusgame.usuario_service.dto.ActualizarUsuarioRequest;
import cl.focusgame.usuario_service.dto.UsuarioResponse;
import cl.focusgame.usuario_service.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestion de usuarios registrados")
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioModelAssembler assembler;

    public UsuarioController(UsuarioService service, UsuarioModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @Operation(summary = "Lista todos los usuarios")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UsuarioResponse>>> listar() {
        CollectionModel<EntityModel<UsuarioResponse>> modelo = assembler.toCollectionModel(service.listarTodos())
                .add(linkTo(methodOn(UsuarioController.class).listar()).withSelfRel());
        return ResponseEntity.ok(modelo);
    }

    @Operation(summary = "Obtiene un usuario por id")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Actualiza email y username de un usuario")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioResponse>> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody ActualizarUsuarioRequest req) {
        return ResponseEntity.ok(assembler.toModel(service.actualizar(id, req)));
    }

    @Operation(summary = "Elimina un usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
