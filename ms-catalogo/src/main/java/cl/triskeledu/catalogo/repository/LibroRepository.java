package cl.triskeledu.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.triskeledu.catalogo.model.Libro;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    
    Optional<Libro> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);
}