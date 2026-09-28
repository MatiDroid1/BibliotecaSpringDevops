package cl.triskeledu.usuarios.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta del login exitoso.
 * Contiene el token JWT y datos básicos del usuario autenticado.
 */
@Data
@Builder
public class LoginResponse {

    /** Token JWT generado tras la autenticación exitosa */
    private String token;

    /** Tipo de token (siempre "Bearer") */
    @Builder.Default
    private String tokenType = "Bearer";

    /** Email del usuario autenticado */
    private String email;

    /** Nombre completo del usuario */
    private String nombre;

    /** Rol del usuario (Administrador, Bibliotecario, Cliente) */
    private String rol;

    /** Tiempo de expiración del token en milisegundos */
    private long expiresIn;
}
