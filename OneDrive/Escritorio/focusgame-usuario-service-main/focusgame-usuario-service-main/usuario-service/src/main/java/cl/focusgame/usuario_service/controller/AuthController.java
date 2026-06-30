package cl.focusgame.usuario_service.controller;

import cl.focusgame.usuario_service.assembler.UsuarioModelAssembler;
import cl.focusgame.usuario_service.dto.LoginRequest;
import cl.focusgame.usuario_service.dto.RegistroRequest;
import cl.focusgame.usuario_service.dto.TokenResponse;
import cl.focusgame.usuario_service.dto.UsuarioResponse;
import cl.focusgame.usuario_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Registro y login de usuarios")
public class AuthController {

    private final AuthService authService;
    private final UsuarioModelAssembler assembler;

    public AuthController(AuthService authService, UsuarioModelAssembler assembler) {
        this.authService = authService;
        this.assembler = assembler;
    }

    @Operation(summary = "Registra un nuevo usuario")
    @PostMapping("/registro")
    public ResponseEntity<EntityModel<UsuarioResponse>> registrar(@Valid @RequestBody RegistroRequest req) {
        UsuarioResponse creado = authService.registrar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(creado));
    }

    @Operation(summary = "Autentica un usuario y devuelve un token JWT")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
