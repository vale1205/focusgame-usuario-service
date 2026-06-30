package cl.focusgame.usuario_service.controller;

import cl.focusgame.usuario_service.assembler.UsuarioModelAssembler;
import cl.focusgame.usuario_service.dto.LoginRequest;
import cl.focusgame.usuario_service.dto.RegistroRequest;
import cl.focusgame.usuario_service.dto.TokenResponse;
import cl.focusgame.usuario_service.dto.UsuarioResponse;
import cl.focusgame.usuario_service.exception.ConflictoException;
import cl.focusgame.usuario_service.exception.CredencialesInvalidasException;
import cl.focusgame.usuario_service.security.JwtAuthenticationFilter;
import cl.focusgame.usuario_service.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UsuarioModelAssembler assembler;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void registrarDevuelve201ConElUsuarioCreado() throws Exception {
        UsuarioResponse creado = new UsuarioResponse(1L, "a@b.com", "ana");
        when(authService.registrar(any())).thenReturn(creado);
        when(assembler.toModel(creado)).thenReturn(EntityModel.of(creado));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new RegistroRequest("a@b.com", "ana", "clave123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("ana"));
    }

    @Test
    void registrarDevuelve409CuandoElEmailYaExiste() throws Exception {
        when(authService.registrar(any())).thenThrow(new ConflictoException("El email ya esta registrado"));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new RegistroRequest("a@b.com", "ana", "clave123"))))
                .andExpect(status().isConflict());
    }

    @Test
    void registrarDevuelve400CuandoFaltanDatos() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new RegistroRequest("", "", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginDevuelve200ConToken() throws Exception {
        when(authService.login(any())).thenReturn(new TokenResponse("token-jwt", "Bearer"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LoginRequest("ana", "clave123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void loginDevuelve401CuandoCredencialesSonInvalidas() throws Exception {
        when(authService.login(any())).thenThrow(new CredencialesInvalidasException("Usuario o password invalidos"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LoginRequest("ana", "mala"))))
                .andExpect(status().isUnauthorized());
    }
}
