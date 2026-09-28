package cl.triskeledu.catalogo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "cl.triskeledu.catalogo.client")
@SpringBootApplication
public class BibliotecaCatalogoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BibliotecaCatalogoApplication.class, args);
	}

}
