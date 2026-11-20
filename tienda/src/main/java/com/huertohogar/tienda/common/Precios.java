package com.huertohogar.tienda.common;

/**
 * Reglas de precio compartidas por carrito y pedidos.
 * Igual que el frontend (utils/storage.js): correos @duocuc.cl tienen 20% de descuento.
 * Los precios en Chile incluyen IVA (19%): neto = total / 1.19.
 */
public final class Precios {

    public static final double IVA = 0.19;
    public static final double DESCUENTO_DUOC = 0.20;

    private Precios() {
    }

    public static boolean tieneDescuentoDuoc(String correo) {
        return correo != null && correo.toLowerCase().endsWith("@duocuc.cl");
    }

    /** Precio unitario final redondeado a pesos (mismo calculo que calcularPrecioReal del frontend). */
    public static long precioFinal(Double precio, String correo) {
        long base = precio == null ? 0 : Math.round(precio);
        return tieneDescuentoDuoc(correo) ? Math.round(base * (1 - DESCUENTO_DUOC)) : base;
    }

    public static long neto(long total) {
        return Math.round(total / (1 + IVA));
    }
}
