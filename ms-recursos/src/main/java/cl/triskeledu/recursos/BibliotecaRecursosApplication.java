package cl.triskeledu.recursos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "cl.triskeledu.recursos.client")
@SpringBootApplication
public class BibliotecaRecursosApplication {

	public static void main(String[] args) {
		SpringApplication.run(BibliotecaRecursosApplication.class, args);
	}

}
