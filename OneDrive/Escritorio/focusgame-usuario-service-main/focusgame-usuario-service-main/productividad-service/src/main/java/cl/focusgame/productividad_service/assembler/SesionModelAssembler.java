package cl.focusgame.productividad_service.assembler;

import cl.focusgame.productividad_service.controller.SesionController;
import cl.focusgame.productividad_service.model.Sesion;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SesionModelAssembler implements RepresentationModelAssembler<Sesion, EntityModel<Sesion>> {

    @Override
    public EntityModel<Sesion> toModel(Sesion sesion) {
        EntityModel<Sesion> modelo = EntityModel.of(sesion,
                linkTo(methodOn(SesionController.class).obtener(sesion.getId())).withSelfRel(),
                linkTo(methodOn(SesionController.class).listar()).withRel("sesiones"),
                linkTo(methodOn(SesionController.class).listarPorUsuario(sesion.getUsuarioId())).withRel("sesionesDelUsuario"));

        if ("EN_CURSO".equals(sesion.getEstado())) {
            modelo.add(linkTo(methodOn(SesionController.class).completar(sesion.getId())).withRel("completar"));
            modelo.add(linkTo(methodOn(SesionController.class).cancelar(sesion.getId())).withRel("cancelar"));
        }
        return modelo;
    }
}
