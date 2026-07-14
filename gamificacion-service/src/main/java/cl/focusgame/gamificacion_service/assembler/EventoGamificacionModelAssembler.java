package cl.focusgame.gamificacion_service.assembler;

import cl.focusgame.gamificacion_service.controller.GamificacionController;
import cl.focusgame.gamificacion_service.model.EventoGamificacion;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EventoGamificacionModelAssembler
        implements RepresentationModelAssembler<EventoGamificacion, EntityModel<EventoGamificacion>> {

    @Override
    public EntityModel<EventoGamificacion> toModel(EventoGamificacion evento) {
        return EntityModel.of(evento,
                linkTo(methodOn(GamificacionController.class).historialPorUsuario(evento.getUsuarioId()))
                        .withRel("historialDelUsuario"));
    }
}
