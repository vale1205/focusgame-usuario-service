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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SesionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SesionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SesionService service;

    @MockBean
    private SesionModelAssembler assembler;

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
    void listarDevuelve200ConColeccionDeSesiones() throws Exception {
        Sesion s = crearSesion(1L, 10L, 5L, "EN_CURSO");
        when(service.listarTodas()).thenReturn(List.of(s));
        when(assembler.toCollectionModel(any())).thenReturn(CollectionModel.of(List.of(EntityModel.of(s))));

        mockMvc.perform(get("/api/sesiones"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("EN_CURSO")));
    }

    @Test
    void obtenerDevuelve200ConLaSesion() throws Exception {
        Sesion s = crearSesion(1L, 10L, 5L, "EN_CURSO");
        when(service.buscarPorId(1L)).thenReturn(s);
        when(assembler.toModel(s)).thenReturn(EntityModel.of(s));

        mockMvc.perform(get("/api/sesiones/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"));
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
        when(assembler.toModel(creada)).thenReturn(EntityModel.of(creada));

        mockMvc.perform(post("/api/sesiones")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CrearSesionRequest(10L, 5L, 25))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"));
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
        when(assembler.toModel(completada)).thenReturn(EntityModel.of(completada));

        mockMvc.perform(put("/api/sesiones/{id}/completar", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }

    @Test
    void cancelarDevuelve200ConEstadoCancelada() throws Exception {
        Sesion cancelada = crearSesion(1L, 10L, 5L, "CANCELADA");
        when(service.cancelar(1L)).thenReturn(cancelada);
        when(assembler.toModel(cancelada)).thenReturn(EntityModel.of(cancelada));

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
