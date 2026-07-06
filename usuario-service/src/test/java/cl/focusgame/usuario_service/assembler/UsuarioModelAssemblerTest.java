package cl.focusgame.usuario_service.assembler;

import cl.focusgame.usuario_service.dto.UsuarioResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioModelAssemblerTest {

    private final UsuarioModelAssembler assembler = new UsuarioModelAssembler();

    @BeforeEach
    void configurarContextoDeRequest() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void limpiarContextoDeRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("toModel agrega los links self, usuarios, actualizar y eliminar al UsuarioResponse")
    void toModelAgregaLinksSelfYUsuarios() {
        UsuarioResponse usuario = new UsuarioResponse(1L, "a@b.com", "ana");

        EntityModel<UsuarioResponse> resultado = assembler.toModel(usuario);

        assertThat(resultado.getContent()).isEqualTo(usuario);
        assertThat(resultado.getLink("self")).isPresent();
        assertThat(resultado.getLink("usuarios")).isPresent();
        assertThat(resultado.getLink("actualizar")).isPresent();
        assertThat(resultado.getLink("eliminar")).isPresent();
    }
}
