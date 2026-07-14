package cl.focusgame.gamificacion_service.assembler;

import cl.focusgame.gamificacion_service.model.EventoGamificacion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventoGamificacionModelAssemblerTest {

    private final EventoGamificacionModelAssembler assembler = new EventoGamificacionModelAssembler();

    private EventoGamificacion crearEvento() {
        EventoGamificacion e = new EventoGamificacion();
        e.setId(1L);
        e.setSesionId(5L);
        e.setUsuarioId(10L);
        e.setXpOtorgado(25);
        e.setFecha(LocalDateTime.now());
        return e;
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
    @DisplayName("toModel agrega el link historialDelUsuario")
    void toModelAgregaLinkHistorialDelUsuario() {
        EntityModel<EventoGamificacion> resultado = assembler.toModel(crearEvento());

        assertThat(resultado.getLink("historialDelUsuario")).isPresent();
    }
}
