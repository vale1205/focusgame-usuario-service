package cl.focusgame.gamificacion_service.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// Propaga el token JWT entrante hacia los servicios remotos en cada llamada Feign,
// para que la peticion remota tambien pase la cadena de seguridad stateless.
public class FeignConfig {

    @Bean
    public RequestInterceptor authForwardingInterceptor() {
        return template -> {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                String auth = servletAttrs.getRequest().getHeader("Authorization");
                if (auth != null && !auth.isBlank()) {
                    template.header("Authorization", auth);
                }
            }
        };
    }
}
