package cl.triskeledu.common.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Proveedor central de tokens JWT.
 *
 * Responsabilidades:
 * 1. GENERAR tokens firmados con HMAC-SHA256 incluyendo email, rol y nombre.
 * 2. VALIDAR tokens verificando firma, expiración y estructura.
 * 3. EXTRAER claims (atributos): email, rol y nombre desde un token válido.
 *
 * Esta clase vive en el módulo common para que TODOS los microservicios
 * compartan la misma lógica de tokens sin duplicar código.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    // ─── Generación ──────────────────────────────────────────────────────────

    /**
     * Genera un token JWT firmado.
     *
     * @param email          email del usuario (será el "subject" del token)
     * @param rol            rol del usuario (Administrador, Bibliotecario, Cliente)
     * @param nombreCompleto nombre completo para incluir como claim adicional
     * @return token JWT como String
     */
    public String generarToken(String email, String rol, String nombreCompleto) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .subject(email)                        // Subject = email del usuario
                .claim("rol", rol)                     // Claim personalizado: rol
                .claim("nombre", nombreCompleto)       // Claim personalizado: nombre
                .issuedAt(ahora)                       // Fecha de emisión
                .expiration(expiracion)                // Fecha de expiración
                .signWith(getSigningKey())              // Firma con HMAC-SHA256
                .compact();
    }

    // ─── Validación ──────────────────────────────────────────────────────────

    /**
     * Valida un token JWT verificando firma y expiración.
     *
     * @param token el token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    // ─── Extracción de Claims ────────────────────────────────────────────────

    /**
     * Extrae el email (subject) del token.
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrae el rol del token.
     */
    public String getRolFromToken(String token) {
        return getClaims(token).get("rol", String.class);
    }

    /**
     * Extrae el nombre completo del token.
     */
    public String getNombreFromToken(String token) {
        return getClaims(token).get("nombre", String.class);
    }

    // [SWAGGER-INI]
    /**
     * Extrae la fecha de expiración del token.
     * Utilizado por el servicio de logout para saber cuándo
     * un token en la blacklist puede ser limpiado.
     */
    public Date getExpirationFromToken(String token) {
        return getClaims(token).getExpiration();
    }
    // [SWAGGER-FIN]

    // ─── Métodos privados ────────────────────────────────────────────────────

    /**
     * Parsea el token y retorna todos los claims.
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Construye la clave de firma a partir del secreto en Base64.
     * Usa HMAC-SHA256 (requiere clave de al menos 256 bits).
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
