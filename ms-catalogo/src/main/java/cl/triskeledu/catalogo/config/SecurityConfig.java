package cl.triskeledu.catalogo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import cl.triskeledu.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

/**
 * Configuración de Spring Security para ms-catalogo.
 *
 * Este microservicio actúa como RESOURCE SERVER:
 * - NO emite tokens JWT (eso lo hace ms-usuarios)
 * - Valida tokens JWT en cada petición usando el filtro compartido
 * - Aplica reglas de autorización según el rol del usuario
 *
 * Matriz de autorización:
 * ┌────────────────────────────┬────────┬────────────────────────────────────────────────┐
 * │ Endpoint                   │ Método │ Acceso                                         │
 * ├────────────────────────────┼────────┼────────────────────────────────────────────────┤
 * │ /actuator/**               │ ALL    │ Público (monitoreo)                            │
 * │ /api/v1/libros/**          │ GET    │ Administrador, Bibliotecario, Cliente           │
 * │ /api/v1/libros/**          │ POST   │ Administrador, Bibliotecario                   │
 * │ /api/v1/libros/**          │ PUT    │ Administrador, Bibliotecario                   │
 * │ /api/v1/libros/**          │ DELETE │ Administrador, Bibliotecario                   │
 * │ Cualquier otro             │ ALL    │ Autenticado (con token válido)                 │
 * └────────────────────────────┴────────┴────────────────────────────────────────────────┘
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth

                // [SWAGGER-INI]
                // PERMITIR RUTAS PÚBLICAS DE SWAGGER / SPRINGDOC
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/favicon.ico"
                ).permitAll()
                // [SWAGGER-FIN]

                // Actuator siempre público
                .requestMatchers("/actuator/**").permitAll()

                // Lectura de libros: cualquier rol autenticado
                .requestMatchers(HttpMethod.GET, "/api/v1/libros/**")
                    .hasAnyRole("Administrador", "Bibliotecario", "Cliente")

                // Escritura de libros: solo Administrador y Bibliotecario
                .requestMatchers(HttpMethod.POST, "/api/v1/libros/**")
                    .hasAnyRole("Administrador", "Bibliotecario")
                .requestMatchers(HttpMethod.PUT, "/api/v1/libros/**")
                    .hasAnyRole("Administrador", "Bibliotecario")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/libros/**")
                    .hasAnyRole("Administrador", "Bibliotecario")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
