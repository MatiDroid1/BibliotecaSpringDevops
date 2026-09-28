package cl.triskeledu.common.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * Auto-configuración que registra {@link InstanceHeaderFilter} en cada
 * microservicio servlet que tenga la librería common en su classpath.
 *
 * Se activa únicamente en aplicaciones web de tipo SERVLET (excluye
 * el API Gateway, que es reactivo/WebFlux).
 *
 * El filtro se registra con la prioridad más alta (Ordered.HIGHEST_PRECEDENCE)
 * para que los headers X-Instance-Port y X-Instance-Id estén presentes
 * incluso cuando otro filtro (como el de seguridad) rechace la petición.
 *
 * El puerto NO se resuelve aquí en tiempo de construcción del bean, sino
 * dentro de {@link InstanceHeaderFilter#doFilter} en cada request. Esto es
 * necesario porque esta auto-configuración se ejecuta antes de que Tomcat
 * haya arrancado, momento en que local.server.port aún no existe y
 * server.port puede valer 0. Al delegar la resolución al filtro se garantiza
 * que el puerto ya fue asignado por el contenedor embebido.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class InstanceHeaderAutoConfiguration {

    @Bean
    public FilterRegistrationBean<InstanceHeaderFilter> instanceHeaderFilter(
            Environment environment,
            @Value("${spring.application.name:unknown}") String applicationName) {

        FilterRegistrationBean<InstanceHeaderFilter> registration =
                new FilterRegistrationBean<>(new InstanceHeaderFilter(environment, applicationName));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}