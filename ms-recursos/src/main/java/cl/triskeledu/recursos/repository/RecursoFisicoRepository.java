package cl.triskeledu.recursos.repository;

import cl.triskeledu.recursos.model.RecursoFisico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecursoFisicoRepository extends JpaRepository<RecursoFisico, Long> {

    Optional<RecursoFisico> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsByLibroIsbn(String isbn);

    List<RecursoFisico> findByDisponible(Boolean disponible);

    List<RecursoFisico> findByTipoRecurso(String tipoRecurso);
}
