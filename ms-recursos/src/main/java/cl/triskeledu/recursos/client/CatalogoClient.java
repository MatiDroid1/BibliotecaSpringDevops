package cl.triskeledu.recursos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-catalogo", url = "${clients.ms-catalogo.url:}")
public interface CatalogoClient {

    @GetMapping("/api/v1/libros/existe/isbn/{isbn}")
    boolean existsByIsbn(@PathVariable("isbn") String isbn);

}
