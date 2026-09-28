package cl.triskeledu.catalogo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "categorias",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_categorias_nombre", columnNames = "nombre")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;

    // Define el lado inverso de la relación ManyToMany, 'mappedBy' indica que esta entidad 
    // no es el dueño de la relación. Esto significa que no se gestionarán las operaciones 
    // de persistencia (INSERT/DELETE) en la tabla de unión desde esta entidad, sino que se 
    // delegará al lado propietario (Libro). En un sentido lógico, una categoría puede existir 
    // sin libros, mientras que un libro "posee" categorías, por lo que tiene sentido 
    // que Libro sea el dueño de la relación.
    @Builder.Default
    @ManyToMany(mappedBy = "categorias") 
    private List<Libro> libros = new ArrayList<>();
}