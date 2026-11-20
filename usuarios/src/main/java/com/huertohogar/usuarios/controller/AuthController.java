package com.huertohogar.usuarios.controller;

import com.huertohogar.usuarios.dto.AuthResponse;
import com.huertohogar.usuarios.dto.LoginRequest;
import com.huertohogar.usuarios.dto.RefreshRequest;
import com.huertohogar.usuarios.dto.RegistroRequest;
import com.huertohogar.usuarios.dto.UsuarioResponse;
import com.huertohogar.usuarios.security.UsuarioAutenticado;
import com.huertohogar.usuarios.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints que usa src/services/authApi.js del frontend. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Sesión", description = "Registro, inicio y cierre de sesión, y renovación de tokens")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesión · público",
            description = "El ejemplo ya trae la cuenta admin. Copia el `accessToken` de la respuesta y pégalo en **Authorize**.")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/registro")
    @SecurityRequirements
    @Operation(summary = "Registrar cliente · público",
            description = "Crea una cuenta con rol CLIENTE y devuelve los tokens (queda con la sesión iniciada).")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(req));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Renovar tokens · público",
            description = "Recibe el `refreshToken` (7 días) y entrega un par nuevo. Deja de servir al cerrar sesión.")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refrescar(req.refreshToken());
    }

    @GetMapping("/me")
    @Operation(summary = "Usuario de la sesión actual · con token",
            description = "El frontend lo llama al recargar la página para comprobar que el token sigue vigente.")
    public UsuarioResponse me(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        return authService.perfil(yo.id());
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión · con token",
            description = "Invalida todos los tokens emitidos antes (access y refresh). Responde 204 sin contenido.")
    public ResponseEntity<Void> logout(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        authService.logout(yo.id());
        return ResponseEntity.noContent().build();
    }
}
