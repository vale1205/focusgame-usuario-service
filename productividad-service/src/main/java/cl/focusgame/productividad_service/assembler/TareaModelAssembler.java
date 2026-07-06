package cl.focusgame.productividad_service.assembler;

import cl.focusgame.productividad_service.controller.TareaController;
import cl.focusgame.productividad_service.model.Tarea;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TareaModelAssembler implements RepresentationModelAssembler<Tarea, EntityModel<Tarea>> {

    @Override
    public EntityModel<Tarea> toModel(Tarea tarea) {
        return EntityModel.of(tarea,
                linkTo(methodOn(TareaController.class).obtener(tarea.getId())).withSelfRel(),
                linkTo(methodOn(TareaController.class).listar()).withRel("tareas"),
                linkTo(methodOn(TareaController.class).listarPorUsuario(tarea.getUsuarioId())).withRel("tareasDelUsuario"),
                linkTo(methodOn(TareaController.class).cambiarEstado(tarea.getId(), null)).withRel("actualizarEstado"),
                linkTo(methodOn(TareaController.class).eliminar(tarea.getId())).withRel("eliminar"));
    }
}
