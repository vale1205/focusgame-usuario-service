package cl.focusgame.usuario_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "clave-de-prueba-de-al-menos-32-bytes-0123456789";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600000L);
    }

    @Test
    void generarTokenProduceTokenValidoConElUsernameCorrecto() {
        String token = jwtService.generarToken("ana", 1L);

        assertThat(token).isNotBlank();
        assertThat(jwtService.esValido(token)).isTrue();
        assertThat(jwtService.extraerUsername(token)).isEqualTo("ana");
    }

    @Test
    void esValidoDevuelveFalseParaTokenMalformado() {
        assertThat(jwtService.esValido("token-invalido")).isFalse();
    }

    @Test
    void esValidoDevuelveFalseParaTokenFirmadoConOtraClave() {
        JwtService otroServicio = new JwtService("otra-clave-distinta-de-al-menos-32-bytes-987654321", 3600000L);
        String token = otroServicio.generarToken("ana", 1L);

        assertThat(jwtService.esValido(token)).isFalse();
    }

    @Test
    void esValidoDevuelveFalseParaTokenExpirado() {
        JwtService servicioExpirado = new JwtService(SECRET, -1000L);
        String token = servicioExpirado.generarToken("ana", 1L);

        assertThat(jwtService.esValido(token)).isFalse();
    }
}
