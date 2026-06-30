package cl.focusgame.productividad_service.service;

import cl.focusgame.productividad_service.client.UsuarioClient;
import cl.focusgame.productividad_service.dto.CrearSesionRequest;
import cl.focusgame.productividad_service.exception.RecursoNoEncontradoException;
import cl.focusgame.productividad_service.model.Sesion;
import cl.focusgame.productividad_service.repository.SesionRepository;
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
class SesionServiceTest {

    @Mock
    private SesionRepository repo;

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private SesionService service;

    private Sesion crearSesion(Long id, Long usuarioId, Long tareaId, String estado) {
        Sesion s = new Sesion();
        s.setId(id);
        s.setUsuarioId(usuarioId);
        s.setTareaId(tareaId);
        s.setDuracionMinutos(25);
        s.setEstado(estado);
        return s;
    }

    private FeignException.NotFound usuarioNoEncontrado() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/usuarios/1",
                java.util.Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.NotFound("not found", request, null, null);
    }

    @Test
    void listarPorUsuarioFiltraPorUsuarioId() {
        when(repo.findByUsuarioId(10L)).thenReturn(List.of(crearSesion(1L, 10L, 5L, "EN_CURSO")));

        List<Sesion> resultado = service.listarPorUsuario(10L);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void iniciarCreaSesionEnCursoCuandoUsuarioYTareaExisten() {
        CrearSesionRequest req = new CrearSesionRequest(10L, 5L, 25);
        when(tareaRepository.existsById(5L)).thenReturn(true);
        when(repo.save(any(Sesion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sesion resultado = service.iniciar(req);

        assertThat(resultado.getEstado()).isEqualTo("EN_CURSO");
        assertThat(resultado.getFechaInicio()).isNotNull();
        verify(usuarioClient).obtenerPorId(10L);
    }

    @Test
    void iniciarLanzaExcepcionCuandoUsuarioNoExiste() {
        CrearSesionRequest req = new CrearSesionRequest(999L, 5L, 25);
        when(usuarioClient.obtenerPorId(999L)).thenThrow(usuarioNoEncontrado());

        assertThatThrownBy(() -> service.iniciar(req))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).save(any());
    }

    @Test
    void iniciarLanzaExcepcionCuandoTareaNoExiste() {
        CrearSesionRequest req = new CrearSesionRequest(10L, 999L, 25);
        when(tareaRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.iniciar(req))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).save(any());
    }

    @Test
    void completarMarcaLaSesionComoCompletadaYRegistraFechaFin() {
        Sesion existente = crearSesion(1L, 10L, 5L, "EN_CURSO");
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Sesion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sesion resultado = service.completar(1L);

        assertThat(resultado.getEstado()).isEqualTo("COMPLETADA");
        assertThat(resultado.getFechaFin()).isNotNull();
    }

    @Test
    void cancelarMarcaLaSesionComoCancelada() {
        Sesion existente = crearSesion(1L, 10L, 5L, "EN_CURSO");
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Sesion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sesion resultado = service.cancelar(1L);

        assertThat(resultado.getEstado()).isEqualTo("CANCELADA");
    }

    @Test
    void eliminarLanzaExcepcionCuandoNoExiste() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
