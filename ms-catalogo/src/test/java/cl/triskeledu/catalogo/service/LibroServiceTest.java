package cl.triskeledu.catalogo.service;

import cl.triskeledu.catalogo.client.RecursoClient;
import cl.triskeledu.catalogo.dto.CategoriaResponse;
import cl.triskeledu.catalogo.dto.LibroProyeccionSyncRequest;
import cl.triskeledu.catalogo.dto.LibroRequest;
import cl.triskeledu.catalogo.dto.LibroResponse;
import cl.triskeledu.catalogo.mapper.LibroMapper;
import cl.triskeledu.catalogo.model.Categoria;
import cl.triskeledu.catalogo.model.Libro;
import cl.triskeledu.catalogo.repository.CategoriaRepository;
import cl.triskeledu.catalogo.repository.LibroRepository;
import cl.triskeledu.common.exception.DuplicateResourceException;
import cl.triskeledu.common.exception.EntityNotFoundException;
import cl.triskeledu.common.exception.ReferentialIntegrityException;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para LibroService.
 *
 * Mockito permite aislar LibroService de sus dependencias reales:
 * repositorios, mapper y cliente externo.
 */
@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    // @Mock crea una dependencia simulada, evitando usar la base de datos real.
    @Mock
    private LibroRepository libroRepository;

    // Mock del repositorio de categorías.
    @Mock
    private CategoriaRepository categoriaRepository;

    // Mock del mapper para controlar la conversión entre entidad y DTO.
    @Mock
    private LibroMapper libroMapper;

    // Mock del cliente externo para evitar llamadas reales a otro servicio.
    @Mock
    private RecursoClient recursoClient;

    // @InjectMocks crea LibroService e inyecta automáticamente los mocks anteriores.
    @InjectMocks
    private LibroService libroService;

    // DataFaker genera datos aleatorios para los objetos de prueba.
    private final Faker faker = new Faker();

    /**
     * Configuración común antes de cada prueba.
     *
     * Aquí se define cómo deben comportarse algunos mocks usados por varias pruebas.
     */
    @BeforeEach
    void setUp() {

        /*
         * lenient():
         * Evita que Mockito falle si alguna prueba no usa esta configuración.
         *
         * when(...):
         * Define qué método del mock se está simulando.
         *
         * any(Libro.class):
         * Acepta cualquier instancia de Libro como argumento.
         *
         * thenAnswer(...):
         * Permite crear una respuesta dinámica usando el argumento recibido.
         */
        lenient().when(libroMapper.toResponse(any(Libro.class))).thenAnswer(invocation -> {
            Libro libro = invocation.getArgument(0);

            if (libro == null) return null;

            LibroResponse response = new LibroResponse();
            response.setId(libro.getId());
            response.setIsbn(libro.getIsbn());
            response.setTitulo(libro.getTitulo());
            response.setEditorial(libro.getEditorial());
            response.setAnioPublicacion(libro.getAnioPublicacion());
            response.setAutor(libro.getAutor());

            if (libro.getCategorias() != null) {
                List<CategoriaResponse> catResponses = libro.getCategorias()
                        .stream()
                        .map(c -> {
                            CategoriaResponse cr = new CategoriaResponse();
                            cr.setId(c.getId());
                            cr.setNombre(c.getNombre());
                            return cr;
                        })
                        .toList();

                response.setCategorias(catResponses);
            }

            return response;
        });

        /*
         * Simula la conversión de una lista de Libro a una lista de LibroResponse.
         */
        lenient().when(libroMapper.toResponseList(anyList())).thenAnswer(invocation -> {
            List<Libro> libros = invocation.getArgument(0);

            if (libros == null) return null;

            return libros.stream()
                    .map(libroMapper::toResponse)
                    .toList();
        });

        /*
         * doAnswer(...):
         * Se usa aquí porque updateEntity(...) es un método void.
         * Permite simular que el mapper copia los datos del request al libro.
         */
        lenient().doAnswer(invocation -> {
            LibroRequest request = invocation.getArgument(0);
            Libro libro = invocation.getArgument(1);

            if (request != null && libro != null) {
                libro.setIsbn(request.getIsbn());
                libro.setTitulo(request.getTitulo());
                libro.setEditorial(request.getEditorial());
                libro.setAnioPublicacion(request.getAnioPublicacion());
                libro.setAutor(request.getAutor());
            }

            return null;

        }).when(libroMapper).updateEntity(any(LibroRequest.class), any(Libro.class));
    }

    /**
     * Genera un ISBN ficticio para las pruebas.
     */
    private String generateFakeIsbn() {
        return "978-"
                + faker.number().digit()
                + "-"
                + faker.number().digits(4)
                + "-"
                + faker.number().digits(4)
                + "-"
                + faker.number().digit();
    }

    /**
     * Crea una entidad Libro con datos aleatorios.
     */
    private Libro crearLibroSimulado(Long id) {
        Libro libro = new Libro();

        libro.setId(id);
        libro.setIsbn(generateFakeIsbn());
        libro.setTitulo(faker.book().title());
        libro.setAutor(faker.book().author());
        libro.setEditorial(faker.book().publisher());
        libro.setAnioPublicacion(faker.number().numberBetween(1450, 2026));
        libro.setCategorias(new ArrayList<>());

        return libro;
    }

    /**
     * Crea un LibroRequest con datos aleatorios.
     */
    private LibroRequest crearLibroRequestSimulado() {
        LibroRequest request = new LibroRequest();

        request.setIsbn(generateFakeIsbn());
        request.setTitulo(faker.book().title());
        request.setAutor(faker.book().author());
        request.setEditorial(faker.book().publisher());
        request.setAnioPublicacion(faker.number().numberBetween(1450, 2026));

        return request;
    }

    /**
     * Prueba findAll() cuando existen libros registrados.
     */
    @Test
    void findAll_DeberiaRetornarListaDeLibros_CuandoExistenRegistros() {
        Libro libro1 = crearLibroSimulado(1L);
        Libro libro2 = crearLibroSimulado(2L);
        Libro libro3 = crearLibroSimulado(3L);

        // when(...).thenReturn(...): simula lo que devuelve el repositorio.
        when(libroRepository.findAll()).thenReturn(List.of(libro1, libro2, libro3));

        List<LibroResponse> resultado = libroService.findAll();

        // assertNotNull: verifica que el resultado no sea null.
        assertNotNull(resultado, "La lista retornada no debe ser nula");

        // assertEquals: compara el valor esperado con el valor obtenido.
        assertEquals(3, resultado.size(), "La lista debe contener exactamente 3 elementos");

        LibroResponse primerRegistro = resultado.get(0);

        assertEquals(libro1.getId(), primerRegistro.getId(), "El ID debe coincidir");
        assertEquals(libro1.getIsbn(), primerRegistro.getIsbn(), "El ISBN debe coincidir");
        assertEquals(libro1.getTitulo(), primerRegistro.getTitulo(), "El título debe coincidir");
        assertEquals(libro1.getAutor(), primerRegistro.getAutor(), "El autor debe coincidir");
        assertEquals(libro1.getEditorial(), primerRegistro.getEditorial(), "La editorial debe coincidir");
        assertEquals(libro1.getAnioPublicacion(), primerRegistro.getAnioPublicacion(), "El año de publicación debe coincidir");

        // verify: comprueba que el método del mock fue llamado.
        verify(libroRepository).findAll();
    }

    /**
     * Prueba findById() cuando el ID existe.
     */
    @Test
    void findById_DeberiaRetornarLibro_CuandoIdExiste() {
        Long id = 10L;
        Libro libro = crearLibroSimulado(id);

        // Simula que el repositorio encuentra el libro.
        when(libroRepository.findById(id)).thenReturn(Optional.of(libro));

        LibroResponse resultado = libroService.findById(id);

        // Verifica que el servicio retorne un objeto válido.
        assertNotNull(resultado);

        // Compara los datos esperados con los datos retornados.
        assertEquals(libro.getId(), resultado.getId());
        assertEquals(libro.getIsbn(), resultado.getIsbn());
        assertEquals(libro.getTitulo(), resultado.getTitulo());

        // Verifica que se haya buscado por ID.
        verify(libroRepository).findById(id);
    }

    /**
     * Prueba findById() cuando el ID no existe.
     */
    @Test
    void findById_DeberiaLanzarEntityNotFoundException_CuandoIdNoExiste() {
        Long id = 999L;

        // Simula que el repositorio no encuentra el libro.
        when(libroRepository.findById(id)).thenReturn(Optional.empty());

        // Verifica que se lance la excepción esperada.
        assertThrows(EntityNotFoundException.class, () -> libroService.findById(id));

        // Verifica que se haya consultado el repositorio.
        verify(libroRepository).findById(id);
    }

    /**
     * Prueba findByIsbn() cuando el ISBN existe.
     */
    @Test
    void findByIsbn_DeberiaRetornarLibro_CuandoIsbnExiste() {
        String isbn = generateFakeIsbn();

        Libro libro = crearLibroSimulado(1L);
        libro.setIsbn(isbn);

        // Simula que el repositorio encuentra el libro por ISBN.
        when(libroRepository.findByIsbn(isbn)).thenReturn(Optional.of(libro));

        LibroResponse resultado = libroService.findByIsbn(isbn);

        // Verifica que el resultado no sea null.
        assertNotNull(resultado);

        // Verifica que los datos retornados sean los esperados.
        assertEquals(isbn, resultado.getIsbn());
        assertEquals(libro.getTitulo(), resultado.getTitulo());

        // Verifica que se haya llamado al método correcto del repositorio.
        verify(libroRepository).findByIsbn(isbn);
    }

    /**
     * Prueba findByIsbn() cuando el ISBN no existe.
     */
    @Test
    void findByIsbn_DeberiaLanzarEntityNotFoundException_CuandoIsbnNoExiste() {
        String isbn = generateFakeIsbn();

        // Simula que no existe un libro con ese ISBN.
        when(libroRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        // Verifica que se lance la excepción esperada.
        assertThrows(EntityNotFoundException.class, () -> libroService.findByIsbn(isbn));

        // Verifica que se haya buscado por ISBN.
        verify(libroRepository).findByIsbn(isbn);
    }

    /**
     * Prueba existsByIsbn() cuando el ISBN existe.
     */
    @Test
    void existsByIsbn_DeberiaRetornarTrue_CuandoElIsbnExiste() {
        String isbn = generateFakeIsbn();

        // Simula que el ISBN existe.
        when(libroRepository.existsByIsbn(isbn)).thenReturn(true);

        boolean resultado = libroService.existsByIsbn(isbn);

        // Verifica que el resultado booleano sea true.
        assertTrue(resultado);

        // Verifica que se haya consultado la existencia del ISBN.
        verify(libroRepository).existsByIsbn(isbn);
    }

    /**
     * Prueba create() cuando el request es válido y el ISBN no está duplicado.
     */
    @Test
    void create_DeberiaCrearLibroYSincronizarProyeccion_CuandoRequestEsValidoYIsbnEsUnico() {
        LibroRequest request = crearLibroRequestSimulado();

        // Simula que no existe otro libro con el mismo ISBN.
        when(libroRepository.findByIsbn(request.getIsbn())).thenReturn(Optional.empty());

        /*
         * thenAnswer permite modificar y retornar el argumento recibido.
         * Aquí se simula que la base de datos asigna el ID al guardar.
         */
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> {
            Libro l = invocation.getArgument(0);
            l.setId(100L);
            return l;
        });

        LibroResponse resultado = libroService.create(request);

        // Verifica que el servicio retorne una respuesta válida.
        assertNotNull(resultado);

        // Verifica que los datos retornados coincidan con lo esperado.
        assertEquals(100L, resultado.getId());
        assertEquals(request.getIsbn(), resultado.getIsbn());
        assertEquals(request.getTitulo(), resultado.getTitulo());

        // Verifica las llamadas esperadas a los mocks.
        verify(libroRepository).findByIsbn(request.getIsbn());
        verify(libroRepository).save(any(Libro.class));

        // Verifica que se haya sincronizado la proyección en ms-recursos.
        verify(recursoClient).upsertLibroProyeccion(eq(request.getIsbn()), any(LibroProyeccionSyncRequest.class));
    }

    /**
     * Prueba create() cuando el ISBN ya existe.
     */
    @Test
    void create_DeberiaLanzarDuplicateResourceException_CuandoIsbnYaExiste() {
        LibroRequest request = crearLibroRequestSimulado();

        Libro libroExistente = crearLibroSimulado(2L);
        libroExistente.setIsbn(request.getIsbn());

        // Simula que ya existe un libro con el mismo ISBN.
        when(libroRepository.findByIsbn(request.getIsbn())).thenReturn(Optional.of(libroExistente));

        // Verifica que se lance excepción por duplicidad.
        assertThrows(DuplicateResourceException.class, () -> libroService.create(request));

        // Verifica que se haya validado la existencia del ISBN.
        verify(libroRepository).findByIsbn(request.getIsbn());

        // never(): verifica que estos métodos no hayan sido llamados.
        verify(libroRepository, never()).save(any(Libro.class));
        verify(recursoClient, never()).upsertLibroProyeccion(anyString(), any());
    }

    /**
     * Prueba update() cuando el ISBN no cambia.
     */
    @Test
    void update_DeberiaActualizarLibroYSincronizarProyeccion_CuandoElIsbnNoCambia() {
        Long id = 5L;

        Libro libroExistente = crearLibroSimulado(id);
        LibroRequest request = crearLibroRequestSimulado();

        request.setIsbn(libroExistente.getIsbn());

        // Simula que el libro existe.
        when(libroRepository.findById(id)).thenReturn(Optional.of(libroExistente));

        // Simula que save() retorna el mismo libro actualizado.
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LibroResponse resultado = libroService.update(id, request);

        // Verifica que la respuesta no sea null.
        assertNotNull(resultado);

        // Verifica que los datos hayan sido actualizados.
        assertEquals(request.getTitulo(), resultado.getTitulo());
        assertEquals(request.getIsbn(), resultado.getIsbn());

        // Verifica que se buscó el libro por ID.
        verify(libroRepository).findById(id);

        // No debe buscar duplicado porque el ISBN no cambió.
        verify(libroRepository, never()).findByIsbn(anyString());

        // Verifica que se guardó y se sincronizó la proyección.
        verify(libroRepository).save(any(Libro.class));
        verify(recursoClient).upsertLibroProyeccion(eq(request.getIsbn()), any(LibroProyeccionSyncRequest.class));
    }

    /**
     * Prueba update() cuando el ISBN cambia y no está duplicado.
     */
    @Test
    void update_DeberiaActualizarLibroYSincronizarProyeccion_CuandoElIsbnCambiaYNoEstaDuplicado() {
        Long id = 5L;

        Libro libroExistente = crearLibroSimulado(id);
        LibroRequest request = crearLibroRequestSimulado();

        // Simula que el libro existe.
        when(libroRepository.findById(id)).thenReturn(Optional.of(libroExistente));

        // Simula que el nuevo ISBN no está duplicado.
        when(libroRepository.findByIsbn(request.getIsbn())).thenReturn(Optional.empty());

        // Simula que save() retorna la entidad actualizada.
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LibroResponse resultado = libroService.update(id, request);

        // Verifica que la respuesta sea válida.
        assertNotNull(resultado);

        // Verifica que el ISBN fue actualizado.
        assertEquals(request.getIsbn(), resultado.getIsbn());

        // Verifica que se buscó el libro original.
        verify(libroRepository).findById(id);

        // Verifica que se consultó la unicidad del nuevo ISBN.
        verify(libroRepository).findByIsbn(request.getIsbn());

        // Verifica que se guardó y se sincronizó la proyección.
        verify(libroRepository).save(any(Libro.class));
        verify(recursoClient).upsertLibroProyeccion(eq(request.getIsbn()), any(LibroProyeccionSyncRequest.class));
    }

    /**
     * Prueba update() cuando el nuevo ISBN ya pertenece a otro libro.
     */
    @Test
    void update_DeberiaLanzarDuplicateResourceException_CuandoElIsbnCambiaAUnIsbnDuplicado() {
        Long id = 5L;

        Libro libroExistente = crearLibroSimulado(id);
        LibroRequest request = crearLibroRequestSimulado();

        Libro otroLibroConMismoIsbn = crearLibroSimulado(8L);
        otroLibroConMismoIsbn.setIsbn(request.getIsbn());

        // Simula que el libro a actualizar existe.
        when(libroRepository.findById(id)).thenReturn(Optional.of(libroExistente));

        // Simula que el nuevo ISBN ya pertenece a otro libro.
        when(libroRepository.findByIsbn(request.getIsbn())).thenReturn(Optional.of(otroLibroConMismoIsbn));

        // Verifica que se lance excepción por duplicidad.
        assertThrows(DuplicateResourceException.class, () -> libroService.update(id, request));

        // Verifica las búsquedas realizadas.
        verify(libroRepository).findById(id);
        verify(libroRepository).findByIsbn(request.getIsbn());

        // No debe guardar ni sincronizar proyección si hay duplicidad.
        verify(libroRepository, never()).save(any(Libro.class));
        verify(recursoClient, never()).upsertLibroProyeccion(anyString(), any());
    }

    /**
     * Prueba deleteById() cuando el libro no tiene asociaciones.
     */
    @Test
    void deleteById_DeberiaEliminarLibroYSincronizarProyeccion_CuandoNoTieneAsociaciones() {
        Long id = 15L;

        Libro libro = crearLibroSimulado(id);

        // Simula que el libro existe.
        when(libroRepository.findById(id)).thenReturn(Optional.of(libro));

        // Simula que no existen recursos físicos asociados al ISBN.
        when(recursoClient.existsByIsbn(libro.getIsbn())).thenReturn(false);

        libroService.deleteById(id);

        // Verifica las llamadas realizadas.
        verify(libroRepository).findById(id);
        verify(recursoClient).existsByIsbn(libro.getIsbn());
        verify(libroRepository).delete(libro);

        // Verifica que se haya eliminado la proyección en ms-recursos.
        verify(recursoClient).deleteLibroProyeccion(libro.getIsbn());
    }

    /**
     * Prueba deleteById() cuando el libro tiene categorías asociadas.
     */
    @Test
    void deleteById_DeberiaLanzarReferentialIntegrityException_CuandoTieneCategoriasAsociadas() {
        Long id = 15L;

        Libro libro = crearLibroSimulado(id);

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Novela");

        // Se agrega una categoría para simular una asociación existente.
        libro.getCategorias().add(categoria);

        // Simula que el libro existe.
        when(libroRepository.findById(id)).thenReturn(Optional.of(libro));

        // Simula que no hay recursos físicos, pero sí categoría asociada.
        when(recursoClient.existsByIsbn(libro.getIsbn())).thenReturn(false);

        // Verifica que se lance excepción por integridad referencial.
        assertThrows(ReferentialIntegrityException.class, () -> libroService.deleteById(id));

        // Verifica las consultas realizadas.
        verify(libroRepository).findById(id);
        verify(recursoClient).existsByIsbn(libro.getIsbn());

        // No debe eliminar ni sincronizar proyección si tiene asociaciones.
        verify(libroRepository, never()).delete(any(Libro.class));
        verify(recursoClient, never()).deleteLibroProyeccion(anyString());
    }

    /**
     * Prueba deleteById() cuando existen recursos físicos asociados.
     */
    @Test
    void deleteById_DeberiaLanzarReferentialIntegrityException_CuandoTieneRecursosFisicosAsociados() {
        Long id = 15L;

        Libro libro = crearLibroSimulado(id);

        // Simula que el libro existe.
        when(libroRepository.findById(id)).thenReturn(Optional.of(libro));

        // Simula que existen recursos físicos asociados al ISBN.
        when(recursoClient.existsByIsbn(libro.getIsbn())).thenReturn(true);

        // Verifica que se lance excepción por integridad referencial.
        assertThrows(ReferentialIntegrityException.class, () -> libroService.deleteById(id));

        // Verifica las consultas realizadas.
        verify(libroRepository).findById(id);
        verify(recursoClient).existsByIsbn(libro.getIsbn());

        // No debe eliminar ni sincronizar proyección si existen recursos físicos.
        verify(libroRepository, never()).delete(any(Libro.class));
        verify(recursoClient, never()).deleteLibroProyeccion(anyString());
    }

    /**
     * Prueba addCategoriaALibro() cuando la categoría no estaba asociada.
     */
    @Test
    void addCategoriaALibro_DeberiaAsociarCategoriaYGuardar_CuandoNoEstabaAsociadaPreviamente() {
        Long libroId = 1L;
        Long categoriaId = 2L;

        Libro libro = crearLibroSimulado(libroId);

        Categoria categoria = new Categoria();
        categoria.setId(categoriaId);
        categoria.setNombre("Ficción");

        // Simula que existen el libro y la categoría.
        when(libroRepository.findById(libroId)).thenReturn(Optional.of(libro));
        when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));

        libroService.addCategoriaALibro(libroId, categoriaId);

        // Verifica que la categoría fue agregada al libro.
        assertTrue(libro.getCategorias().contains(categoria), "La categoría debería haberse agregado");

        // Verifica las búsquedas y el guardado.
        verify(libroRepository).findById(libroId);
        verify(categoriaRepository).findById(categoriaId);
        verify(libroRepository).save(libro);
    }

    /**
     * Prueba addCategoriaALibro() cuando la categoría ya estaba asociada.
     */
    @Test
    void addCategoriaALibro_NoDeberiaGuardar_CuandoLaCategoriaYaEstabaAsociada() {
        Long libroId = 1L;
        Long categoriaId = 2L;

        Libro libro = crearLibroSimulado(libroId);

        Categoria categoria = new Categoria();
        categoria.setId(categoriaId);
        categoria.setNombre("Ficción");

        // La categoría ya está asociada al libro.
        libro.getCategorias().add(categoria);

        // Simula que existen el libro y la categoría.
        when(libroRepository.findById(libroId)).thenReturn(Optional.of(libro));
        when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));

        libroService.addCategoriaALibro(libroId, categoriaId);

        // Verifica las búsquedas realizadas.
        verify(libroRepository).findById(libroId);
        verify(categoriaRepository).findById(categoriaId);

        // No se guarda porque la asociación ya existía.
        verify(libroRepository, never()).save(any(Libro.class));
    }
}