package cl.triskeledu.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor de Feign que propaga el token JWT en llamadas entre microservicios.
 *
 * Cuando ms-catalogo llama a ms-recursos (o viceversa) vía Feign, este interceptor
 * toma el token JWT del SecurityContext actual (el que llegó en la petición original)
 * y lo agrega como header "Authorization: Bearer <token>" en la llamada Feign.
 *
 * Sin este interceptor, las llamadas inter-servicio llegarían SIN token y serían
 * rechazadas con 401 (Unauthorized) por el filtro JWT del microservicio destino.
 */
@Slf4j
@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void apply(RequestTemplate template) {
        // Obtener la autenticación actual del hilo
        // SecurityContextHolder es un contenedor que mantiene la información de
        // seguridad (autenticación) para el hilo actual (línea de ejecución en CPU).
        // El hilo actual es el que maneja la petición HTTP entrante, y el token JWT
        // se almacena allí después de ser validado por el filtro JWT.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Si el usuario está autenticado, se extrae el token JWT y se inyecta en
        // la cabecera de la petición Feign.
        if (authentication != null && authentication.getCredentials() instanceof String token) {
            template.header(AUTHORIZATION_HEADER, BEARER_PREFIX + token);
            log.debug("Token JWT propagado en llamada Feign a: {}", template.url());
        }
    }
}
