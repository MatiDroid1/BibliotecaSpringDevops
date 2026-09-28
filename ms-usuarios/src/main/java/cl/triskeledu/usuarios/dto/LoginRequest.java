package cl.triskeledu.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para la solicitud de inicio de sesión (login).
 * Solo requiere email y contraseña.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
