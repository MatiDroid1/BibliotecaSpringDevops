package cl.triskeledu.recursos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LibroProyeccionRequest {

    @NotBlank(message = "El ISBN es obligatorio")
    @Size(max = 20, message = "El ISBN no puede superar los 20 caracteres")
    private String isbn;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede superar los 255 caracteres")
    private String titulo;
}
