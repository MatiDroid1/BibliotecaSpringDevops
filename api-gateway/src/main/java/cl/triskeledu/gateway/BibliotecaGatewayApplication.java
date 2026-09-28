package cl.triskeledu.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Clase principal del API Gateway del proyecto Biblioteca de Triskeledu.
 *
 * Esta aplicación actúa como punto de entrada único (Single Entry Point) para
 * todos los microservicios del sistema. Todas las peticiones de los clientes
 * (navegadores, Postman, aplicaciones móviles, etc.) deben dirigirse a este
 * gateway en el puerto 9000, y éste se encarga de enrutar cada petición al
 * microservicio correspondiente.
 *
 * Funcionalidades principales:
 * - Enrutamiento inteligente de peticiones basado en el path de la URL.
 * - Balanceo de carga automático (round-robin) entre múltiples instancias
 *   de un mismo microservicio, usando Spring Cloud LoadBalancer y Eureka.
 * - Descubrimiento dinámico de servicios mediante Eureka Server.
 * - Configuración CORS centralizada para peticiones desde navegadores.
 *
 * Arquitectura:
 * El gateway utiliza Spring Cloud Gateway basado en WebFlux (reactivo),
 * que proporciona un modelo de I/O no bloqueante ideal para un proxy/router
 * de alto rendimiento.
 *
 * @see <a href="https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway.html">
 *      Spring Cloud Gateway Documentation</a>
 */
@SpringBootApplication
@EnableDiscoveryClient  // Habilita el registro y descubrimiento de servicios en Eureka Server
public class BibliotecaGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaGatewayApplication.class, args);
    }

}
