package cl.triskeledu.common;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import cl.triskeledu.common.security.JwtProperties;

/**
 * Auto-configuración de la librería common.
 *
 * Spring Boot descubre esta clase automáticamente al leer el archivo:
 *   META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 *
 * Esto permite que cualquier microservicio que declare common como dependencia
 * Maven reciba todos sus beans (p.ej. GlobalExceptionHandler, JwtTokenProvider,
 * JwtAuthenticationFilter) sin necesidad de agregar @ComponentScan ni @Import
 * en su propia clase principal.
 *
 * El @ComponentScan apunta al paquete raíz de common para registrar todos
 * los @Component, @RestControllerAdvice, @Service, etc. que se agreguen
 * en el futuro sin modificar esta clase.
 *
 * @EnableConfigurationProperties activa la lectura de JwtProperties desde
 * el application.yml de cada microservicio.
 */
@AutoConfiguration
@ComponentScan(basePackages = "cl.triskeledu.common")
@EnableConfigurationProperties(JwtProperties.class)
public class CommonAutoConfiguration {
    // Clase de configuración pura: no necesita cuerpo.
    // Spring Boot la procesa al arrancar cualquier microservicio
    // que tenga common en su classpath.
}
