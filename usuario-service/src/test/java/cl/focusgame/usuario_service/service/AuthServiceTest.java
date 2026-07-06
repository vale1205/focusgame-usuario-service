package cl.focusgame.usuario_service.service;

import cl.focusgame.usuario_service.dto.LoginRequest;
import cl.focusgame.usuario_service.dto.RegistroRequest;
import cl.focusgame.usuario_service.dto.TokenResponse;
import cl.focusgame.usuario_service.dto.UsuarioResponse;
import cl.focusgame.usuario_service.exception.ConflictoException;
import cl.focusgame.usuario_service.exception.CredencialesInvalidasException;
import cl.focusgame.usuario_service.model.Usuario;
import cl.focusgame.usuario_service.repository.UsuarioRepository;
import cl.focusgame.usuario_service.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository repo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registrarCreaUsuarioConPasswordCifrada() {
        RegistroRequest req = new RegistroRequest("a@b.com", "ana", "clave123");
        when(repo.existsByEmail("a@b.com")).thenReturn(false);
        when(repo.existsByUsername("ana")).thenReturn(false);
        when(passwordEncoder.encode("clave123")).thenReturn("hash-cifrado");
        when(repo.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponse resultado = authService.registrar(req);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.email()).isEqualTo("a@b.com");
        assertThat(resultado.username()).isEqualTo("ana");

        verify(repo).save(argThat(u -> "hash-cifrado".equals(u.getPasswordHash())));
    }

    @Test
    void registrarLanzaConflictoSiEmailYaExiste() {
        RegistroRequest req = new RegistroRequest("a@b.com", "ana", "clave123");
        when(repo.existsByEmail("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(req))
                .isInstanceOf(ConflictoException.class);

        verify(repo, never()).save(any());
    }

    @Test
    void registrarLanzaConflictoSiUsernameYaExiste() {
        RegistroRequest req = new RegistroRequest("a@b.com", "ana", "clave123");
        when(repo.existsByEmail("a@b.com")).thenReturn(false);
        when(repo.existsByUsername("ana")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(req))
                .isInstanceOf(ConflictoException.class);

        verify(repo, never()).save(any());
    }

    @Test
    void loginDevuelveTokenCuandoCredencialesSonValidas() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("ana");
        u.setPasswordHash("hash-cifrado");

        when(repo.findByUsername("ana")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("clave123", "hash-cifrado")).thenReturn(true);
        when(jwtService.generarToken("ana", 1L)).thenReturn("token-jwt");

        TokenResponse resultado = authService.login(new LoginRequest("ana", "clave123"));

        assertThat(resultado.token()).isEqualTo("token-jwt");
        assertThat(resultado.tipo()).isEqualTo("Bearer");
    }

    @Test
    void loginLanzaCredencialesInvalidasCuandoUsuarioNoExiste() {
        when(repo.findByUsername("ana")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana", "clave123")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void loginLanzaCredencialesInvalidasCuandoPasswordNoCoincide() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("ana");
        u.setPasswordHash("hash-cifrado");

        when(repo.findByUsername("ana")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("incorrecta", "hash-cifrado")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana", "incorrecta")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }
}
