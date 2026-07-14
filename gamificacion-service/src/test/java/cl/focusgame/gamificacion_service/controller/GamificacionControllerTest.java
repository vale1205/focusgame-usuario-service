package cl.focusgame.gamificacion_service.controller;

import cl.focusgame.gamificacion_service.assembler.EventoGamificacionModelAssembler;
import cl.focusgame.gamificacion_service.dto.ResumenGamificacionResponse;
import cl.focusgame.gamificacion_service.exception.ConflictoException;
import cl.focusgame.gamificacion_service.exception.RecursoNoEncontradoException;
import cl.focusgame.gamificacion_service.model.EventoGamificacion;
import cl.focusgame.gamificacion_service.security.JwtAuthenticationFilter;
import cl.focusgame.gamificacion_service.service.GamificacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GamificacionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(EventoGamificacionModelAssembler.class)
class GamificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GamificacionService service;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private EventoGamificacion crearEvento(Long id, Long sesionId, Long usuarioId, int xp) {
        EventoGamificacion e = new EventoGamificacion();
        e.setId(id);
        e.setSesionId(sesionId);
        e.setUsuarioId(usuarioId);
        e.setXpOtorgado(xp);
        e.setFecha(LocalDateTime.now());
        return e;
    }

    @Test
    void procesarSesionDevuelve200ConElResumen() throws Exception {
        when(service.procesarSesion(1L)).thenReturn(new ResumenGamificacionResponse(25, 2, List.of("Subida de nivel 2")));

        mockMvc.perform(post("/api/gamificacion/procesar-sesion/{sesionId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.xpOtorgado").value(25))
                .andExpect(jsonPath("$.nivelActual").value(2))
                .andExpect(jsonPath("$.recompensasOtorgadas[0]").value("Subida de nivel 2"));
    }

    @Test
    void procesarSesionDevuelve404CuandoLaSesionNoExiste() throws Exception {
        when(service.procesarSesion(99L)).thenThrow(new RecursoNoEncontradoException("La sesion 99 no existe"));

        mockMvc.perform(post("/api/gamificacion/procesar-sesion/{sesionId}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void procesarSesionDevuelve409CuandoLaSesionNoEstaCompletada() throws Exception {
        when(service.procesarSesion(1L)).thenThrow(new ConflictoException("La sesion 1 no esta completada"));

        mockMvc.perform(post("/api/gamificacion/procesar-sesion/{sesionId}", 1L))
                .andExpect(status().isConflict());
    }

    @Test
    void historialPorUsuarioDevuelve200ConLosEventos() throws Exception {
        when(service.historialPorUsuario(10L)).thenReturn(List.of(crearEvento(1L, 5L, 10L, 25)));

        mockMvc.perform(get("/api/gamificacion/eventos/usuario/{usuarioId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void historialPorUsuarioDevuelve204CuandoNoHayEventos() throws Exception {
        when(service.historialPorUsuario(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/gamificacion/eventos/usuario/{usuarioId}", 10L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}
