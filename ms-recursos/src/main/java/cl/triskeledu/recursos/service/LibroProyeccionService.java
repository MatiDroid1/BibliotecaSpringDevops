package cl.triskeledu.recursos.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.triskeledu.common.exception.ReferentialIntegrityException;
import cl.triskeledu.recursos.model.LibroProyeccion;
import cl.triskeledu.recursos.repository.LibroProyeccionRepository;
import cl.triskeledu.recursos.repository.RecursoFisicoRepository;
import cl.triskeledu.recursos.dto.LibroProyeccionResponse;
import cl.triskeledu.recursos.mapper.LibroProyeccionMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de la proyección local de libros.
 * ms-catalogo mantiene estos datos sincronizados mediante llamadas Feign.
 */
@Service
@RequiredArgsConstructor
public class LibroProyeccionService {

    private final LibroProyeccionRepository libroProyeccionRepository;
    private final RecursoFisicoRepository recursoFisicoRepository;
    private final LibroProyeccionMapper libroProyeccionMapper;

    @Transactional
    public List<LibroProyeccionResponse> findAll() {
        return libroProyeccionMapper.toResponseList(libroProyeccionRepository.findAll());
    }

    @Transactional
    public void save(String isbn, String titulo) {
        LibroProyeccion libroProyeccion = libroProyeccionRepository.findByIsbn(isbn)
                .orElseGet(LibroProyeccion::new);
        libroProyeccion.setIsbn(isbn);
        libroProyeccion.setTitulo(titulo);
        libroProyeccionRepository.save(libroProyeccion);
    }

    @Transactional
    public void deleteByIsbn(String isbn) {
        libroProyeccionRepository.findByIsbn(isbn).ifPresent(libroProyeccion -> {
            if (recursoFisicoRepository.existsByLibroIsbn(isbn)) {
                throw new ReferentialIntegrityException("Libro Proyección", isbn, "Recursos Físicos");
            }
            libroProyeccionRepository.delete(libroProyeccion);
        });
    }
}
