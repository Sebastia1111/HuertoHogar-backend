package com.huertohogar.usuarios.security;

import com.huertohogar.usuarios.repository.UsuarioRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee el header "Authorization: Bearer <token>", lo valida y, si es correcto,
 * deja al usuario autenticado con su rol (ROLE_ADMIN, ROLE_VENDEDOR, ROLE_CLIENTE).
 *
 * No es @Component a proposito: se crea en SecurityConfig para que solo corra dentro
 * de la cadena de Spring Security (si fuera bean, Spring Boot lo registraria dos veces).
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String ATRIBUTO_ERROR = "jwt_error";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                UsuarioAutenticado usuario = jwtService.validar(token);
                // El token deja de servir si el usuario fue desactivado/eliminado
                // o si cerro sesion / cambio su clave (tokenVersion distinta)
                boolean vigente = usuarioRepository.findById(usuario.id())
                        .map(u -> u.isActivo() && u.getTokenVersion() == usuario.version())
                        .orElse(false);
                if (vigente) {
                    var auth = new UsernamePasswordAuthenticationToken(usuario, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol())));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    request.setAttribute(ATRIBUTO_ERROR, "Sesión cerrada o usuario inactivo. Inicia sesión nuevamente");
                }
            } catch (ExpiredJwtException e) {
                request.setAttribute(ATRIBUTO_ERROR, "TOKEN_EXPIRADO");
            } catch (JwtException | IllegalArgumentException e) {
                request.setAttribute(ATRIBUTO_ERROR, "Token inválido");
            }
        }
        chain.doFilter(request, response);
    }
}
