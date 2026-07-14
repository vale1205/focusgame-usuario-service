package cl.focusgame.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// El gateway NO valida el JWT: solo reenvia el header Authorization tal cual
// llega hacia el microservicio destino (cada uno valida su propio token).
// Este filtro solo deja traza de la peticion para trazabilidad/depuracion.
@Component
public class AuthHeaderLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthHeaderLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        boolean tieneAuth = exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
        log.info("Gateway: {} {} (Authorization presente: {})",
                exchange.getRequest().getMethod(), exchange.getRequest().getPath().value(), tieneAuth);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
