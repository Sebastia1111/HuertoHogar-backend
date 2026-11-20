package com.huertohogar.tienda.pedido.model;

import java.util.Set;

/** Ciclo de vida del pedido, desde la preparacion hasta la entrega. */
public enum EstadoPedido {
    PENDIENTE("Pedido recibido"),
    EN_PREPARACION("En preparación"),
    EN_CAMINO("En camino"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    private final String descripcion;

    EstadoPedido(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Transiciones permitidas: solo se avanza, y se puede cancelar antes de despachar. */
    public boolean puedeCambiarA(EstadoPedido nuevo) {
        return switch (this) {
            case PENDIENTE -> Set.of(EN_PREPARACION, CANCELADO).contains(nuevo);
            case EN_PREPARACION -> Set.of(EN_CAMINO, CANCELADO).contains(nuevo);
            case EN_CAMINO -> nuevo == ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }
}
