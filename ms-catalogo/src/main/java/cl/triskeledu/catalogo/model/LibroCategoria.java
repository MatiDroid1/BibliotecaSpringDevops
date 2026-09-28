package cl.triskeledu.catalogo.model;

import jakarta.persistence.*;
import lombok.*;


// La clase LibroCategoria representa la relación entre libros y categorías en la base de datos.
// Pero en este diseño, no es estrictamente necesaria como entidad separada, ya que la relación 
// ManyToMany entre Libro y Categoria se maneja directamente a través de una tabla de unión.
// Esta clase se mantiene para ilustrar cómo se podría modelar la relación si se necesitara 
// almacenar información adicional sobre la relación entre un libro y una categoría (por 
// ejemplo, fecha de asignación, usuario que asignó, etc.). Si no se requiere información 
// adicional, esta clase podría ser eliminada y la relación ManyToMany se manejaría directamente 
// entre Libro y Categoria.

@Entity
@Table(
    name = "libro_categoria",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_libro_categoria_libro_categoria",
            columnNames = {"libro_id", "categoria_id"}
        )
    },
    indexes = {
        @Index(name = "idx_libro_categoria_libro", columnList = "libro_id"),
        @Index(name = "idx_libro_categoria_categoria", columnList = "categoria_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibroCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}