package cl.focusgame.productividad_service.client;

import cl.focusgame.productividad_service.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Cliente HTTP declarativo hacia usuario-service.
@FeignClient(name = "usuario-service", url = "${usuario-service.url}", configuration = FeignConfig.class)
public interface UsuarioClient {

    @GetMapping("/api/usuarios/{id}")
    UsuarioDTO obtenerPorId(@PathVariable("id") Long id);
}
