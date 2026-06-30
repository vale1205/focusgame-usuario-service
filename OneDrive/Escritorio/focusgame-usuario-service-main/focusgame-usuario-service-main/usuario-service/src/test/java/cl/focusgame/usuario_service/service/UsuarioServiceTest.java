package cl.focusgame.usuario_service.service;

import cl.focusgame.usuario_service.dto.ActualizarUsuarioRequest;
import cl.focusgame.usuario_service.dto.UsuarioResponse;
import cl.focusgame.usuario_service.exception.RecursoNoEncontradoException;
import cl.focusgame.usuario_service.model.Usuario;
import cl.focusgame.usuario_service.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repo;

    @InjectMocks
    private UsuarioService service;

    private Usuario crearUsuario(Long id, String email, String username) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        u.setUsername(username);
        u.setPasswordHash("hash-secreto");
        return u;
    }

    @Test
    void listarTodosMapeaUsuariosAResponseSinExponerPassword() {
        when(repo.findAll()).thenReturn(List.of(crearUsuario(1L, "a@b.com", "ana")));

        List<UsuarioResponse> resultado = service.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
        assertThat(resultado.get(0).email()).isEqualTo("a@b.com");
        assertThat(resultado.get(0).username()).isEqualTo("ana");
    }

    @Test
    void buscarPorIdDevuelveUsuarioCuandoExiste() {
        when(repo.findById(1L)).thenReturn(Optional.of(crearUsuario(1L, "a@b.com", "ana")));

        UsuarioResponse resultado = service.buscarPorId(1L);

        assertThat(resultado.username()).isEqualTo("ana");
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void actualizarModificaEmailYUsername() {
        Usuario existente = crearUsuario(1L, "viejo@b.com", "viejo");
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse resultado = service.actualizar(1L,
                new ActualizarUsuarioRequest("nuevo@b.com", "nuevo"));

        assertThat(resultado.email()).isEqualTo("nuevo@b.com");
        assertThat(resultado.username()).isEqualTo("nuevo");
    }

    @Test
    void actualizarLanzaExcepcionCuandoUsuarioNoExiste() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L,
                new ActualizarUsuarioRequest("a@b.com", "ana")))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminarBorraCuandoExiste() {
        when(repo.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(repo).deleteById(1L);
    }

    @Test
    void eliminarLanzaExcepcionCuandoNoExiste() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).deleteById(any());
    }
}
