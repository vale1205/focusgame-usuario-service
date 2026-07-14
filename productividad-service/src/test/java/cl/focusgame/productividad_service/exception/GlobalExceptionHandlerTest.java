package cl.focusgame.productividad_service.exception;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private FeignException.NotFound feignNotFound() {
        Request feignRequest = Request.create(Request.HttpMethod.GET, "/api/usuarios/1",
                Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.NotFound("not found", feignRequest, null, null);
    }

    @Test
    @DisplayName("noEncontrado devuelve 404 con el mensaje de la excepcion")
    void noEncontradoDevuelve404() {
        when(request.getRequestURI()).thenReturn("/api/tareas/99");

        ResponseEntity<ErrorResponse> resultado = handler.noEncontrado(
                new RecursoNoEncontradoException("Tarea con id 99 no existe"), request);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resultado.getBody().message()).isEqualTo("Tarea con id 99 no existe");
    }

    @Test
    @DisplayName("validacion devuelve 400 con el detalle de cada campo invalido")
    void validacionDevuelve400ConDetalles() {
        when(request.getRequestURI()).thenReturn("/api/tareas");
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("crearTareaRequest", "titulo", "no debe estar vacio");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> resultado = handler.validacion(ex, request);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resultado.getBody().detalles()).containsExactly("titulo: no debe estar vacio");
    }

    @Test
    @DisplayName("feign devuelve 502 cuando falla la comunicacion con usuario-service")
    void feignDevuelve502() {
        when(request.getRequestURI()).thenReturn("/api/tareas");

        ResponseEntity<ErrorResponse> resultado = handler.feign(feignNotFound(), request);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(resultado.getBody().message()).isEqualTo("No se pudo contactar al servicio de usuarios");
    }

    @Test
    @DisplayName("remoto devuelve 503 con el mensaje de la excepcion")
    void remotoDevuelve503() {
        when(request.getRequestURI()).thenReturn("/api/tareas");

        ResponseEntity<ErrorResponse> resultado = handler.remoto(
                new ServicioRemotoException("Servicio de usuarios no disponible"), request);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(resultado.getBody().message()).isEqualTo("Servicio de usuarios no disponible");
    }

    @Test
    @DisplayName("generico devuelve 500 para cualquier excepcion no controlada")
    void genericoDevuelve500() {
        when(request.getRequestURI()).thenReturn("/api/tareas");

        ResponseEntity<ErrorResponse> resultado = handler.generico(
                new RuntimeException("fallo inesperado"), request);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resultado.getBody().message()).isEqualTo("Error interno del servidor");
    }
}
