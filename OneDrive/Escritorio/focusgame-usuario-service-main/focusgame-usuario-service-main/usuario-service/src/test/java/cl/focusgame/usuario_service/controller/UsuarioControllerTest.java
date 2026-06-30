package cl.focusgame.usuario_service.controller;

import cl.focusgame.usuario_service.assembler.UsuarioModelAssembler;
import cl.focusgame.usuario_service.dto.ActualizarUsuarioRequest;
import cl.focusgame.usuario_service.dto.UsuarioResponse;
import cl.focusgame.usuario_service.exception.RecursoNoEncontradoException;
import cl.focusgame.usuario_service.security.JwtAuthenticationFilter;
import cl.focusgame.usuario_service.service.UsuarioService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService service;

    @MockBean
    private UsuarioModelAssembler assembler;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listarDevuelve200ConColeccionDeUsuarios() throws Exception {
        UsuarioResponse u = new UsuarioResponse(1L, "a@b.com", "ana");
        when(service.listarTodos()).thenReturn(List.of(u));
        when(assembler.toCollectionModel(any())).thenReturn(CollectionModel.of(List.of(EntityModel.of(u))));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ana")));
    }

    @Test
    void obtenerDevuelve200ConElUsuario() throws Exception {
        UsuarioResponse u = new UsuarioResponse(1L, "a@b.com", "ana");
        when(service.buscarPorId(1L)).thenReturn(u);
        when(assembler.toModel(u)).thenReturn(EntityModel.of(u));

        mockMvc.perform(get("/api/usuarios/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana"));
    }

    @Test
    void obtenerDevuelve404CuandoNoExiste() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("Usuario con id 99 no existe"));

        mockMvc.perform(get("/api/usuarios/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarDevuelve200ConElUsuarioActualizado() throws Exception {
        UsuarioResponse actualizado = new UsuarioResponse(1L, "nuevo@b.com", "nuevo");
        ActualizarUsuarioRequest req = new ActualizarUsuarioRequest("nuevo@b.com", "nuevo");
        when(service.actualizar(eq(1L), any())).thenReturn(actualizado);
        when(assembler.toModel(actualizado)).thenReturn(EntityModel.of(actualizado));

        mockMvc.perform(put("/api/usuarios/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nuevo@b.com"));
    }

    @Test
    void actualizarDevuelve400CuandoElEmailEsInvalido() throws Exception {
        ActualizarUsuarioRequest req = new ActualizarUsuarioRequest("no-es-un-email", "nuevo");

        mockMvc.perform(put("/api/usuarios/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
