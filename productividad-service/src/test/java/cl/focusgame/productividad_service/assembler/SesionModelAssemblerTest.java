package cl.focusgame.productividad_service.assembler;

import cl.focusgame.productividad_service.model.Sesion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class SesionModelAssemblerTest {

    private final SesionModelAssembler assembler = new SesionModelAssembler();

    private Sesion crearSesion(String estado) {
        Sesion s = new Sesion();
        s.setId(1L);
        s.setUsuarioId(10L);
        s.setTareaId(5L);
        s.setDuracionMinutos(25);
        s.setEstado(estado);
        return s;
    }

    @BeforeEach
    void configurarContextoDeRequest() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void limpiarContextoDeRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("toModel agrega links completar y cancelar cuando la sesion esta EN_CURSO")
    void toModelAgregaLinksDeAccionCuandoEstaEnCurso() {
        EntityModel<Sesion> resultado = assembler.toModel(crearSesion("EN_CURSO"));

        assertThat(resultado.getLink("self")).isPresent();
        assertThat(resultado.getLink("sesiones")).isPresent();
        assertThat(resultado.getLink("sesionesDelUsuario")).isPresent();
        assertThat(resultado.getLink("eliminar")).isPresent();
        assertThat(resultado.getLink("completar")).isPresent();
        assertThat(resultado.getLink("cancelar")).isPresent();
    }

    @Test
    @DisplayName("toModel no agrega links de accion cuando la sesion no esta EN_CURSO")
    void toModelNoAgregaLinksDeAccionCuandoNoEstaEnCurso() {
        EntityModel<Sesion> resultado = assembler.toModel(crearSesion("COMPLETADA"));

        assertThat(resultado.getLink("self")).isPresent();
        assertThat(resultado.getLink("eliminar")).isPresent();
        assertThat(resultado.getLink("completar")).isEmpty();
        assertThat(resultado.getLink("cancelar")).isEmpty();
    }
}
