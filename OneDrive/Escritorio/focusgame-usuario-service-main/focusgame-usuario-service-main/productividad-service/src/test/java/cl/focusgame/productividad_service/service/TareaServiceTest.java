package cl.focusgame.productividad_service.service;

import cl.focusgame.productividad_service.client.UsuarioClient;
import cl.focusgame.productividad_service.dto.CrearTareaRequest;
import cl.focusgame.productividad_service.exception.RecursoNoEncontradoException;
import cl.focusgame.productividad_service.model.Tarea;
import cl.focusgame.productividad_service.repository.TareaRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

    @Mock
    private TareaRepository repo;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private TareaService service;

    private Tarea crearTarea(Long id, Long usuarioId, String estado) {
        Tarea t = new Tarea();
        t.setId(id);
        t.setTitulo("Estudiar");
        t.setDescripcion("Repasar examen");
        t.setUsuarioId(usuarioId);
        t.setEstado(estado);
        return t;
    }

    private FeignException.NotFound usuarioNoEncontrado() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/usuarios/1",
                java.util.Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.NotFound("not found", request, null, null);
    }

    @Test
    void listarTodasDevuelveTodasLasTareas() {
        when(repo.findAll()).thenReturn(List.of(crearTarea(1L, 10L, "PENDIENTE")));

        List<Tarea> resultado = service.listarTodas();

        assertThat(resultado).hasSize(1);
    }

    @Test
    void listarPorUsuarioFiltraPorUsuarioId() {
        when(repo.findByUsuarioId(10L)).thenReturn(List.of(crearTarea(1L, 10L, "PENDIENTE")));

        List<Tarea> resultado = service.listarPorUsuario(10L);

        assertThat(resultado).hasSize(1);
        verify(repo).findByUsuarioId(10L);
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void crearGuardaTareaConEstadoPendienteCuandoUsuarioExiste() {
        CrearTareaRequest req = new CrearTareaRequest("Estudiar", "Repasar examen", 10L);
        when(repo.save(any(Tarea.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tarea resultado = service.crear(req);

        assertThat(resultado.getEstado()).isEqualTo("PENDIENTE");
        assertThat(resultado.getUsuarioId()).isEqualTo(10L);
        verify(usuarioClient).obtenerPorId(10L);
    }

    @Test
    void crearLanzaExcepcionCuandoUsuarioNoExiste() {
        CrearTareaRequest req = new CrearTareaRequest("Estudiar", "Repasar examen", 999L);
        when(usuarioClient.obtenerPorId(999L)).thenThrow(usuarioNoEncontrado());

        assertThatThrownBy(() -> service.crear(req))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).save(any());
    }

    @Test
    void actualizarEstadoCambiaElEstadoDeLaTarea() {
        Tarea existente = crearTarea(1L, 10L, "PENDIENTE");
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Tarea.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tarea resultado = service.actualizarEstado(1L, "EN_PROCESO");

        assertThat(resultado.getEstado()).isEqualTo("EN_PROCESO");
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
    }
}
