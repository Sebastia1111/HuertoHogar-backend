package com.huertohogar.usuarios.service;

import com.huertohogar.usuarios.dto.AuthResponse;
import com.huertohogar.usuarios.dto.LoginRequest;
import com.huertohogar.usuarios.dto.RegistroRequest;
import com.huertohogar.usuarios.dto.UsuarioResponse;
import com.huertohogar.usuarios.exception.ReglaNegocioException;
import com.huertohogar.usuarios.model.Rol;
import com.huertohogar.usuarios.model.Usuario;
import com.huertohogar.usuarios.repository.UsuarioRepository;
import com.huertohogar.usuarios.validation.RunValidator;
import com.huertohogar.usuarios.security.JwtService;
import com.huertohogar.usuarios.security.JwtService.DatosRefresh;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Logica de autenticacion:
 * - registro (siempre como CLIENTE)
 * - login (verifica BCrypt y entrega access + refresh token)
 * - refresh: valida el refresh token (JWT) y entrega un par nuevo
 * - logout: sube tokenVersion, asi todos los tokens anteriores dejan de servir
 * No se guarda ningun token en la base de datos.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest req) {
        String correo = req.correo().trim().toLowerCase();
        String run = RunValidator.normalizar(req.run());
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Este correo ya se encuentra registrado.",
                    Map.of("correo", "Este correo ya se encuentra registrado."));
        }
        if (usuarioRepository.existsByRun(run)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Este RUN ya se encuentra registrado.",
                    Map.of("run", "Este RUN ya se encuentra registrado."));
        }
        Usuario usuario = Usuario.builder()
                .run(run)
                .nombre(req.nombre().trim())
                .apellidos(req.apellidos().trim())
                .correo(correo)
                .password(passwordEncoder.encode(req.clave()))
                .fechaNacimiento(req.fechaNacimiento())
                .region(req.region())
                .comuna(req.comuna())
                .direccion(req.direccion().trim())
                .telefono(req.telefono())
                .rol(Rol.CLIENTE) // nadie puede auto-registrarse como ADMIN o VENDEDOR
                .activo(true)
                .build();
        usuarioRepository.save(usuario);
        return emitirTokens(usuario);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        // Mismo mensaje para correo o contrasena incorrectos: no revela que correos existen
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(req.correo().trim())
                .filter(u -> passwordEncoder.matches(req.clave(), u.getPassword()))
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.UNAUTHORIZED,
                        "Correo o contraseña incorrectos"));
        if (!usuario.isActivo()) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "La cuenta está desactivada");
        }
        return emitirTokens(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse refrescar(String refreshToken) {
        DatosRefresh datos;
        try {
            datos = jwtService.validarRefresh(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ReglaNegocioException(HttpStatus.UNAUTHORIZED, "Sesión expirada. Inicia sesión nuevamente");
        }
        Usuario usuario = usuarioRepository.findById(datos.usuarioId())
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.UNAUTHORIZED, "Refresh token inválido"));
        if (usuario.getTokenVersion() != datos.version()) {
            // Cerro sesion o cambio su clave despues de emitir este token
            throw new ReglaNegocioException(HttpStatus.UNAUTHORIZED, "Sesión cerrada. Inicia sesión nuevamente");
        }
        if (!usuario.isActivo()) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "La cuenta está desactivada");
        }
        return emitirTokens(usuario);
    }

    /**
     * Cierre de sesion con el access token (asi lo llama el frontend):
     * sube tokenVersion y todos los tokens emitidos antes (access y refresh) dejan de servir.
     */
    @Transactional
    public void logout(Long usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(u -> u.setTokenVersion(u.getTokenVersion() + 1));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .map(UsuarioResponse::desde)
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    private AuthResponse emitirTokens(Usuario usuario) {
        return new AuthResponse(jwtService.generarAccessToken(usuario), jwtService.generarRefreshToken(usuario),
                "Bearer", jwtService.getExpiracionSegundos(), UsuarioResponse.desde(usuario));
    }
}
