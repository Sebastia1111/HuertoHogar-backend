package com.huertohogar.tienda.catalogo.model;

import com.huertohogar.tienda.inventario.model.Producto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Historial (trazabilidad) de cada cambio de stock: quien, cuando, cuanto y por que. */
@Entity
@Table(name = "movimientos_stock")
@Getter
@Setter
@NoArgsConstructor
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    /** Unidades movidas (siempre positivo; el tipo indica si suma o resta) */
    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private int stockAnterior;

    @Column(nullable = false)
    private int stockNuevo;

    @Column(length = 200)
    private String motivo;

    /** Ej: numero de pedido HH-1001 */
    @Column(length = 60)
    private String referencia;

    /** Correo de quien hizo el movimiento */
    @Column(length = 100)
    private String usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    void alCrear() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
