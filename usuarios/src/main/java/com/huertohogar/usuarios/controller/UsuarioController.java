package com.huertohogar.usuarios.controller;

import com.huertohogar.usuarios.dto.UsuarioAdminRequest;
import com.huertohogar.usuarios.dto.UsuarioResponse;
import com.huertohogar.usuarios.model.Rol;
import com.huertohogar.usuarios.security.UsuarioAutenticado;
import com.huertohogar.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CRUD de usuarios para el administrador (PanelUsuarios.jsx). La regla de rol está en SecurityConfig. */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "3. Usuarios (ADMIN)", description = "CRUD completo de cuentas. Solo para el rol ADMIN")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios · ADMIN", description = "Filtro opcional por rol, ej: ?rol=CLIENTE")
    public List<UsuarioResponse> listar(@Parameter(description = "ADMIN, VENDEDOR o CLIENTE")
                                        @RequestParam(required = false) Rol rol) {
        return service.listar(rol);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver un usuario · ADMIN")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crear usuario con cualquier rol · ADMIN")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioAdminRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar usuario (rol, activo, datos) · ADMIN",
            description = "Si la clave viene vacía, no se cambia. El admin no puede quitarse su rol ni desactivarse.")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioAdminRequest req,
                                      @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        return service.actualizar(id, req, yo.id());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario · ADMIN", description = "El admin no puede eliminar su propia cuenta.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        service.eliminar(id, yo.id());
        return ResponseEntity.noContent().build();
    }
}
