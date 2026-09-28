package cl.triskeledu.usuarios.config;

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
 * Configuración de Spring Security para ms-usuarios.
 *
 * Este microservicio actúa como SERVIDOR DE AUTENTICACIÓN:
 * - Expone /api/v1/auth/** como endpoints públicos (login y registro)
 * - Protege /api/v1/usuarios/** según roles
 * - Usa sesiones STATELESS (sin cookies de sesión, solo JWT)
 *
 * Matriz de autorización:
 * ┌────────────────────────────┬────────┬────────────────────────────────────┐
 * │ Endpoint                   │ Método │ Acceso                             │
 * ├────────────────────────────┼────────┼────────────────────────────────────┤
 * │ /api/v1/auth/**            │ ALL    │ Público (sin token)                │
 * │ /actuator/**               │ ALL    │ Público (monitoreo)                │
 * │ /api/v1/usuarios/**        │ GET    │ Administrador, Bibliotecario       │
 * │ /api/v1/usuarios/**        │ POST   │ Administrador                      │
 * │ /api/v1/usuarios/**        │ PUT    │ Administrador                      │
 * │ /api/v1/usuarios/**        │ DELETE │ Administrador                      │
 * │ Cualquier otro             │ ALL    │ Autenticado (con token válido)     │
 * └────────────────────────────┴────────┴────────────────────────────────────┘
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Cadena de filtros de seguridad.
     *
     * Flujo de cada petición HTTP:
     * 1. JwtAuthenticationFilter (extrae y valida token)
     * 2. Spring Security verifica reglas de autorización
     * 3. Si pasa, llega al controlador; si no, devuelve 401 o 403
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Desactivar CSRF (no necesario en APIs stateless con JWT)
            .csrf(csrf -> csrf.disable())

            // 2. Política de sesiones: STATELESS (sin HttpSession ni cookies)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 3. Reglas de autorización por endpoint
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

                // Endpoints públicos (sin token)
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()

                // Endpoints de usuarios por rol
                .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/**")
                    .hasAnyRole("Administrador", "Bibliotecario")
                .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/**")
                    .hasRole("Administrador")
                .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/**")
                    .hasRole("Administrador")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/**")
                    .hasRole("Administrador")

                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )

            // 4. Agregar el filtro JWT ANTES del filtro de autenticación por defecto
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean para codificar contraseñas con BCrypt.
     * BCrypt genera un salt aleatorio automáticamente y produce hashes
     * de 60 caracteres que incluyen el salt (auto-contenidos).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
