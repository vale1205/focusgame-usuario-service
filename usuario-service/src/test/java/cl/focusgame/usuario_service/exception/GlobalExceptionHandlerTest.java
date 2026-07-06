package cl.focusgame.usuario_service.exception;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("noEncontrado devuelve 404 con el mensaje de la excepcion")
    void noEncontradoDevuelve404() {
        when(request.getRequestURI()).thenReturn("/api/usuarios/99");

        ResponseEntity<ErrorResponse> resultado = handler.noEncontrado(
                new RecursoNoEncontradoException("Usuario con id 99 no existe"), request);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resultado.getBody().message()).isEqualTo("Usuario con id 99 no existe");
        assertThat(resultado.getBody().path()).isEqualTo("/api/usuarios/99");
    }

    @Test
    @DisplayName("conflicto devuelve 409 con el mensaje de la excepcion")
    void conflictoDevuelve409() {
        when(request.getRequestURI()).thenReturn("/api/auth/registro");

        ResponseEntity<ErrorResponse> resultado = handler.conflicto(
                new ConflictoException("El email ya esta registrado"), request);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resultado.getBody().message()).isEqualTo("El email ya esta registrado");
    }

    @Test
    @DisplayName("credenciales devuelve 401 con el mensaje de la excepcion")
    void credencialesDevuelve401() {
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        ResponseEntity<ErrorResponse> resultado = handler.credenciales(
                new CredencialesInvalidasException("Usuario o password invalidos"), request);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resultado.getBody().message()).isEqualTo("Usuario o password invalidos");
    }

    @Test
    @DisplayName("validacion devuelve 400 con el detalle de cada campo invalido")
    void validacionDevuelve400ConDetalles() {
        when(request.getRequestURI()).thenReturn("/api/usuarios/1");
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("actualizarUsuarioRequest", "email", "debe ser un email valido");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> resultado = handler.validacion(ex, request);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resultado.getBody().message()).isEqualTo("Datos de entrada invalidos");
        assertThat(resultado.getBody().detalles()).containsExactly("email: debe ser un email valido");
    }

    @Test
    @DisplayName("generico devuelve 500 para cualquier excepcion no controlada")
    void genericoDevuelve500() {
        when(request.getRequestURI()).thenReturn("/api/usuarios");

        ResponseEntity<ErrorResponse> resultado = handler.generico(
                new RuntimeException("fallo inesperado"), request);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resultado.getBody().message()).isEqualTo("Error interno del servidor");
        assertThat(resultado.getBody().path()).isEqualTo("/api/usuarios");
    }
}
