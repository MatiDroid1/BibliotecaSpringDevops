package cl.triskeledu.recursos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import cl.triskeledu.recursos.dto.UsuarioResponse;

@FeignClient(name = "ms-usuarios", url = "${clients.ms-usuarios.url:}")
public interface UserClient {

    @GetMapping("/api/v1/usuarios/email/{email}")
    UsuarioResponse findByEmail(@PathVariable("email") String email);

}
