package cl.triskeledu.common.server;

import java.io.IOException;

import org.springframework.core.env.Environment;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro HTTP que agrega headers de infraestructura a cada respuesta:
 *
 *   X-Instance-Port: puerto real en el que corre esta instancia
 *   X-Instance-Id:   nombre-del-servicio + puerto (ej. ms-catalogo-9002)
 *
 * Diseñado como clase pura — sin @Component — para que su ciclo de vida
 * sea controlado por {@link InstanceHeaderAutoConfiguration} a través de
 * un {@link org.springframework.boot.web.servlet.FilterRegistrationBean}.
 *
 * Resolución del puerto en cada request — orden de precedencia:
 *   1. local.server.port  →  puerto real asignado por Tomcat al arrancar
 *                             (disponible solo después de que el contenedor
 *                             embebido haya iniciado; funciona con puerto=0)
 *   2. server.port        →  valor configurado en application.yml
 *   3. 8080               →  fallback por defecto de Spring Boot
 *
 * La resolución ocurre en doFilter y no en el constructor porque esta clase
 * se instancia durante el arranque del contexto Spring, antes de que Tomcat
 * haya asignado el puerto. En ese momento local.server.port aún no existe.
 *
 * Permite que la información de instancia sea visible en los headers HTTP
 * sin contaminar los DTOs de negocio.
 */
public class InstanceHeaderFilter implements Filter {

    private final Environment environment;
    private final String applicationName;

    public InstanceHeaderFilter(Environment environment, String applicationName) {
        this.environment = environment;
        this.applicationName = applicationName;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;

        // Resuelto aquí y no en el constructor: garantiza que Tomcat ya asignó
        // el puerto real antes de que llegue el primer request.
        String localPort  = environment.getProperty("local.server.port");
        String serverPort = environment.getProperty("server.port");
        String port = localPort != null ? localPort
                    : serverPort != null ? serverPort
                    : "8080";

        response.setHeader("X-Instance-Port", port);
        response.setHeader("X-Instance-Id", applicationName + "-" + port);

        chain.doFilter(req, res);
    }
}