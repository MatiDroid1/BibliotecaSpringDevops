package cl.triskeledu.catalogo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "libros",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_libros_isbn", columnNames = "isbn")
    },
    indexes = {
        @Index(name = "idx_libros_isbn", columnList = "isbn"),
        @Index(name = "idx_libros_titulo", columnList = "titulo")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "isbn", nullable = false, length = 20, unique = true)
    private String isbn;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "editorial", nullable = false, length = 100)
    private String editorial;

    @Column(name = "anio_publicacion", nullable = false)
    private Integer anioPublicacion;

    @Column(name = "autor", nullable = false, length = 150)
    private String autor;

    // Define a Libro como el "Dueño de la relación" (Owner), esto se debe a que en un sentido lógico, 
    // un libro "posee" categorías, mientras que una categoría puede existir sin libros.
    // @JoinTable crea automáticamente la tabla física 'libro_categoria' en la BD 
    // sin necesidad de una clase Java intermedia para representar la relación ManyToMany.
    // Al no tener 'mappedBy', JPA usará esta configuración para ejecutar los 
    // INSERT/DELETE en la tabla de unión (libro_categoria).
    @Builder.Default
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "libro_categoria", 
        joinColumns = @JoinColumn(name = "libro_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categorias = new ArrayList<>();

}