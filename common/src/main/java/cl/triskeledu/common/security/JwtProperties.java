package cl.triskeledu.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Propiedades de configuración para JWT.
 *
 * Se mapean automáticamente desde el application.yml de cada microservicio:
 *
 *   jwt:
 *     secret: <clave-base64>
 *     expiration-ms: 3600000
 *
 * @ConfigurationProperties vincula el prefijo "jwt" a los campos de esta clase.
 * Spring inyecta los valores al arrancar, evitando que estén hardcodeados en el código.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Clave secreta en Base64 para firmar y verificar tokens JWT.
     * Debe tener al menos 256 bits (32 bytes) para HMAC-SHA256.
     */
    private String secret;

    /**
     * Tiempo de expiración del token en milisegundos.
     * Valor por defecto: 3600000 ms = 1 hora.
     */
    private long expirationMs = 3600000;
}
