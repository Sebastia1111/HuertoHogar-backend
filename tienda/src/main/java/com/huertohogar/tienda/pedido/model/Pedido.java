package com.huertohogar.tienda.pedido.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Pedido confirmado; junto a sus detalles funciona como la boleta de la compra. */
@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numero de boleta, ej: HH-1001 */
    @Column(nullable = false, unique = true, length = 40)
    private String numero;

    /** Id del usuario en el microservicio usuarios (viene del JWT, sin FK) */
    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 100)
    private String correoCliente;

    @Column(length = 120)
    private String nombreCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDate fechaEntregaPreferida;

    @Column(length = 300)
    private String direccionEntrega;

    @Column(length = 60)
    private String comuna;

    @Column(length = 60)
    private String region;

    /** Suma a precio de lista */
    @Column(nullable = false)
    private long subtotal;

    /** Descuento aplicado (20% Duoc) */
    @Column(nullable = false)
    private long descuento;

    @Column(nullable = false)
    private long neto;

    @Column(nullable = false)
    private long iva;

    @Column(nullable = false)
    private long total;

    /** Referencia que envio el frontend (su numero local) */
    @Column(length = 60)
    private String referenciaCliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DetallePedido> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<HistorialEstadoPedido> historial = new ArrayList<>();

    @PrePersist
    void alCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
