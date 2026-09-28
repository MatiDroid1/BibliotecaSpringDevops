package cl.triskeledu.recursos.dto;

import lombok.Data;

@Data
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
    private Boolean activo;
}
