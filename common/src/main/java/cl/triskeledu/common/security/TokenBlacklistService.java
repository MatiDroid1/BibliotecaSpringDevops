package cl.triskeledu.common.security;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de blacklist de tokens JWT en memoria.
 *
 * Cuando un usuario hace logout, su token se agrega a esta lista negra.
 * El filtro JWT verifica contra esta lista antes de aceptar un token.
 *
 * Los tokens expirados se limpian automáticamente cada 10 minutos
 * para evitar crecimiento descontrolado de la memoria.
 *
 * NOTA: En un entorno productivo con múltiples instancias, se debería
 * usar Redis u otro almacén compartido en lugar de memoria local.
 */
@Slf4j
@Service
@EnableScheduling
public class TokenBlacklistService {

    // Mapa: token → fecha de expiración del token
    private final Map<String, Date> blacklist = new ConcurrentHashMap<>();

    /**
     * Agrega un token a la lista negra.
     *
     * @param token          el token JWT a invalidar
     * @param expirationDate la fecha de expiración original del token
     */
    public void addToBlacklist(String token, Date expirationDate) {
        blacklist.put(token, expirationDate);
        log.info("Token agregado a la blacklist. Tokens en blacklist: {}", blacklist.size());
    }

    /**
     * Verifica si un token está en la lista negra.
     *
     * @param token el token JWT a verificar
     * @return true si el token fue revocado (está en la blacklist)
     */
    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    /**
     * Limpieza automática de tokens expirados cada 10 minutos.
     * Los tokens que ya pasaron su fecha de expiración natural
     * no necesitan seguir en la blacklist porque ya son inválidos por sí mismos.
     */
    @Scheduled(fixedRate = 600000) // cada 10 minutos
    public void limpiarTokensExpirados() {
        Date ahora = new Date();
        int antes = blacklist.size();
        blacklist.entrySet().removeIf(entry -> entry.getValue().before(ahora));
        int eliminados = antes - blacklist.size();
        if (eliminados > 0) {
            log.info("Blacklist limpiada: {} tokens expirados eliminados. Restantes: {}",
                    eliminados, blacklist.size());
        }
    }
}
