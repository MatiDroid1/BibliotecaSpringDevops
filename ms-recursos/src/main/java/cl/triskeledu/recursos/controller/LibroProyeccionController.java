package cl.triskeledu.recursos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.triskeledu.recursos.dto.LibroProyeccionRequest;
import cl.triskeledu.recursos.dto.LibroProyeccionResponse;
import cl.triskeledu.recursos.service.LibroProyeccionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/libros-proyeccion")
public class LibroProyeccionController {

    private final LibroProyeccionService libroProyeccionService;

    @GetMapping
    public ResponseEntity<List<LibroProyeccionResponse>> findAll() {
        return ResponseEntity.ok(libroProyeccionService.findAll());
    }

    @PutMapping("/{isbn}")
    public ResponseEntity<Void> upsert(
            @PathVariable String isbn,
            @Valid @RequestBody LibroProyeccionRequest request) {
        libroProyeccionService.save(isbn, request.getTitulo());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> deleteByIsbn(@PathVariable String isbn) {
        libroProyeccionService.deleteByIsbn(isbn);
        return ResponseEntity.noContent().build();
    }
}
