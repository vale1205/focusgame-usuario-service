package cl.focusgame.gamificacion_service.exception;

public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
