package cl.focusgame.api_gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.RequestPath;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthHeaderLoggingFilterTest {

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private GatewayFilterChain chain;

    private final AuthHeaderLoggingFilter filter = new AuthHeaderLoggingFilter();

    @Test
    @DisplayName("filter deja pasar la peticion hacia la cadena sin modificarla, con Authorization presente")
    void filterContinuaLaCadenaCuandoHayAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer token-jwt");
        RequestPath path = RequestPath.parse("/api/progresos", null);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> resultado = filter.filter(exchange, chain);

        assertThat(resultado).isNotNull();
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("filter deja pasar la peticion hacia la cadena sin modificarla, sin Authorization")
    void filterContinuaLaCadenaSinAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        RequestPath path = RequestPath.parse("/api/progresos", null);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain);

        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("getOrder devuelve la precedencia mas baja")
    void getOrderDevuelveLowestPrecedence() {
        assertThat(filter.getOrder()).isEqualTo(org.springframework.core.Ordered.LOWEST_PRECEDENCE);
    }
}
