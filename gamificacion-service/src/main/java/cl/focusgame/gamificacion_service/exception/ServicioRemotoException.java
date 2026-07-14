package cl.focusgame.gamificacion_service.exception;

// Se lanza cuando falla la comunicacion con un microservicio remoto.
public class ServicioRemotoException extends RuntimeException {
    public ServicioRemotoException(String mensaje) {
        super(mensaje);
    }

    public ServicioRemotoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
