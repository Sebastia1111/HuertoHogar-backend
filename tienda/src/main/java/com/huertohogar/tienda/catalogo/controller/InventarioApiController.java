package com.huertohogar.tienda.catalogo.controller;

import com.huertohogar.tienda.catalogo.dto.AjusteStockRequest;
import com.huertohogar.tienda.catalogo.dto.MovimientoResponse;
import com.huertohogar.tienda.catalogo.dto.ProductoRequest;
import com.huertohogar.tienda.catalogo.dto.ProductoResponse;
import com.huertohogar.tienda.catalogo.dto.ResumenInventarioResponse;
import com.huertohogar.tienda.catalogo.dto.StockRequest;
import com.huertohogar.tienda.catalogo.service.ProductoGestionService;
import com.huertohogar.tienda.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestión del inventario (panel de administración del frontend, inventarioApi.js).
 * Los permisos por rol están en SecurityConfig. Convive con el ProductoController original
 * del equipo (/api/inventario), que no se modificó y no se muestra en Swagger.
 */
@RestController
@RequestMapping("/api/inventario")
@Tag(name = "3. Inventario (ADMIN / VENDEDOR)",
        description = "CRUD de productos y control de stock con historial. Consultar es público; "
                + "crear, editar y mover stock: ADMIN o VENDEDOR; eliminar: solo ADMIN")
public class InventarioApiController {

    private final ProductoGestionService productos;

    public InventarioApiController(ProductoGestionService productos) {
        this.productos = productos;
    }

    // ---------- Productos (CRUD) ----------

    @GetMapping("/productos")
    @SecurityRequirements
    @Operation(summary = "Listar productos con filtros · público",
            description = "Ej: `?q=miel&categoria=organicos&stockBajo=true`. "
                    + "`incluirInactivos` solo tiene efecto con token de ADMIN o VENDEDOR.")
    public List<ProductoResponse> listar(
            @Parameter(description = "Busca en nombre, descripción, origen y código") @RequestParam(required = false) String q,
            @Parameter(description = "frutas, verduras, organicos o lacteos") @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "false") boolean soloDisponibles,
            @Parameter(description = "true = solo los que están bajo su stock mínimo") @RequestParam(defaultValue = "false") boolean stockBajo,
            @RequestParam(defaultValue = "false") boolean incluirInactivos,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        boolean verInactivos = incluirInactivos && yo != null && yo.esPersonal();
        return productos.listar(categoria, q, soloDisponibles, stockBajo, verInactivos);
    }

    @GetMapping("/productos/{id}")
    @SecurityRequirements
    @Operation(summary = "Ver un producto por id · público")
    public ProductoResponse obtener(@Parameter(example = "1") @PathVariable Long id) {
        return productos.obtener(id);
    }

    @PostMapping("/productos")
    @Operation(summary = "Crear producto · ADMIN o VENDEDOR",
            description = "Registra el stock inicial como un movimiento de ENTRADA.")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest req,
                                                  @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productos.crear(req, yo.correo()));
    }

    @PutMapping("/productos/{id}")
    @Operation(summary = "Editar producto · ADMIN o VENDEDOR",
            description = "Si cambia el stock, queda registrado como un movimiento de AJUSTE.")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest req,
                                       @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        return productos.actualizar(id, req, yo.correo());
    }

    @DeleteMapping("/productos/{id}")
    @Operation(summary = "Eliminar producto · solo ADMIN",
            description = "Por defecto es una baja lógica (deja de venderse y conserva su historial). "
                    + "Con `definitivo=true` se borra, solo si nunca se vendió.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean definitivo) {
        productos.eliminar(id, definitivo);
        return ResponseEntity.noContent().build();
    }

    // ---------- Stock ----------

    @PatchMapping("/productos/{id}/stock")
    @Operation(summary = "Fijar el stock · ADMIN o VENDEDOR",
            description = "Lo usa el panel \"Gestión de Stock\" del frontend (conteo físico).")
    public ProductoResponse actualizarStock(@PathVariable Long id, @Valid @RequestBody StockRequest req,
                                            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        return productos.actualizarStock(id, req.stock(), req.motivo(), yo.correo());
    }

    @PostMapping("/productos/{id}/ajuste")
    @Operation(summary = "Entrada o salida de stock · ADMIN o VENDEDOR",
            description = "ENTRADA suma unidades (llegó mercadería); SALIDA las resta (merma). Nunca deja stock negativo.")
    public ProductoResponse ajustar(@PathVariable Long id, @Valid @RequestBody AjusteStockRequest req,
                                    @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        return productos.ajustar(id, req, yo.correo());
    }

    @GetMapping("/productos/{id}/movimientos")
    @Operation(summary = "Historial de stock de un producto · ADMIN o VENDEDOR",
            description = "Quién cambió el stock, cuándo, cuánto y por qué (incluye las ventas).")
    public List<MovimientoResponse> movimientos(@PathVariable Long id) {
        return productos.movimientos(id);
    }

    // ---------- Indicadores ----------

    @GetMapping("/resumen")
    @Operation(summary = "Resumen del inventario · ADMIN o VENDEDOR",
            description = "Total de productos, unidades, valor del inventario y cuántos tienen stock bajo o agotado.")
    public ResumenInventarioResponse resumen() {
        return productos.resumen();
    }

    @GetMapping("/alertas/stock-bajo")
    @Operation(summary = "Productos con stock bajo · ADMIN o VENDEDOR")
    public List<ProductoResponse> alertas() {
        return productos.alertasStockBajo();
    }
}
