package com.huertohogar.tienda.catalogo.controller;

import com.huertohogar.tienda.catalogo.dto.CategoriaRequest;
import com.huertohogar.tienda.catalogo.dto.CategoriaResponse;
import com.huertohogar.tienda.catalogo.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CRUD de categorías (frutas, verduras, orgánicos, lácteos...). Los permisos están en SecurityConfig. */
@RestController
@RequestMapping("/api/inventario/categorias")
@Tag(name = "4. Categorías", description = "Consultar es público; crear y editar: ADMIN o VENDEDOR; eliminar: solo ADMIN")
public class CategoriaController {

    private final CategoriaService categorias;

    public CategoriaController(CategoriaService categorias) {
        this.categorias = categorias;
    }

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Listar categorías · público")
    public List<CategoriaResponse> listar() {
        return categorias.listar();
    }

    @PostMapping
    @Operation(summary = "Crear categoría · ADMIN o VENDEDOR")
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categorias.crear(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar categoría · ADMIN o VENDEDOR")
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return categorias.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría · solo ADMIN", description = "Solo si no tiene productos asociados.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categorias.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
