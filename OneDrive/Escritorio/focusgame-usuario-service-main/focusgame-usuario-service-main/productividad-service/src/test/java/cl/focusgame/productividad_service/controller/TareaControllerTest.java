package cl.focusgame.productividad_service.controller;

import cl.focusgame.productividad_service.assembler.TareaModelAssembler;
import cl.focusgame.productividad_service.dto.CrearTareaRequest;
import cl.focusgame.productividad_service.exception.RecursoNoEncontradoException;
import cl.focusgame.productividad_service.model.Tarea;
import cl.focusgame.productividad_service.security.JwtAuthenticationFilter;
import cl.focusgame.productividad_service.service.TareaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TareaController.class)
@AutoConfigureMockMvc(addFilters = false)
class TareaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TareaService service;

    @MockBean
    private TareaModelAssembler assembler;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Tarea crearTarea(Long id, Long usuarioId, String estado) {
        Tarea t = new Tarea();
        t.setId(id);
        t.setTitulo("Estudiar");
        t.setDescripcion("Repasar examen");
        t.setUsuarioId(usuarioId);
        t.setEstado(estado);
        return t;
    }

    @Test
    void listarDevuelve200ConColeccionDeTareas() throws Exception {
        Tarea t = crearTarea(1L, 10L, "PENDIENTE");
        when(service.listarTodas()).thenReturn(List.of(t));
        when(assembler.toCollectionModel(any())).thenReturn(CollectionModel.of(List.of(EntityModel.of(t))));

        mockMvc.perform(get("/api/tareas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Estudiar")));
    }

    @Test
    void obtenerDevuelve200ConLaTarea() throws Exception {
        Tarea t = crearTarea(1L, 10L, "PENDIENTE");
        when(service.buscarPorId(1L)).thenReturn(t);
        when(assembler.toModel(t)).thenReturn(EntityModel.of(t));

        mockMvc.perform(get("/api/tareas/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Estudiar"));
    }

    @Test
    void obtenerDevuelve404CuandoNoExiste() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("Tarea con id 99 no existe"));

        mockMvc.perform(get("/api/tareas/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearDevuelve201ConLaTareaCreada() throws Exception {
        Tarea creada = crearTarea(1L, 10L, "PENDIENTE");
        when(service.crear(any())).thenReturn(creada);
        when(assembler.toModel(creada)).thenReturn(EntityModel.of(creada));

        mockMvc.perform(post("/api/tareas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new CrearTareaRequest("Estudiar", "Repasar examen", 10L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void crearDevuelve400CuandoFaltanDatos() throws Exception {
        mockMvc.perform(post("/api/tareas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new CrearTareaRequest("", "", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearDevuelve404CuandoElUsuarioNoExiste() throws Exception {
        when(service.crear(any())).thenThrow(new RecursoNoEncontradoException("El usuario 999 no existe"));

        mockMvc.perform(post("/api/tareas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new CrearTareaRequest("Estudiar", "Repasar examen", 999L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void cambiarEstadoDevuelve200ConElNuevoEstado() throws Exception {
        Tarea actualizada = crearTarea(1L, 10L, "EN_PROCESO");
        when(service.actualizarEstado(eq(1L), eq("EN_PROCESO"))).thenReturn(actualizada);
        when(assembler.toModel(actualizada)).thenReturn(EntityModel.of(actualizada));

        mockMvc.perform(put("/api/tareas/{id}/estado", 1L).param("estado", "EN_PROCESO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/tareas/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
