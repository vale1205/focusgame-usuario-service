package cl.focusgame.gamificacion_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "clave-de-prueba-de-al-menos-32-bytes-0123456789";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    private String token(String secret, String username, long expiraEnMs) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date ahora = new Date();
        return Jwts.builder()
                .subject(username)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiraEnMs))
                .signWith(key)
                .compact();
    }

    @Test
    void esValidoDevuelveTrueParaTokenFirmadoConLaMismaClave() {
        String token = token(SECRET, "ana", 3600000L);

        assertThat(jwtService.esValido(token)).isTrue();
        assertThat(jwtService.extraerUsername(token)).isEqualTo("ana");
    }

    @Test
    void esValidoDevuelveFalseParaTokenMalformado() {
        assertThat(jwtService.esValido("token-invalido")).isFalse();
    }

    @Test
    void esValidoDevuelveFalseParaTokenFirmadoConOtraClave() {
        String token = token("otra-clave-distinta-de-al-menos-32-bytes-987654321", "ana", 3600000L);

        assertThat(jwtService.esValido(token)).isFalse();
    }

    @Test
    void esValidoDevuelveFalseParaTokenExpirado() {
        String token = token(SECRET, "ana", -1000L);

        assertThat(jwtService.esValido(token)).isFalse();
    }
}
