package cl.triskeledu.usuarios.controller;

import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.triskeledu.common.security.JwtTokenProvider;
import cl.triskeledu.common.security.TokenBlacklistService;
import cl.triskeledu.usuarios.dto.LoginRequest;
import cl.triskeledu.usuarios.dto.LoginResponse;
import cl.triskeledu.usuarios.dto.RegisterRequest;
import cl.triskeledu.usuarios.dto.UsuarioResponse;
import cl.triskeledu.usuarios.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador de autenticación (público, sin requerir token JWT).
 *
 * Endpoints:
 * - POST /api/v1/auth/login    → Iniciar sesión, obtener token JWT
 * - POST /api/v1/auth/register → Registrar nuevo usuario (rol Cliente)
 * - POST /api/v1/auth/logout   → Cerrar sesión, invalidar token JWT
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Endpoint de inicio de sesión.
     *
     * Recibe email y contraseña, valida las credenciales y retorna
     * un token JWT que el cliente debe incluir en futuras peticiones
     * como header: Authorization: Bearer <token>
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de registro público.
     *
     * Permite a cualquier persona crear una cuenta con rol "Cliente".
     * La contraseña se hashea con BCrypt antes de almacenarla.
     */
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {
        UsuarioResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint de cierre de sesión (logout).
     *
     * Recibe el token JWT en el header Authorization y lo agrega
     * a una blacklist en memoria. El token quedará invalidado
     * hasta que expire naturalmente (limpieza automática cada 10 min).
     *
     * Requiere el header: Authorization: Bearer <token>
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authorizationHeader) {

        // Extraer el token del header "Bearer <token>"
        String token = authorizationHeader.substring(7);

        // Obtener la fecha de expiración del token para la limpieza automática
        Date expiration = jwtTokenProvider.getExpirationFromToken(token);

        // Agregar a la blacklist
        tokenBlacklistService.addToBlacklist(token, expiration);

        String email = jwtTokenProvider.getEmailFromToken(token);
        log.info("Logout exitoso para: {}", email);

        return ResponseEntity.ok(Map.of("message", "Sesión cerrada exitosamente"));
    }
}
