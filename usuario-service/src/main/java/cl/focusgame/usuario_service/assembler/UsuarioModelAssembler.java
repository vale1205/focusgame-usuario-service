package cl.focusgame.usuario_service.assembler;

import cl.focusgame.usuario_service.controller.UsuarioController;
import cl.focusgame.usuario_service.dto.UsuarioResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UsuarioModelAssembler implements RepresentationModelAssembler<UsuarioResponse, EntityModel<UsuarioResponse>> {

    @Override
    public EntityModel<UsuarioResponse> toModel(UsuarioResponse usuario) {
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).obtener(usuario.id())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listar()).withRel("usuarios"),
                linkTo(methodOn(UsuarioController.class).actualizar(usuario.id(), null)).withRel("actualizar"),
                linkTo(methodOn(UsuarioController.class).eliminar(usuario.id())).withRel("eliminar"));
    }
}
