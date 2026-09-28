package cl.triskeledu.usuarios.dto;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO de respuesta para Usuario con soporte HATEOAS.
 *
 * Al extender RepresentationModel, Jackson serializa automáticamente
 * el campo "_links" cuando el controlador agrega links con .add(Link...).
 *
 * La contraseña nunca se expone aquí (omitida intencionalmente).
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UsuarioResponse extends RepresentationModel<UsuarioResponse> {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
    private Boolean activo;
}
