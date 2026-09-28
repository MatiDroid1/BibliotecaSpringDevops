package cl.triskeledu.catalogo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import cl.triskeledu.catalogo.dto.LibroProyeccionSyncRequest;

@FeignClient(name = "ms-recursos", url = "${clients.ms-recursos.url:}")
public interface RecursoClient {

    @GetMapping("/api/v1/recursos/isbn/{isbn}")
    boolean existsByIsbn(@PathVariable("isbn") String isbn);

    @PutMapping("/api/v1/libros-proyeccion/{isbn}")
    void upsertLibroProyeccion(
            @PathVariable("isbn") String isbn,
            @RequestBody LibroProyeccionSyncRequest request);

    @DeleteMapping("/api/v1/libros-proyeccion/{isbn}")
    void deleteLibroProyeccion(@PathVariable("isbn") String isbn);
}
