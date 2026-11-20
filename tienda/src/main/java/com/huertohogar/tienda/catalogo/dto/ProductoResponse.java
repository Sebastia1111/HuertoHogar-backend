package com.huertohogar.tienda.catalogo.dto;

import com.huertohogar.tienda.catalogo.model.ProductoCatalogo;
import com.huertohogar.tienda.inventario.model.Producto;

/**
 * Producto tal como lo usa el frontend (services/productoMapper.js).
 * id = id del Producto de inventario (el que se usa en PUT/PATCH/DELETE).
 * categoria = slug (frutas, verduras, organicos, lacteos).
 */
public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        Double precio,
        int stock,
        int stockMinimo,
        String unidad,
        String categoria,
        String categoriaNombre,
        String origen,
        String emoji,
        String practicasSostenibles,
        String imagenUrl,
        boolean activo,
        boolean disponible,
        boolean stockBajo
) {
    public static ProductoResponse desde(ProductoCatalogo pc) {
        Producto p = pc.getProducto();
        int stock = p.getStock() == null ? 0 : p.getStock();
        return new ProductoResponse(p.getId(), pc.getCodigo(), p.getNombre(), p.getDescripcion(), p.getPrecio(),
                stock, pc.getStockMinimo(), pc.getUnidad(), pc.getCategoria().getSlug(),
                pc.getCategoria().getNombre(), pc.getOrigen(), pc.getEmoji(), pc.getPracticasSostenibles(),
                pc.getImagenUrl(), pc.isActivo(), pc.isActivo() && stock > 0, stock <= pc.getStockMinimo());
    }
}
