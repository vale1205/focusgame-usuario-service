package cl.focusgame.gamificacion_service.client;

import cl.focusgame.gamificacion_service.dto.SesionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Cliente HTTP declarativo hacia productividad-service.
@FeignClient(name = "productividad-service", url = "${productividad-service.url}", configuration = FeignConfig.class)
public interface SesionClient {

    @GetMapping("/api/sesiones/{id}")
    SesionDTO obtenerPorId(@PathVariable("id") Long id);
}
