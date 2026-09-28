package cl.triskeledu.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "cl.triskeledu.usuarios.client")
@SpringBootApplication
public class BibliotecaUsuariosApplication {

	public static void main(String[] args) {
		SpringApplication.run(BibliotecaUsuariosApplication.class, args);
	}

}
