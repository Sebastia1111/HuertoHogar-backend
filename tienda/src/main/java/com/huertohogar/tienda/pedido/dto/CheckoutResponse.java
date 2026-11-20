package com.huertohogar.tienda.pedido.dto;

import com.huertohogar.tienda.catalogo.dto.ProductoResponse;

import java.util.List;

/**
 * Respuesta del checkout. "productos" trae el stock ya descontado:
 * useInventario.js del frontend lo usa para actualizar el catalogo en pantalla.
 */
public record CheckoutResponse(
        String numero,
        long total,
        PedidoResponse pedido,
        List<ProductoResponse> productos
) {
}
