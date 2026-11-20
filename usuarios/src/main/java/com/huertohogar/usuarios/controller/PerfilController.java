package com.huertohogar.usuarios.controller;

import com.huertohogar.usuarios.dto.CambioPasswordRequest;
import com.huertohogar.usuarios.dto.PerfilUpdateRequest;
import com.huertohogar.usuarios.dto.UsuarioResponse;
import com.huertohogar.usuarios.security.UsuarioAutenticado;
import com.huertohogar.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Perfil del usuario logueado (PerfilPage.jsx del frontend). Siempre opera sobre el dueño del token. */
@RestController
@RequestMapping("/api/usuarios/me")
@Tag(name = "2. Mi perfil", description = "El usuario logueado ve y edita sus propios datos (cualquier rol)")
public class PerfilController {

    private final UsuarioService service;

    public PerfilController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Ver mi perfil · con token")
    public UsuarioResponse miPerfil(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        return service.obtenerPerfil(yo.id());
    }

    @PutMapping
    @Operation(summary = "Actualizar mi perfil · con token",
            description = "Nombre, apellidos, fecha de nacimiento, región, comuna, dirección de entrega y teléfono.")
    public UsuarioResponse actualizarMiPerfil(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo,
                                              @Valid @RequestBody PerfilUpdateRequest req) {
        return service.actualizarPerfil(yo.id(), req);
    }

    @PutMapping("/password")
    @Operation(summary = "Cambiar mi contraseña · con token",
            description = "Por seguridad cierra todas las sesiones: hay que volver a iniciar sesión.")
    public ResponseEntity<Void> cambiarPassword(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo,
                                                @Valid @RequestBody CambioPasswordRequest req) {
        service.cambiarPassword(yo.id(), req);
        return ResponseEntity.noContent().build();
    }
}
