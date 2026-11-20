package com.huertohogar.tienda.catalogo.model;

import com.huertohogar.tienda.inventario.model.Producto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ficha comercial de un producto del inventario (relacion 1 a 1).
 * La entidad Producto original (nombre, descripcion, precio, stock) NO se modifico:
 * los datos que pide el caso y el frontend (codigo, categoria, origen, unidad,
 * stock minimo, practicas sostenibles, emoji, imagen, activo) viven aqui.
 */
@Entity
@Table(name = "productos_catalogo")
@Getter
@Setter
@NoArgsConstructor
public class ProductoCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codigo del caso: FR001, VR002, PO001... */
    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @OneToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /** Bajo este stock se genera una alerta */
    @Column(nullable = false)
    private int stockMinimo;

    /** Unidad de venta, ej: kilo, bolsa de 500g */
    @Column(length = 40)
    private String unidad;

    /** Lugar de origen, ej: Valle del Maule */
    @Column(length = 100)
    private String origen;

    @Column(length = 500)
    private String practicasSostenibles;

    @Column(length = 16)
    private String emoji;

    @Column(length = 300)
    private String imagenUrl;

    /** false = dado de baja (no se muestra en el catalogo, pero se conserva su historial) */
    @Column(nullable = false)
    private boolean activo = true;
}
