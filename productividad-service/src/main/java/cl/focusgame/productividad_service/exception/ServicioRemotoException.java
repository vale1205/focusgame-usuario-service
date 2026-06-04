package cl.focusgame.productividad_service.exception;

// Se lanza cuando falla la comunicacion con un microservicio remoto.
public class ServicioRemotoException extends RuntimeException {
    public ServicioRemotoException(String mensaje) {
        super(mensaje);
    }
}
