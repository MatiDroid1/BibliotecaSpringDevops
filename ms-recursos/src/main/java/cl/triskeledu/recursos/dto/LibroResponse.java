package cl.triskeledu.recursos.dto;

import lombok.Data;

@Data
public class LibroResponse {

    private Long id;
    private String isbn;
    private String titulo;
    private String editorial;
    private Integer anioPublicacion;
    private String autor;
}
