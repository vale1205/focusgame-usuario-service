package cl.focusgame.gamificacion_service.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class FeignConfigTest {

    private final RequestInterceptor interceptor = new FeignConfig().authForwardingInterceptor();

    @AfterEach
    void limpiarContextoDeRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Propaga el header Authorization cuando esta presente en la peticion entrante")
    void propagaAuthorizationCuandoEstaPresente() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-jwt");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers().get("Authorization")).containsExactly("Bearer token-jwt");
    }

    @Test
    @DisplayName("No agrega header cuando la peticion entrante no trae Authorization")
    void noPropagaHeaderCuandoNoHayAuthorization() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers().get("Authorization")).isNull();
    }

    @Test
    @DisplayName("No falla cuando no hay contexto de peticion HTTP disponible")
    void noFallaCuandoNoHayContextoDeRequest() {
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers().get("Authorization")).isNull();
    }
}
