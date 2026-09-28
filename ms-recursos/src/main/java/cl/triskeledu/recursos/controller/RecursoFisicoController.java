package cl.triskeledu.recursos.controller;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.triskeledu.recursos.dto.RecursoFisicoRequest;
import cl.triskeledu.recursos.dto.RecursoFisicoResponse;
import cl.triskeledu.recursos.service.RecursoFisicoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recursos")
public class RecursoFisicoController {

    private final RecursoFisicoService recursoService;

    // ─── Métodos auxiliares HATEOAS ───────────────────────────────────────────

    /**
     * Agrega los links de navegación a un RecursoFisicoResponse:
     *   - self         → GET    /api/v1/recursos/{id}
     *   - update       → PUT    /api/v1/recursos/{id}
     *   - delete       → DELETE /api/v1/recursos/{id}
     *   - disponibles  → GET    /api/v1/recursos/disponibles?disponible=true
     *                    (útil para saber si hay otros recursos disponibles
     *                     del mismo tipo; muy relevante para préstamos/reservas)
     *   - all          → GET    /api/v1/recursos
     */
    private RecursoFisicoResponse addLinks(RecursoFisicoResponse recurso) {
        Long id = recurso.getId();

        recurso.add(linkTo(methodOn(RecursoFisicoController.class).findById(id)).withSelfRel());

        recurso.add(linkTo(methodOn(RecursoFisicoController.class).update(id, null))
                .withRel("update").withTitle("PUT - Actualizar recurso"));

        recurso.add(linkTo(methodOn(RecursoFisicoController.class).deleteById(id))
                .withRel("delete").withTitle("DELETE - Eliminar recurso"));

        // Link a recursos disponibles: muy útil para clientes que quieren
        // saber cuáles están disponibles para préstamo antes de hacer una solicitud.
        recurso.add(linkTo(methodOn(RecursoFisicoController.class).findByDisponible(true))
                .withRel("disponibles").withTitle("GET - Recursos disponibles"));

        recurso.add(linkTo(methodOn(RecursoFisicoController.class).findAll())
                .withRel("all").withTitle("GET - Listado de recursos"));

        return recurso;
    }

    // ─── Endpoints ────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<CollectionModel<RecursoFisicoResponse>> findAll() {
        List<RecursoFisicoResponse> recursos = recursoService.findAll();
        recursos.forEach(this::addLinks);

        CollectionModel<RecursoFisicoResponse> collection = CollectionModel.of(
                recursos,
                linkTo(methodOn(RecursoFisicoController.class).findAll()).withSelfRel()
        );
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecursoFisicoResponse> findById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(addLinks(recursoService.findById(id)));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<RecursoFisicoResponse> findBySku(@PathVariable String sku) {
        return ResponseEntity.ok(addLinks(recursoService.findBySku(sku)));
    }

    // GET /api/v1/recursos/disponibles?disponible=true
    @GetMapping("/disponibles")
    public ResponseEntity<CollectionModel<RecursoFisicoResponse>> findByDisponible(
            @RequestParam(defaultValue = "true") Boolean disponible) {
        List<RecursoFisicoResponse> recursos = recursoService.findByDisponible(disponible);
        recursos.forEach(this::addLinks);

        CollectionModel<RecursoFisicoResponse> collection = CollectionModel.of(
                recursos,
                linkTo(methodOn(RecursoFisicoController.class).findByDisponible(disponible)).withSelfRel()
        );
        return ResponseEntity.ok(collection);
    }

    // GET /api/v1/recursos/tipo/Libro
    @GetMapping("/tipo/{tipoRecurso}")
    public ResponseEntity<CollectionModel<RecursoFisicoResponse>> findByTipo(@PathVariable String tipoRecurso) {
        List<RecursoFisicoResponse> recursos = recursoService.findByTipoRecurso(tipoRecurso);
        recursos.forEach(this::addLinks);

        CollectionModel<RecursoFisicoResponse> collection = CollectionModel.of(
                recursos,
                linkTo(methodOn(RecursoFisicoController.class).findByTipo(tipoRecurso)).withSelfRel()
        );
        return ResponseEntity.ok(collection);
    }

    @PostMapping
    public ResponseEntity<RecursoFisicoResponse> create(@Valid @RequestBody RecursoFisicoRequest request) {
        RecursoFisicoResponse creado = addLinks(recursoService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecursoFisicoResponse> update(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody RecursoFisicoRequest request) {
        return ResponseEntity.ok(addLinks(recursoService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @NonNull Long id) {
        recursoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint utilitario interno (usa Feign desde ms-catalogo): no aplica HATEOAS
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Boolean> existByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(recursoService.existsByIsbn(isbn));
    }
}
