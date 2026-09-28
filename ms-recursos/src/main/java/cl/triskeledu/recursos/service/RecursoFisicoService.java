package cl.triskeledu.recursos.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.triskeledu.recursos.dto.RecursoFisicoRequest;
import cl.triskeledu.recursos.dto.RecursoFisicoResponse;
import cl.triskeledu.common.exception.*;
import cl.triskeledu.recursos.mapper.RecursoFisicoMapper;
import cl.triskeledu.recursos.model.LibroProyeccion;
import cl.triskeledu.recursos.model.RecursoFisico;
import cl.triskeledu.recursos.repository.LibroProyeccionRepository;
import cl.triskeledu.recursos.repository.RecursoFisicoRepository;
import cl.triskeledu.recursos.client.CatalogoClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Servicio encargado de aplicar las reglas de negocio de recursos físicos:
 * - Gestiona operaciones CRUD sobre recursos físicos del inventario.
 * - Valida que el SKU sea único (clave de negocio del dominio de recursos).
 * - Valida que si el tipo de recurso es 'Libro', el ISBN debe existir en la proyección local.
 * - Permite filtrar recursos por disponibilidad y tipo.
 * - Lanza excepciones personalizadas del módulo common para casos de error específicos.
 * - Utiliza un mapper para convertir entre entidades y DTOs, manteniendo el código limpio y separado.
 */
@Service
@RequiredArgsConstructor
public class RecursoFisicoService {

    private final RecursoFisicoRepository recursoRepository;
    private final LibroProyeccionRepository libroProyeccionRepository;
    private final RecursoFisicoMapper recursoMapper;
    private final CatalogoClient catalogoClient;

    public List<RecursoFisicoResponse> findAll() {
        return recursoMapper.toResponseList(recursoRepository.findAll());
    }

    public RecursoFisicoResponse findById(long id) {
        return recursoMapper.toResponse(getRecursoById(id));
    }

    public RecursoFisicoResponse findBySku(String sku) {
        return recursoMapper.toResponse(getRecursoBySku(sku));
    }

    public List<RecursoFisicoResponse> findByDisponible(Boolean disponible) {
        return recursoMapper.toResponseList(recursoRepository.findByDisponible(disponible));
    }

    public List<RecursoFisicoResponse> findByTipoRecurso(String tipoRecurso) {
        return recursoMapper.toResponseList(recursoRepository.findByTipoRecurso(tipoRecurso));
    }

    @Transactional
    public RecursoFisicoResponse create(RecursoFisicoRequest request) {

        validateSkuUnico(request.getSku());

        // Valida contra el microservicio dueño del dato.
        // Si el ISBN no existe en catálogo, Feign lanzará error y no se guardará el recurso.
        if (!catalogoClient.existsByIsbn(request.getIsbn())) {
            throw new EntityNotFoundException("Libros en Catálogo", "ISBN", request.getIsbn());
        }

        // Usa la proyección local sincronizada por Feign desde ms-catalogo.
        LibroProyeccion libroProyeccion = libroProyeccionRepository
                .findByIsbn(request.getIsbn())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Libros",
                        "ISBN",
                        request.getIsbn()
                ));

        RecursoFisico recurso = new RecursoFisico();
        recursoMapper.updateEntity(request, recurso);

        recurso.setTipoRecurso("Libro");
        recurso.setLibro(libroProyeccion);
        recurso.setDisponible(true);

        return recursoMapper.toResponse(recursoRepository.save(recurso));
    }

    @Transactional
    public RecursoFisicoResponse update(long id, RecursoFisicoRequest request) {
        RecursoFisico recurso = getRecursoById(id);
        // Solo valida unicidad si cambió el SKU
        if (!recurso.getSku().equalsIgnoreCase(request.getSku())) {
            validateSkuUnico(request.getSku());
        }
        recursoMapper.updateEntity(request, recurso);
        recurso.setLibro(resolverLibroProyeccion(request));
        return recursoMapper.toResponse(recursoRepository.save(recurso));
    }

    @Transactional
    public void deleteById(long id) {
        RecursoFisico recurso = getRecursoById(id);
        List<String> tablasAsociadas = new java.util.ArrayList<>();
        if (!recurso.getHistorial().isEmpty())      tablasAsociadas.add("Historial de eventos");
        if (!recurso.getMantenimientos().isEmpty()) tablasAsociadas.add("Mantenimientos");
        if (!tablasAsociadas.isEmpty())
            throw new ReferentialIntegrityException("Recursos físicos", id, String.join(", ", tablasAsociadas));
        recursoRepository.delete(recurso);
    }

    // ─── Métodos privados auxiliares ─────────────────────────────────────────

    private RecursoFisico getRecursoById(long id) {
        return recursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recursos físicos", "ID", id));
    }

    private RecursoFisico getRecursoBySku(String sku) {
        return recursoRepository.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("Recursos físicos", "SKU", sku));
    }

    private void validateSkuUnico(String sku) {
        recursoRepository.findBySku(sku).ifPresent(r -> {
            throw new DuplicateResourceException("Un Recurso físico", "SKU", sku, r.getTipoRecurso());
        });
    }

    /**
     * Si el tipo de recurso es 'Libro', busca y devuelve la proyección del libro por ISBN.
     * Si el ISBN no existe en la tabla libros_proyeccion, lanza EntityNotFoundException.
     * Para otros tipos de recurso devuelve null (sin ISBN).
     */
    private LibroProyeccion resolverLibroProyeccion(RecursoFisicoRequest request) {
        if ("Libro".equals(request.getTipoRecurso())) {
            String isbn = request.getIsbn();
            if (isbn == null || isbn.isBlank()) {
                throw new IllegalArgumentException("El ISBN es obligatorio cuando el tipo de recurso es 'Libro'");
            }
            return libroProyeccionRepository.findById(isbn)
                    .orElseThrow(() -> new EntityNotFoundException("Libros proyección", "ISBN", isbn));
        }
        return null;
    }

    public boolean existsByIsbn(String isbn) {
        return recursoRepository.existsByLibroIsbn(isbn);
    }
}
