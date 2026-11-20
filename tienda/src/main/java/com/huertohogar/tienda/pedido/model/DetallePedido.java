package com.huertohogar.tienda.pedido.model;

import com.huertohogar.tienda.inventario.model.Producto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Linea de la boleta. Guarda una copia del codigo, nombre y precios al momento de la compra:
 * si despues cambia el precio del producto, la boleta no cambia.
 */
@Entity
@Table(name = "detalle_pedido")
@Getter
@Setter
@NoArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 40)
    private String unidad;

    @Column(nullable = false)
    private long precioUnitario;

    /** Precio unitario con descuento aplicado */
    @Column(nullable = false)
    private long precioFinal;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private long subtotal;
}
