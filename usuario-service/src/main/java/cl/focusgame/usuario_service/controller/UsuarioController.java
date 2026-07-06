package cl.focusgame.usuario_service.controller;

import cl.focusgame.usuario_service.assembler.UsuarioModelAssembler;
import cl.focusgame.usuario_service.dto.ActualizarUsuarioRequest;
import cl.focusgame.usuario_service.dto.UsuarioResponse;
import cl.focusgame.usuario_service.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "Lista todos los usuarios",
            description = "Devuelve la coleccion completa de usuarios registrados en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Se encontraron usuarios y se devuelve la coleccion"),
            @ApiResponse(responseCode = "204", description = "No existen usuarios registrados"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UsuarioResponse>>> listar() {
        List<UsuarioResponse> usuarios = service.listarTodos();
        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        CollectionModel<EntityModel<UsuarioResponse>> modelo = assembler.toCollectionModel(usuarios)
                .add(linkTo(methodOn(UsuarioController.class).listar()).withSelfRel());
        return ResponseEntity.ok(modelo);
    }

    @Operation(summary = "Obtiene un usuario por id",
            description = "Busca y devuelve un usuario especifico a partir de su identificador unico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El usuario fue encontrado y se devuelve en el cuerpo de la respuesta"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con el id indicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Actualiza email y username de un usuario",
            description = "Modifica el email y el nombre de usuario de un usuario existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El usuario fue actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Los datos enviados son invalidos (email o username faltantes o con formato incorrecto)"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con el id indicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioResponse>> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody ActualizarUsuarioRequest req) {
        return ResponseEntity.ok(assembler.toModel(service.actualizar(id, req)));
    }

    @Operation(summary = "Elimina un usuario",
            description = "Elimina definitivamente el usuario con el id indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "El usuario fue eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con el id indicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
