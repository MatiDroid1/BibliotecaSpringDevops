package cl.triskeledu.catalogo.service;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import cl.triskeledu.catalogo.client.RecursoClient;
import cl.triskeledu.catalogo.dto.LibroProyeccionSyncRequest;
import cl.triskeledu.catalogo.dto.LibroRequest;
import cl.triskeledu.catalogo.dto.LibroResponse;
import cl.triskeledu.common.exception.*;
import cl.triskeledu.catalogo.mapper.LibroMapper;
import cl.triskeledu.catalogo.model.Categoria;
import cl.triskeledu.catalogo.model.Libro;
import cl.triskeledu.catalogo.repository.CategoriaRepository;
import cl.triskeledu.catalogo.repository.LibroRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Servicio encargado de aplicar las reglas de negocio de libros:
 * - Gestiona operaciones CRUD, validaciones de negocio y reglas de integridad.
 * - Valida que el ISBN sea único.
 * - Maneja las asociaciones con categorías y lanza excepciones personalizadas para casos de error específicos.
 * - Utiliza un mapper para convertir entre entidades y DTOs, manteniendo el código limpio y separado.
 * - Garantiza que las operaciones de actualización y eliminación respeten las reglas de integridad referencial.
 * - Sincroniza la proyección de libros en ms-recursos mediante Feign.
 * - No deja eliminar un libro si tiene categorías asociadas, lanzando una excepción de integridad referencial con detalles claros para el cliente.
  */
@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final LibroMapper libroMapper;
    private final RecursoClient recursoClient;

    public List<LibroResponse> findAll() {
        return libroMapper.toResponseList(libroRepository.findAll());
    }

    public LibroResponse findById(long id) {
        return libroMapper.toResponse(getLibroById(id));
    }

    public LibroResponse findByIsbn(String isbn) {
        return libroMapper.toResponse(getLibroByIsbn(isbn));
    }

    @Transactional
    public LibroResponse create(LibroRequest request) {
        validateIsbnUnico(request.getIsbn());
        Libro libro = new Libro();  
        libroMapper.updateEntity(request, libro);
        libroRepository.save(libro);
        sincronizarProyeccion(libro);
        return libroMapper.toResponse(libro);
    }

     private void validateIsbnUnico(String isbn) {
        libroRepository.findByIsbn(isbn).ifPresent(l -> { throw new DuplicateResourceException("Un Libro", "ISBN", isbn, l.getTitulo()); });
    }

    private Libro getLibroById(long id) {
        return libroRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Libros", "ID", id));  
    }

    private Libro getLibroByIsbn(String isbn) {
        return libroRepository.findByIsbn(isbn).orElseThrow(() -> new EntityNotFoundException("Libros", "ISBN", isbn));  
    }

    private Categoria getCategoriaById(long id) {
        return categoriaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Categorías", "ID", id));
    }

    public boolean existsByIsbn(String isbn) {
        return libroRepository.existsByIsbn(isbn);
    }

    @Transactional
    public LibroResponse update(Long id, LibroRequest request) {
        Libro libro = getLibroById(id);
        if (!request.getIsbn().equalsIgnoreCase(libro.getIsbn())) {
            validateIsbnUnico(request.getIsbn());
        }
        libroMapper.updateEntity(request, libro);
        libroRepository.save(libro);
        sincronizarProyeccion(libro);
        return libroMapper.toResponse(libro);
    }

    @Transactional
    public void deleteById(Long id) {
        Libro libro = getLibroById(id);
        List<String> tablasAsociadas = new ArrayList<>();
        if (!libro.getCategorias().isEmpty()) tablasAsociadas.add("Categorías");
        if (recursoClient.existsByIsbn(libro.getIsbn())) tablasAsociadas.add("Recursos Físicos");
        if (!tablasAsociadas.isEmpty()) throw new ReferentialIntegrityException("Libros", id, String.join(", ", tablasAsociadas));
        libroRepository.delete(libro);
        recursoClient.deleteLibroProyeccion(libro.getIsbn());
    }

    @Transactional
    public void addCategoriaALibro(Long libroId, Long categoriaId) {
        Libro libro = getLibroById(libroId);
        Categoria categoria = getCategoriaById(categoriaId);
        if (!libro.getCategorias().contains(categoria)) {
            libro.getCategorias().add(categoria);
            libroRepository.save(libro); 
        }
    }

    private void sincronizarProyeccion(Libro libro) {
        recursoClient.upsertLibroProyeccion(
                libro.getIsbn(),
                new LibroProyeccionSyncRequest(libro.getIsbn(), libro.getTitulo())
        );
    }

}
