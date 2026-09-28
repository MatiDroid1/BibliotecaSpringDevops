package cl.triskeledu.recursos.dto;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO de respuesta para RecursoFisico con soporte HATEOAS.
 *
 * isbn y titulo provienen de LibroProyeccion (son null cuando
 * el recurso no es de tipo "Libro").
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RecursoFisicoResponse extends RepresentationModel<RecursoFisicoResponse> {

    private Long id;
    private String sku;
    private String tipoRecurso;
    private String isbn;      // null si no es un Libro
    private String titulo;    // null si no es un Libro
    private String estadoFisico;
    private Boolean disponible;
}
