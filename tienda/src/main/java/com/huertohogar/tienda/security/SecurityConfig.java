package com.huertohogar.tienda.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Permisos por rol en la tienda:
 *  - Publico (sin token): ver catalogo, categorias e inventario.
 *  - Cualquier usuario logueado: comprar, ver y cancelar sus pedidos.
 *  - VENDEDOR y ADMIN: crear/editar productos, ajustar stock, ver resumen, alertas y todos los pedidos,
 *    cambiar el estado de los pedidos.
 *  - Solo ADMIN: eliminar productos y categorias.
 */
@Configuration
public class SecurityConfig {

    private static final String[] RUTAS_INVENTARIO = {"/api/inventario", "/api/inventario/**"};

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // consola H2
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/h2-console/**", "/error").permitAll()
                        // Lecturas solo para el personal (antes que las lecturas publicas)
                        .requestMatchers(HttpMethod.GET, "/api/inventario/resumen", "/api/inventario/alertas/**",
                                "/api/inventario/productos/*/movimientos", "/api/tienda/pedidos/todos").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/tienda/pedidos/*/estado").hasAnyRole("ADMIN", "VENDEDOR")
                        // Lectura publica del catalogo e inventario
                        .requestMatchers(HttpMethod.GET, RUTAS_INVENTARIO).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tienda/catalogo", "/api/tienda/catalogo/**").permitAll()
                        // Inventario: eliminar solo ADMIN; crear/editar/stock ADMIN o VENDEDOR
                        .requestMatchers(HttpMethod.DELETE, RUTAS_INVENTARIO).hasRole("ADMIN")
                        .requestMatchers(RUTAS_INVENTARIO).hasAnyRole("ADMIN", "VENDEDOR")
                        // Checkout y pedidos: cualquier usuario logueado (la propiedad se valida en el servicio)
                        .requestMatchers("/api/tienda/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(RestAuthHandlers.noAutenticado())
                        .accessDeniedHandler(RestAuthHandlers.accesoDenegado()))
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * La tienda no guarda usuarios (eso es del microservicio usuarios).
     * Este bean solo evita que Spring cree un usuario en memoria con contrasena generada.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("La tienda autentica solo con JWT");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(origenes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
