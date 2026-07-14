package cl.focusgame.gamificacion_service.controller;

import cl.focusgame.gamificacion_service.assembler.EventoGamificacionModelAssembler;
import cl.focusgame.gamificacion_service.dto.ResumenGamificacionResponse;
import cl.focusgame.gamificacion_service.model.EventoGamificacion;
import cl.focusgame.gamificacion_service.service.GamificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/gamificacion")
@Tag(name = "Gamificacion", description = "Motor de reglas de gamificacion: XP, niveles y recompensas")
public class GamificacionController {

    private final GamificacionService service;
    private final EventoGamificacionModelAssembler assembler;

    public GamificacionController(GamificacionService service, EventoGamificacionModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @Operation(summary = "Procesa una sesion completada y otorga XP y recompensas",
            description = "Obtiene la sesion desde productividad-service (debe estar COMPLETADA), calcula el XP "
                    + "segun su duracion, se lo suma al usuario en progreso-service y evalua reglas de badges "
                    + "(racha de 5 sesiones y subida de nivel) otorgando recompensas via recompensa-service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "La sesion fue procesada y se devuelve el resumen de XP y recompensas otorgadas"),
            @ApiResponse(responseCode = "404", description = "La sesion no existe o el usuario no tiene progreso registrado"),
            @ApiResponse(responseCode = "409", description = "La sesion existe pero no esta en estado COMPLETADA"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @PostMapping("/procesar-sesion/{sesionId}")
    public ResponseEntity<ResumenGamificacionResponse> procesarSesion(@PathVariable Long sesionId) {
        return ResponseEntity.ok(service.procesarSesion(sesionId));
    }

    @Operation(summary = "Historial de eventos de gamificacion de un usuario",
            description = "Devuelve el historial de sesiones procesadas (XP otorgado) para el usuario indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Se encontraron eventos y se devuelve la coleccion"),
            @ApiResponse(responseCode = "204", description = "El usuario no tiene eventos registrados"),
            @ApiResponse(responseCode = "401", description = "No autenticado: falta o es invalido el token JWT")
    })
    @GetMapping("/eventos/usuario/{usuarioId}")
    public ResponseEntity<CollectionModel<EntityModel<EventoGamificacion>>> historialPorUsuario(@PathVariable Long usuarioId) {
        List<EventoGamificacion> eventos = service.historialPorUsuario(usuarioId);
        if (eventos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        CollectionModel<EntityModel<EventoGamificacion>> modelo = assembler.toCollectionModel(eventos)
                .add(linkTo(methodOn(GamificacionController.class).historialPorUsuario(usuarioId)).withSelfRel());
        return ResponseEntity.ok(modelo);
    }
}
