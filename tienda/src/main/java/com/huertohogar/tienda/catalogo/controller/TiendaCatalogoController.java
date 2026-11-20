package com.huertohogar.tienda.catalogo.controller;

import com.huertohogar.tienda.catalogo.dto.ProductoResponse;
import com.huertohogar.tienda.catalogo.service.ProductoGestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catálogo público que carga el frontend (tiendaApi.listarCatalogo). Solo productos activos. */
@RestController
@RequestMapping("/api/tienda/catalogo")
@SecurityRequirements
@Tag(name = "1. Catálogo (público)", description = "Lo que ve cualquier visitante, sin iniciar sesión")
public class TiendaCatalogoController {

    private final ProductoGestionService productos;

    public TiendaCatalogoController(ProductoGestionService productos) {
        this.productos = productos;
    }

    @GetMapping
    @Operation(summary = "Ver el catálogo con filtros · público",
            description = "Todos los filtros son opcionales. Ej: `?q=manzana&categoria=frutas&soloDisponibles=true`")
    public List<ProductoResponse> catalogo(
            @Parameter(description = "Busca en nombre, descripción, origen y código", example = "manzana")
            @RequestParam(required = false) String q,
            @Parameter(description = "frutas, verduras, organicos o lacteos", example = "frutas")
            @RequestParam(required = false) String categoria,
            @Parameter(description = "true = solo productos con stock")
            @RequestParam(defaultValue = "false") boolean soloDisponibles) {
        return productos.listar(categoria, q, soloDisponibles, false, false);
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Detalle de un producto · público")
    public ProductoResponse detalle(@Parameter(example = "FR001") @PathVariable String codigo) {
        return productos.obtenerPorCodigo(codigo, false);
    }
}
