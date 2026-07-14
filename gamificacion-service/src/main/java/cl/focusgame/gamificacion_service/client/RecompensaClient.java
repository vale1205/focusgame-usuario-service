package cl.focusgame.gamificacion_service.client;

import cl.focusgame.gamificacion_service.dto.CrearRecompensaRequest;
import cl.focusgame.gamificacion_service.dto.RecompensaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Cliente HTTP declarativo hacia recompensa-service.
@FeignClient(name = "recompensa-service", url = "${recompensa-service.url}", configuration = FeignConfig.class)
public interface RecompensaClient {

    @PostMapping("/api/recompensas")
    RecompensaDTO otorgar(@RequestBody CrearRecompensaRequest req);
}
