package cl.focusgame.productividad_service.assembler;

import cl.focusgame.productividad_service.model.Tarea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class TareaModelAssemblerTest {

    private final TareaModelAssembler assembler = new TareaModelAssembler();

    @BeforeEach
    void configurarContextoDeRequest() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void limpiarContextoDeRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("toModel agrega los links self, tareas, tareasDelUsuario, actualizarEstado y eliminar")
    void toModelAgregaLinksEsperados() {
        Tarea tarea = new Tarea();
        tarea.setId(1L);
        tarea.setTitulo("Estudiar");
        tarea.setUsuarioId(10L);
        tarea.setEstado("PENDIENTE");

        EntityModel<Tarea> resultado = assembler.toModel(tarea);

        assertThat(resultado.getContent()).isEqualTo(tarea);
        assertThat(resultado.getLink("self")).isPresent();
        assertThat(resultado.getLink("tareas")).isPresent();
        assertThat(resultado.getLink("tareasDelUsuario")).isPresent();
        assertThat(resultado.getLink("actualizarEstado")).isPresent();
        assertThat(resultado.getLink("eliminar")).isPresent();
    }
}
