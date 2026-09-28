package cl.triskeledu.catalogo.dto;

import java.util.List;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO de respuesta para Libro con soporte HATEOAS.
 *
 * Extender RepresentationModel agrega el campo "_links" a la serialización JSON.
 * Spring HATEOAS lo puebla automáticamente cuando el controlador invoca add(Link...).
 *
 * @EqualsAndHashCode(callSuper = false) evita conflictos con los equals/hashCode
 * que genera Lombok al combinarse con la herencia de RepresentationModel.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LibroResponse extends RepresentationModel<LibroResponse> {

    private Long id;
    private String isbn;
    private String titulo;
    private String editorial;
    private Integer anioPublicacion;
    private String autor;

    //[HATEOAS] Lista de categorías asociadas al libro
    private List<CategoriaResponse> categorias; 

}
