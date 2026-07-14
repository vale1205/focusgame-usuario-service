package cl.focusgame.usuario_service.service;

import cl.focusgame.usuario_service.dto.LoginRequest;
import cl.focusgame.usuario_service.dto.RegistroRequest;
import cl.focusgame.usuario_service.dto.TokenResponse;
import cl.focusgame.usuario_service.dto.UsuarioResponse;
import cl.focusgame.usuario_service.exception.ConflictoException;
import cl.focusgame.usuario_service.exception.CredencialesInvalidasException;
import cl.focusgame.usuario_service.model.Usuario;
import cl.focusgame.usuario_service.repository.UsuarioRepository;
import cl.focusgame.usuario_service.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository repo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest req) {
        log.info("Registrando usuario email={} username={}", req.email(), req.username());
        if (repo.existsByEmail(req.email())) {
            log.warn("Registro rechazado: email ya registrado email={}", req.email());
            throw new ConflictoException("El email ya esta registrado");
        }
        if (repo.existsByUsername(req.username())) {
            log.warn("Registro rechazado: username ya en uso username={}", req.username());
            throw new ConflictoException("El username ya esta en uso");
        }
        Usuario u = new Usuario();
        u.setEmail(req.email());
        u.setUsername(req.username());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        Usuario guardado = repo.save(u);
        log.debug("Usuario registrado exitosamente id={} username={}", guardado.getId(), guardado.getUsername());
        return new UsuarioResponse(guardado.getId(), guardado.getEmail(), guardado.getUsername());
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        log.info("Intento de login username={}", req.username());
        Usuario u = repo.findByUsername(req.username())
                .orElseThrow(() -> {
                    log.warn("Login fallido: username no existe username={}", req.username());
                    return new CredencialesInvalidasException("Usuario o password invalidos");
                });
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            log.warn("Login fallido: password incorrecta username={}", req.username());
            throw new CredencialesInvalidasException("Usuario o password invalidos");
        }
        String token = jwtService.generarToken(u.getUsername(), u.getId());
        log.debug("Login exitoso username={}", u.getUsername());
        return new TokenResponse(token, "Bearer");
    }
}
