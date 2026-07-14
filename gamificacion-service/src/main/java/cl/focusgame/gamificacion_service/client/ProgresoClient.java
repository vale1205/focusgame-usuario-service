package cl.focusgame.gamificacion_service.client;

import cl.focusgame.gamificacion_service.dto.ProgresoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Cliente HTTP declarativo hacia progreso-service.
@FeignClient(name = "progreso-service", url = "${progreso-service.url}", configuration = FeignConfig.class)
public interface ProgresoClient {

    @GetMapping("/api/progresos/usuario/{usuarioId}")
    ProgresoDTO obtenerPorUsuario(@PathVariable("usuarioId") Long usuarioId);

    @PutMapping("/api/progresos/usuario/{usuarioId}/sumar-xp")
    ProgresoDTO sumarXp(@PathVariable("usuarioId") Long usuarioId, @RequestParam("puntos") Integer puntos);
}
