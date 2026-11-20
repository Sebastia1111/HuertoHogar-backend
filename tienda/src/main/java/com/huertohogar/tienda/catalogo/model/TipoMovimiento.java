package com.huertohogar.tienda.catalogo.model;

public enum TipoMovimiento {
    /** Llega mercaderia */
    ENTRADA,
    /** Merma, perdida o salida manual */
    SALIDA,
    /** Conteo fisico: se fija el stock a un valor */
    AJUSTE,
    /** Descuento por una compra */
    VENTA,
    /** Devolucion de stock por un pedido cancelado */
    DEVOLUCION
}
