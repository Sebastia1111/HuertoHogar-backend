package com.huertohogar.tienda.catalogo.dto;

/** Indicadores para el panel de administracion. */
public record ResumenInventarioResponse(
        long totalProductos,
        long productosActivos,
        long unidadesEnStock,
        long valorInventario,
        long productosStockBajo,
        long productosSinStock,
        long categorias
) {
}
