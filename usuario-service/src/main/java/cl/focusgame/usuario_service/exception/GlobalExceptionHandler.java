package cl.focusgame.usuario_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex,
                                                       HttpServletRequest req) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> conflicto(ConflictoException ex,
                                                    HttpServletRequest req) {
        log.warn("Conflicto: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> credenciales(CredencialesInvalidasException ex,
                                        HttpServletRequest req) {
        log.warn("Credenciales invalidas en {}", req.getRequestURI());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex,
                            HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        log.warn("Error de validacion en {}: {}", req.getRequestURI(), detalles);
        return build(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos", req, detalles);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> generico(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado en {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req, List.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String msg,
                                                HttpServletRequest req, List<String> detalles) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                msg, req.getRequestURI(), detalles);
        return ResponseEntity.status(status).body(body);
    }
}
