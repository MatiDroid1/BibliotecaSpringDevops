package cl.triskeledu.recursos.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "libros_proyeccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibroProyeccion {

    @Id
    @Column(name = "isbn", nullable = false, length = 20)
    private String isbn;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;
}
