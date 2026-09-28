package cl.triskeledu.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 150, message = "El apellido no puede superar los 150 caracteres")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener formato válido")
    @Size(max = 150, message = "El email no puede superar los 150 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 3, max = 50, message = "La contraseña debe tener entre 3 y 50 caracteres")
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(
        regexp = "Administrador|Bibliotecario|Cliente",
        message = "El rol debe ser: Administrador, Bibliotecario o Cliente"
    )
    private String rol;
}
