package cl.focusgame.productividad_service.controller;

import cl.focusgame.productividad_service.assembler.SesionModelAssembler;
import cl.focusgame.productividad_service.dto.CrearSesionRequest;
import cl.focusgame.productividad_service.exception.RecursoNoEncontradoException;
import cl.focusgame.productividad_service.model.Sesion;
import cl.focusgame.productividad_service.security.JwtAuthenticationFilter;
import cl.focusgame.productividad_service.service.SesionService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SesionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SesionModelAssembler.class)
class SesionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SesionService service;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Sesion crearSesion(Long id, Long usuarioId, Long tareaId, String estado) {
        Sesion s = new Sesion();
        s.setId(id);
        s.setUsuarioId(usuarioId);
        s.setTareaId(tareaId);
        s.setDuracionMinutos(25);
        s.setEstado(estado);
        s.setFechaInicio(LocalDateTime.now());
        return s;
    }

    @Test
    void listarDevuelve200ConColeccionDeSesionesYLinksHateoas() throws Exception {
        Sesion s = crearSesion(1L, 10L, 5L, "EN_CURSO");
        when(service.listarTodas()).thenReturn(List.of(s));

        mockMvc.perform(get("/api/sesiones"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("EN_CURSO")))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(content().string(containsString("\"eliminar\"")))
                .andExpect(content().string(containsString("\"completar\"")))
                .andExpect(content().string(containsString("\"cancelar\"")));
    }

    @Test
    void listarDevuelve204CuandoNoHaySesiones() throws Exception {
        when(service.listarTodas()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/sesiones"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void listarPorUsuarioDevuelve200ConSesionesDelUsuario() throws Exception {
        Sesion s = crearSesion(1L, 10L, 5L, "EN_CURSO");
        when(service.listarPorUsuario(10L)).thenReturn(List.of(s));

        mockMvc.perform(get("/api/sesiones/usuario/{usuarioId}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("EN_CURSO")))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void listarPorUsuarioDevuelve204CuandoElUsuarioNoTieneSesiones() throws Exception {
        when(service.listarPorUsuario(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/sesiones/usuario/{usuarioId}", 10L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void obtenerDevuelve200ConLaSesionYLinksDeAccionCuandoEstaEnCurso() throws Exception {
        Sesion s = crearSesion(1L, 10L, 5L, "EN_CURSO");
        when(service.buscarPorId(1L)).thenReturn(s);

        mockMvc.perform(get("/api/sesiones/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.eliminar.href").exists())
                .andExpect(jsonPath("$._links.completar.href").exists())
                .andExpect(jsonPath("$._links.cancelar.href").exists());
    }

    @Test
    void obtenerDevuelve200SinLinksDeAccionCuandoNoEstaEnCurso() throws Exception {
        Sesion s = crearSesion(1L, 10L, 5L, "COMPLETADA");
        when(service.buscarPorId(1L)).thenReturn(s);

        mockMvc.perform(get("/api/sesiones/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.eliminar.href").exists())
                .andExpect(jsonPath("$._links.completar").doesNotExist())
                .andExpect(jsonPath("$._links.cancelar").doesNotExist());
    }

    @Test
    void obtenerDevuelve404CuandoNoExiste() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("Sesion con id 99 no existe"));

        mockMvc.perform(get("/api/sesiones/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void iniciarDevuelve201ConLaSesionCreada() throws Exception {
        Sesion creada = crearSesion(1L, 10L, 5L, "EN_CURSO");
        when(service.iniciar(any())).thenReturn(creada);

        mockMvc.perform(post("/api/sesiones")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CrearSesionRequest(10L, 5L, 25))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void iniciarDevuelve400CuandoFaltanDatos() throws Exception {
        mockMvc.perform(post("/api/sesiones")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CrearSesionRequest(null, null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completarDevuelve200ConEstadoCompletada() throws Exception {
        Sesion completada = crearSesion(1L, 10L, 5L, "COMPLETADA");
        when(service.completar(1L)).thenReturn(completada);

        mockMvc.perform(put("/api/sesiones/{id}/completar", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }

    @Test
    void cancelarDevuelve200ConEstadoCancelada() throws Exception {
        Sesion cancelada = crearSesion(1L, 10L, 5L, "CANCELADA");
        when(service.cancelar(1L)).thenReturn(cancelada);

        mockMvc.perform(put("/api/sesiones/{id}/cancelar", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/sesiones/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
