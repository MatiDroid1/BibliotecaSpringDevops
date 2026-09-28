package cl.triskeledu.recursos.repository;

import cl.triskeledu.recursos.model.LibroProyeccion;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibroProyeccionRepository extends JpaRepository<LibroProyeccion, String> {

    Optional<LibroProyeccion> findByIsbn(String isbn);
    void deleteByIsbn(String isbn);

}
