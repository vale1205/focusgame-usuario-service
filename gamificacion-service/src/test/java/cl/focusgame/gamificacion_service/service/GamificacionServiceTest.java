package cl.focusgame.gamificacion_service.service;

import cl.focusgame.gamificacion_service.client.ProgresoClient;
import cl.focusgame.gamificacion_service.client.RecompensaClient;
import cl.focusgame.gamificacion_service.client.SesionClient;
import cl.focusgame.gamificacion_service.dto.CrearRecompensaRequest;
import cl.focusgame.gamificacion_service.dto.ProgresoDTO;
import cl.focusgame.gamificacion_service.dto.ResumenGamificacionResponse;
import cl.focusgame.gamificacion_service.dto.SesionDTO;
import cl.focusgame.gamificacion_service.exception.ConflictoException;
import cl.focusgame.gamificacion_service.exception.RecursoNoEncontradoException;
import cl.focusgame.gamificacion_service.exception.ServicioRemotoException;
import cl.focusgame.gamificacion_service.repository.EventoGamificacionRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamificacionServiceTest {

    @Mock
    private EventoGamificacionRepository repo;

    @Mock
    private SesionClient sesionClient;

    @Mock
    private ProgresoClient progresoClient;

    @Mock
    private RecompensaClient recompensaClient;

    @InjectMocks
    private GamificacionService service;

    private FeignException.NotFound notFound(String path) {
        Request request = Request.create(Request.HttpMethod.GET, path,
                Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.NotFound("not found", request, null, null);
    }

    private FeignException errorServidor(String path) {
        Request request = Request.create(Request.HttpMethod.GET, path,
                Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate());
        Response response = Response.builder()
                .status(500)
                .request(request)
                .headers(Map.of())
                .build();
        return FeignException.errorStatus("methodKey", response);
    }

    @Test
    void procesarSesionCalculaXpYNoOtorgaRecompensasCuandoNoAplican() {
        when(sesionClient.obtenerPorId(1L)).thenReturn(new SesionDTO(1L, 10L, 25, "COMPLETADA"));
        when(progresoClient.obtenerPorUsuario(10L)).thenReturn(new ProgresoDTO(1L, 10L, 50, 1, 2));
        when(progresoClient.sumarXp(10L, 25)).thenReturn(new ProgresoDTO(1L, 10L, 75, 1, 3));

        ResumenGamificacionResponse resumen = service.procesarSesion(1L);

        assertThat(resumen.xpOtorgado()).isEqualTo(25);
        assertThat(resumen.nivelActual()).isEqualTo(1);
        assertThat(resumen.recompensasOtorgadas()).isEmpty();
        verify(recompensaClient, never()).otorgar(any());
        verify(repo).save(any());
    }

    @Test
    void procesarSesionOtorgaRachaDeCincoSesionesCuandoTotalSesionesEsMultiploDeCinco() {
        when(sesionClient.obtenerPorId(1L)).thenReturn(new SesionDTO(1L, 10L, 25, "COMPLETADA"));
        when(progresoClient.obtenerPorUsuario(10L)).thenReturn(new ProgresoDTO(1L, 10L, 75, 1, 4));
        when(progresoClient.sumarXp(10L, 25)).thenReturn(new ProgresoDTO(1L, 10L, 100, 2, 5));

        ResumenGamificacionResponse resumen = service.procesarSesion(1L);

        assertThat(resumen.recompensasOtorgadas()).contains("Racha de 5 sesiones");
        verify(recompensaClient).otorgar(new CrearRecompensaRequest(10L, "Racha de 5 sesiones",
                "Completaste 5 sesiones de enfoque"));
    }

    @Test
    void procesarSesionOtorgaRecompensaDeSubidaDeNivelCuandoElNivelSube() {
        when(sesionClient.obtenerPorId(1L)).thenReturn(new SesionDTO(1L, 10L, 25, "COMPLETADA"));
        when(progresoClient.obtenerPorUsuario(10L)).thenReturn(new ProgresoDTO(1L, 10L, 80, 1, 3));
        when(progresoClient.sumarXp(10L, 25)).thenReturn(new ProgresoDTO(1L, 10L, 105, 2, 4));

        ResumenGamificacionResponse resumen = service.procesarSesion(1L);

        assertThat(resumen.recompensasOtorgadas()).contains("Subida de nivel 2");
        verify(recompensaClient).otorgar(new CrearRecompensaRequest(10L, "Subida de nivel 2",
                "Alcanzaste el nivel 2"));
    }

    @Test
    void procesarSesionLanzaConflictoCuandoLaSesionNoEstaCompletada() {
        when(sesionClient.obtenerPorId(1L)).thenReturn(new SesionDTO(1L, 10L, 25, "EN_CURSO"));

        assertThatThrownBy(() -> service.procesarSesion(1L))
                .isInstanceOf(ConflictoException.class);

        verify(progresoClient, never()).sumarXp(any(), any());
        verify(repo, never()).save(any());
    }

    @Test
    void procesarSesionLanzaExcepcionCuandoLaSesionNoExiste() {
        when(sesionClient.obtenerPorId(99L)).thenThrow(notFound("/api/sesiones/99"));

        assertThatThrownBy(() -> service.procesarSesion(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void procesarSesionLanzaExcepcionCuandoElUsuarioNoTieneProgreso() {
        when(sesionClient.obtenerPorId(1L)).thenReturn(new SesionDTO(1L, 10L, 25, "COMPLETADA"));
        when(progresoClient.obtenerPorUsuario(10L)).thenThrow(notFound("/api/progresos/usuario/10"));

        assertThatThrownBy(() -> service.procesarSesion(1L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(progresoClient, never()).sumarXp(any(), any());
    }

    @Test
    void historialPorUsuarioDelegaEnElRepositorio() {
        service.historialPorUsuario(10L);

        verify(repo).findByUsuarioId(10L);
    }

    @Test
    void procesarSesionLanzaServicioRemotoExceptionCuandoFallaComunicacionConProductividadService() {
        when(sesionClient.obtenerPorId(1L)).thenThrow(errorServidor("/api/sesiones/1"));

        assertThatThrownBy(() -> service.procesarSesion(1L))
                .isInstanceOf(ServicioRemotoException.class);
    }

    @Test
    void procesarSesionLanzaServicioRemotoExceptionCuandoFallaComunicacionAlObtenerProgreso() {
        when(sesionClient.obtenerPorId(1L)).thenReturn(new SesionDTO(1L, 10L, 25, "COMPLETADA"));
        when(progresoClient.obtenerPorUsuario(10L)).thenThrow(errorServidor("/api/progresos/usuario/10"));

        assertThatThrownBy(() -> service.procesarSesion(1L))
                .isInstanceOf(ServicioRemotoException.class);

        verify(progresoClient, never()).sumarXp(any(), any());
    }

    @Test
    void procesarSesionLanzaServicioRemotoExceptionCuandoFallaComunicacionAlSumarXp() {
        when(sesionClient.obtenerPorId(1L)).thenReturn(new SesionDTO(1L, 10L, 25, "COMPLETADA"));
        when(progresoClient.obtenerPorUsuario(10L)).thenReturn(new ProgresoDTO(1L, 10L, 50, 1, 2));
        when(progresoClient.sumarXp(10L, 25)).thenThrow(errorServidor("/api/progresos/usuario/10/sumar-xp"));

        assertThatThrownBy(() -> service.procesarSesion(1L))
                .isInstanceOf(ServicioRemotoException.class);

        verify(repo, never()).save(any());
    }

    @Test
    void procesarSesionLanzaServicioRemotoExceptionCuandoFallaComunicacionAlOtorgarRecompensa() {
        when(sesionClient.obtenerPorId(1L)).thenReturn(new SesionDTO(1L, 10L, 25, "COMPLETADA"));
        when(progresoClient.obtenerPorUsuario(10L)).thenReturn(new ProgresoDTO(1L, 10L, 75, 1, 4));
        when(progresoClient.sumarXp(10L, 25)).thenReturn(new ProgresoDTO(1L, 10L, 100, 2, 5));
        doThrow(errorServidor("/api/recompensas")).when(recompensaClient).otorgar(any());

        assertThatThrownBy(() -> service.procesarSesion(1L))
                .isInstanceOf(ServicioRemotoException.class);
    }
}
